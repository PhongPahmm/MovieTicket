package com.example.movieticket.service.impl;

import com.example.movieticket.common.*;
import com.example.movieticket.dto.request.BookingRequest;
import com.example.movieticket.dto.response.BookedSeatResponse;
import com.example.movieticket.dto.response.BookingResponse;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.SeatUpdateMessageResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.*;
import com.example.movieticket.repository.*;
import com.example.movieticket.service.BookingService;
import com.example.movieticket.service.EmailService;
import com.example.movieticket.service.PriceService;
import com.example.movieticket.service.QRCodeService;
import com.example.movieticket.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;
    BookingSeatRepository bookingSeatRepository;
    PaymentRepository paymentRepository;
    UserRepository userRepository;
    ShowRepository showRepository;
    SeatRepository seatRepository;
    PriceRepository priceRepository;
    PriceService priceService;
    VNPayService vnPayService;
    EmailService emailService;
    QRCodeService qrCodeService;
    UserService userService;
    BookingCleanUpService bookingCleanupService;
    SimpMessagingTemplate messagingTemplate;

    @Transactional
    @Override
    public BookingResponse createBooking(BookingRequest request, HttpServletRequest httpRequest) {
        var currentUser = userService.getCurrentUser();

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));

        // Validate seats availability
        List<Seat> seats = seatRepository.findAllById(request.getSeats());
        if (seats.size() != request.getSeats().size()) {
            throw new AppException(ErrorCode.SEAT_NOT_FOUND);
        }

        // Check if seats are already booked for this show (excluding cancelled bookings)
        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        List<BookingSeat> bookedSeats = bookingSeatRepository.findByShowAndSeatsWithBookingStatus(
                request.getShowId(), request.getSeats(), activeStatuses);
        
        if (!bookedSeats.isEmpty()) {
            List<Integer> bookedSeatIds = bookedSeats.stream()
                    .map(bs -> bs.getSeat().getId())
                    .toList();
            log.warn("Seats already booked: {} for show {}", bookedSeatIds, request.getShowId());
            throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
        }

        // Calculate total amount using Price table
        double totalAmount = calculateTotalAmount(show, seats);

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .bookingTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusMinutes(15))
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);

        // Collect seat IDs after saving booking seats
        List<Integer> seatIds = new ArrayList<>();

        // Create booking seats with current prices
        LocalDate currentDate = LocalDate.now();
        for (Seat seat : seats) {
            int seatPrice = getSeatPrice(show, seat.getSeatType(), currentDate);

            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(booking)
                    .seat(seat)
                    .price(seatPrice)
                    .status(SeatStatus.PENDING)
                    .build();
            bookingSeatRepository.save(bookingSeat);

            seatIds.add(seat.getId()); // Collect for WebSocket
        }
        //  Real-time seat status broadcast
        messagingTemplate.convertAndSend(
                "/topic/show/" + show.getId() + "/seats",
                new SeatUpdateMessageResponse(seatIds, SeatStatus.PENDING)
        );
        // Create payment record
        Payment payment = Payment.builder()
                .user(user)
                .amount(totalAmount)
                .paymentMethod(PaymentMethod.VN_PAY)
                .status(PaymentStatus.PENDING)
                .orderInfo("Booking #" + booking.getId() + " - " + show.getMovie().getTitle())
                .returnUrl(request.getReturnUrl())
                .build();

        payment = paymentRepository.save(payment);

        // Update booking with payment
        booking.setPayment(payment);
        booking = bookingRepository.save(booking);

        // Get client IP address
        String clientIp = getClientIp(httpRequest);
        
        // Generate VNPay payment URL with client IP
        String paymentUrl = vnPayService.createOrder(
                (int) Math.round(totalAmount),
                String.valueOf(payment.getId()),
                request.getReturnUrl(),
                clientIp
        );
        BookingResponse response = mapToBookingResponse(booking);

        response.setPaymentUrl(paymentUrl);
        response.setReturnUrl(request.getReturnUrl());

        return response;
    }


    @Transactional
    @Override
    public BookingResponse handlePaymentReturn(HttpServletRequest request) {
        log.info("Processing VNPay payment return callback");
        
        // Validate and get payment ID
        String paymentIdStr = request.getParameter("vnp_OrderInfo");
        if (paymentIdStr == null || paymentIdStr.isEmpty()) {
            log.error("Missing vnp_OrderInfo parameter in VNPay callback");
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        Integer paymentId;
        try {
            paymentId = Integer.valueOf(paymentIdStr);
        } catch (NumberFormatException e) {
            log.error("Invalid payment ID format: {}", paymentIdStr);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // Verify payment signature and process result
        int paymentResult = vnPayService.orderReturn(request);
        log.info("VNPay payment verification result: {} for payment ID: {}", paymentResult, paymentId);

        // Nếu signature không hợp lệ, throw exception ngay
        if (paymentResult == -1) {
            log.error("Invalid VNPay signature for payment ID: {}", paymentId);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found: {}", paymentId);
                    return new AppException(ErrorCode.PAYMENT_NOT_FOUND);
                });
        
        Booking booking = bookingRepository.findByPayment(payment)
                .orElseThrow(() -> {
                    log.error("Booking not found for payment: {}", payment.getId());
                    return new AppException(ErrorCode.BOOKING_NOT_FOUND);
                });
        if (paymentResult == 1) {
            // Payment successful
            payment.setStatus(PaymentStatus.SUCCESS);
            booking.setStatus(BookingStatus.CONFIRMED);
            List<BookingSeat> bookingSeats = bookingSeatRepository.findByBooking(booking);
            for (BookingSeat seat : bookingSeats) {
                seat.setStatus(SeatStatus.BOOKED);
            }
            bookingSeatRepository.saveAll(bookingSeats);

            // Generate and save QR code
            try {
                // Ensure bookingSeats are loaded
                booking.setBookingSeats(bookingSeats);
                
                String qrCode = qrCodeService.generateQRCode(booking);
                if (qrCode != null) {
                    booking.setQrCode(qrCode);
                    bookingRepository.save(booking);
                    log.info("QR code generated and saved for booking {}", booking.getId());
                } else {
                    log.warn("QR code generation returned null for booking {}", booking.getId());
                }
            } catch (Exception e) {
                log.error("Failed to generate QR code for booking {}", booking.getId(), e);
            }

            try {
                emailService.sendBookingConfirmationEmail(booking);
            } catch (MessagingException e) {
                log.error("Failed to send booking confirmation email", e);
            }
        } else {
            // Payment failed (paymentResult == 0)
            payment.setStatus(PaymentStatus.FAILED);
            booking.setStatus(BookingStatus.CANCELLED);
        }

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        return mapToBookingResponse(booking);
    }

    @Scheduled(fixedRate = 60000)
    public void autoCancelExpiredBookings() {
        bookingCleanupService.cancelExpiredBookings();
    }
    @Override
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Nếu booking đã thanh toán nhưng chưa có QR code, tạo QR code
        if (booking.getPayment() != null && 
            booking.getPayment().getStatus() == PaymentStatus.SUCCESS &&
            (booking.getQrCode() == null || booking.getQrCode().isEmpty())) {
            
            log.info("Booking {} is paid but has no QR code. Generating QR code...", booking.getId());
            try {
                // Ensure bookingSeats are loaded
                List<BookingSeat> bookingSeatsList = bookingSeatRepository.findByBooking(booking);
                booking.setBookingSeats(bookingSeatsList);
                
                log.info("Generating QR code for booking {} with {} seats", booking.getId(), bookingSeatsList.size());
                String qrCode = qrCodeService.generateQRCode(booking);
                if (qrCode != null && !qrCode.isEmpty()) {
                    booking.setQrCode(qrCode);
                    booking = bookingRepository.save(booking);
                    log.info("QR code generated and saved for booking {}. QR code length: {}", booking.getId(), qrCode.length());
                } else {
                    log.warn("QR code generation returned null or empty for booking {}", booking.getId());
                }
            } catch (Exception e) {
                log.error("Failed to generate QR code for booking {}", booking.getId(), e);
                e.printStackTrace(); // Print full stack trace for debugging
            }
        } else {
            log.debug("Booking {} - Payment status: {}, Has QR code: {}", 
                    booking.getId(),
                    booking.getPayment() != null ? booking.getPayment().getStatus() : "null",
                    booking.getQrCode() != null && !booking.getQrCode().isEmpty());
        }

        // Reload booking to ensure we have the latest data including QR code
        booking = bookingRepository.findById(booking.getId()).orElse(booking);
        return mapToBookingResponse(booking);
    }

    @Override
    public BookingResponse getBookingByIdWithPaymentUrl(Integer bookingId, HttpServletRequest httpRequest) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        BookingResponse response = mapToBookingResponse(booking);
        
        // Nếu booking chưa thanh toán, tạo lại payment URL
        if (response.getPaymentStatus() != PaymentStatus.SUCCESS && 
            booking.getPayment() != null) {
            
            Payment payment = booking.getPayment();
            String clientIp = getClientIp(httpRequest);
            String returnUrl = payment.getReturnUrl() != null ? payment.getReturnUrl() : 
                              httpRequest.getScheme() + "://" + httpRequest.getServerName() + 
                              (httpRequest.getServerPort() != 80 && httpRequest.getServerPort() != 443 ? 
                               ":" + httpRequest.getServerPort() : "") + 
                              "/booking-return";
            
            String paymentUrl = vnPayService.createOrder(
                    (int) Math.round(payment.getAmount()),
                    String.valueOf(payment.getId()),
                    returnUrl,
                    clientIp
            );
            
            response.setPaymentUrl(paymentUrl);
            response.setReturnUrl(returnUrl);
        }
        
        return response;
    }

    @Override
    public PageResponse<BookingResponse> getCurrentUserBookings(Integer userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingTime").descending());
        Page<Booking> bookingPage = bookingRepository.findByUser(user, pageable);

        List<BookingResponse> responseList = bookingPage.getContent().stream()
                .map(this::mapToBookingResponse)
                .toList();

        return PageResponse.<BookingResponse>builder()
                .currentPage(bookingPage.getNumber())
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalItems(bookingPage.getTotalElements())
                .items(responseList)
                .build();
    }

    @Override
    public List<BookingResponse> getUserBookings(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUserOrderByBookingTimeDesc(user);

        return bookings.stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    @Override
    public Booking getBookingEntityById(Integer bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
    }

    @Override
    public PageResponse<BookingResponse> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingTime"));
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);

        List<BookingResponse> bookingResponses = bookingPage.getContent().stream()
                .map(this::mapToBookingResponse)
                .toList();

        return PageResponse.<BookingResponse>builder()
                .currentPage(bookingPage.getNumber())
                .pageSize(bookingPage.getSize())
                .totalPages(bookingPage.getTotalPages())
                .totalItems(bookingPage.getTotalElements())
                .items(bookingResponses)
                .build();
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }

        // Set booking status to cancelled
        booking.setStatus(BookingStatus.CANCELLED);
        
        // Release the seats
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBooking(booking);
        for (BookingSeat bookingSeat : bookingSeats) {
            Seat seat = bookingSeat.getSeat();
            seat.setSeatStatus(SeatStatus.AVAILABLE);
            seatRepository.save(seat);
        }

        // Save the booking
        bookingRepository.save(booking);

        // Notify via WebSocket about seat status change
        Show show = booking.getShow();
        List<Integer> seatIds = bookingSeats.stream()
                .map(bs -> bs.getSeat().getId())
                .toList();
        
        SeatUpdateMessageResponse seatUpdate = SeatUpdateMessageResponse.builder()
                .seatIds(seatIds)
                .seatStatus(SeatStatus.AVAILABLE)
                .build();

        // Send seat updates to the show topic via WebSocket
        messagingTemplate.convertAndSend("/topic/show/" + show.getId(), seatUpdate);
        log.info("Sent seat update notification for show {} - {} seats released", show.getId(), seatIds.size());

        return mapToBookingResponse(booking);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBooking(booking);
        
        // Log để debug QR code
        log.debug("Mapping booking {} to response. Has QR code: {}", 
                booking.getId(), 
                booking.getQrCode() != null && !booking.getQrCode().isEmpty());

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUser().getId())
                .showId(booking.getShow().getId())
                .movieTitle(booking.getShow().getMovie().getTitle())
                .screenName(booking.getShow().getScreen().getName())
                .showDate(booking.getShow().getShowDate())
                .startTime(booking.getShow().getStartTime())
                .bookingTime(booking.getBookingTime())
                .expireTime(booking.getExpireTime())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentId(booking.getPayment() != null ? booking.getPayment().getId() : null)
                .paymentStatus(booking.getPayment() != null ? booking.getPayment().getStatus() : null)
                .qrCode(booking.getQrCode())
                .seats(bookingSeats.stream()
                        .map(bs -> BookedSeatResponse.builder()
                                .seatId(bs.getSeat().getId())
                                .row(bs.getSeat().getSeatRow())
                                .number(bs.getSeat().getNumber())
                                .seatType(bs.getSeat().getSeatType())
                                .price(bs.getPrice())
                                .build())
                        .toList())
                .build();
    }

    private double calculateTotalAmount(Show show, List<Seat> seats) {
        LocalDate currentDate = LocalDate.now();
        double totalAmount = 0;

        for (Seat seat : seats) {
            int seatPrice = getSeatPrice(show, seat.getSeatType(), currentDate);
            totalAmount += seatPrice;
        }

        return totalAmount;
    }

    /**
     * Get seat price with fallback logic:
     * 1. Try to get price from Price table (show-specific price)
     * 2. If not found, try to get price from ScreenPrice (screen default price)
     * 3. If still not found, use default hardcoded price
     */
    private int getSeatPrice(Show show, SeatType seatType, LocalDate date) {
        // Try to get price from Price table (show-specific price)
        Optional<Price> priceOpt = priceRepository.findByShowAndSeatTypeAndDateBetween(
                show, seatType, date);
        
        if (priceOpt.isPresent()) {
            return priceOpt.get().getAmount();
        }
        
        // Fallback to ScreenPrice (screen default price)
        Integer screenPrice = priceService.getAmountByScreenAndSeat(
                show.getScreen().getId(), seatType);
        
        if (screenPrice != null && screenPrice > 0) {
            return screenPrice;
        }
        
        // Final fallback to default price
        return getDefaultPrice(seatType);
    }

    private int getDefaultPrice(SeatType seatType) {
        // Return default prices based on seat type
        return switch (seatType) {
            case VIP -> 150000;
            case COUPLE -> 200000;
            case STANDARD -> 100000;
        };
    }

    /**
     * Get client IP address from HTTP request, handling proxy and load balancer headers
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // X-Forwarded-For can contain multiple IPs, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}