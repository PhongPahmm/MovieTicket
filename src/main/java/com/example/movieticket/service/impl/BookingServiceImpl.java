package com.example.movieticket.service.impl;

import com.example.movieticket.common.BookingStatus;
import com.example.movieticket.common.PaymentMethod;
import com.example.movieticket.common.PaymentStatus;
import com.example.movieticket.common.SeatType;
import com.example.movieticket.dto.request.BookingRequest;
import com.example.movieticket.dto.response.BookedSeatResponse;
import com.example.movieticket.dto.response.BookingResponse;
import com.example.movieticket.model.*;
import com.example.movieticket.repository.*;
import com.example.movieticket.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingServiceImpl implements BookingService {
    BookingRepository bookingRepository;
    BookingSeatRepository bookingSeatRepository;
    PaymentRepository paymentRepository;
    UserRepository userRepository;
    ShowRepository showRepository;
    SeatRepository seatRepository;
    PriceRepository priceRepository;
    VNPayService vnPayService;

    @Transactional
    @Override
    public BookingResponse createBooking(BookingRequest request) {
        // Validate user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate show
        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new RuntimeException("Show not found"));

        // Validate seats availability
        List<Seat> seats = seatRepository.findAllById(request.getSeats());
        if (seats.size() != request.getSeats().size()) {
            throw new RuntimeException("Some seats not found");
        }

        // Check if seats are already booked for this show
        boolean seatsAlreadyBooked = bookingSeatRepository.existsByBooking_Show_IdAndSeat_IdIn(
                request.getShowId(), request.getSeats());
        if (seatsAlreadyBooked) {
            throw new RuntimeException("Some seats are already booked");
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

        // Create booking seats with current prices
        LocalDate currentDate = LocalDate.now();
        for (Seat seat : seats) {
            Optional<Price> priceOpt = priceRepository.findByShowAndSeatTypeAndDateBetween(
                    show, seat.getSeatType(), currentDate);

            int seatPrice = priceOpt.map(Price::getAmount)
                    .orElse(getDefaultPrice(seat.getSeatType()));

            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(booking)
                    .seat(seat)
                    .price(seatPrice)
                    .build();
            bookingSeatRepository.save(bookingSeat);
        }

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

        // Generate VNPay payment URL
        String paymentUrl = vnPayService.createOrder(
                (int) Math.round(totalAmount),
                String.valueOf(payment.getId()),
                request.getReturnUrl()
        );

        // Sử dụng mapToBookingResponse
        BookingResponse response = mapToBookingResponse(booking);

        // Thêm paymentUrl và returnUrl rồi trả về
        response.setPaymentUrl(paymentUrl);
        response.setReturnUrl(request.getReturnUrl());

        return response;
    }


    @Transactional
    @Override
    public BookingResponse handlePaymentReturn(HttpServletRequest request) {
        int paymentResult = vnPayService.orderReturn(request);

        String txnRef = request.getParameter("vnp_TxnRef");
        Integer paymentId = Integer.parseInt(txnRef);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        Booking booking = bookingRepository.findByPayment(payment)
                .orElseThrow(() -> new RuntimeException("Booking not found for payment: " + payment.getId()));

        if (paymentResult == 1) {
            // Payment successful
            payment.setStatus(PaymentStatus.SUCCESS);
            booking.setStatus(BookingStatus.CONFIRMED);
        } else if (paymentResult == 0) {
            // Payment failed
            payment.setStatus(PaymentStatus.FAILED);
            booking.setStatus(BookingStatus.CANCELLED);
        } else {
            // Invalid signature
            payment.setStatus(PaymentStatus.FAILED);
            booking.setStatus(BookingStatus.CANCELLED);
        }

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .paymentId(payment.getId())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentStatus(payment.getStatus())
                .build();
    }

    @Transactional
    @Override
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpireTimeBefore(BookingStatus.PENDING, now);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            if (booking.getPayment() != null) {
                booking.getPayment().setStatus(PaymentStatus.FAILED);
                paymentRepository.save(booking.getPayment());
            }
            bookingRepository.save(booking);
        }
    }

    @Override
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return mapToBookingResponse(booking);
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

    private BookingResponse mapToBookingResponse(Booking booking) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBooking(booking);

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .userId(booking.getUser().getId())
                .showId(booking.getShow().getId())
                .movieTitle(booking.getShow().getMovie().getTitle())
                .screenName(booking.getShow().getScreen().getName())
                .bookingTime(booking.getBookingTime())
                .expireTime(booking.getExpireTime())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentId(booking.getPayment() != null ? booking.getPayment().getId() : null)
                .paymentStatus(booking.getPayment() != null ? booking.getPayment().getStatus() : null)
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
            Optional<Price> priceOpt = priceRepository.findByShowAndSeatTypeAndDateBetween(
                    show, seat.getSeatType(), currentDate);

            int seatPrice = priceOpt.map(Price::getAmount)
                    .orElse(getDefaultPrice(seat.getSeatType()));

            totalAmount += seatPrice;
        }

        return totalAmount;
    }

    private int getDefaultPrice(SeatType seatType) {
        // Return default prices based on seat type
        return switch (seatType) {
            case VIP -> 150000;
            case COUPLE -> 200000;
            default -> 80000;
        };
    }
}