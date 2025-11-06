package com.example.movieticket.service.impl;

import com.example.movieticket.model.Booking;
import com.example.movieticket.model.BookingSeat;
import com.example.movieticket.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {

    JavaMailSender mailSender;

    @Override
    public void sendBookingConfirmationEmail(Booking booking) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(booking.getUser().getEmail());
        helper.setSubject("Xác nhận đặt vé - " + booking.getShow().getMovie().getTitle());

        String htmlContent = buildEmailContent(booking);
        helper.setText(htmlContent, true);

        // Attach QR code if available
        if (booking.getQrCode() != null && !booking.getQrCode().isEmpty()) {
            try {
                // Extract base64 data from data URI
                String base64Data = booking.getQrCode();
                if (base64Data.startsWith("data:image")) {
                    base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                }
                
                byte[] qrCodeBytes = Base64.getDecoder().decode(base64Data);
                helper.addAttachment("QRCode.png", 
                    new ByteArrayDataSource(qrCodeBytes, "image/png"));
            } catch (Exception e) {
                // Log error but don't fail email sending
                System.err.println("Failed to attach QR code to email: " + e.getMessage());
            }
        }

        mailSender.send(message);
    }

    private String buildEmailContent(Booking booking) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        List<BookingSeat> seats = booking.getBookingSeats();

        StringBuilder seatInfo = new StringBuilder();
        for (BookingSeat seat : seats) {
            seatInfo.append("<li>")
                    .append("Ghế: ").append(seat.getSeat().getSeatRow()).append(seat.getSeat().getNumber())
                    .append(" (").append(seat.getSeat().getSeatType()).append(") - ")
                    .append(currencyFormat.format(seat.getPrice()))
                    .append("</li>");
        }

        // Build QR code image tag if available
        String qrCodeHtml = "";
        if (booking.getQrCode() != null && !booking.getQrCode().isEmpty()) {
            qrCodeHtml = String.format("""
                <div style="text-align: center; margin: 20px 0;">
                    <h3>Mã QR Code vé của bạn:</h3>
                    <img src="%s" alt="QR Code" style="max-width: 300px; border: 2px solid #333; padding: 10px; background: white;" />
                    <p style="font-size: 12px; color: #666; margin-top: 10px;">Vui lòng xuất trình mã QR này khi đến rạp</p>
                </div>
                """, booking.getQrCode());
        }

        return """
            <html>
            <body>
                <h2>Chào %s,</h2>
                <p>Cảm ơn bạn đã đặt vé tại <strong>MovieTicket</strong>!</p>
                <p><strong>Mã đặt vé:</strong> %d</p>
                <p><strong>Phim:</strong> %s</p>
                <p><strong>Phòng chiếu:</strong> %s</p>
                <p><strong>Ngày chiếu:</strong> %s</p>
                <p><strong>Giờ chiếu:</strong> %s</p>
                <p><strong>Thời gian đặt:</strong> %s</p>
                <p><strong>Tổng tiền:</strong> %s</p>
                <p><strong>Chi tiết ghế:</strong></p>
                <ul>
                    %s
                </ul>
                %s
                <p>Vui lòng đến sớm 15 phút để nhận vé và vào rạp đúng giờ.</p>
                <p>Chúc bạn có trải nghiệm tuyệt vời cùng MovieTicket!</p>
            </body>
            </html>
            """.formatted(
                booking.getUser().getUsername(),
                booking.getId(),
                booking.getShow().getMovie().getTitle(),
                booking.getShow().getScreen().getName(),
                booking.getShow().getShowDate().format(dateFormatter),
                booking.getShow().getStartTime().format(timeFormatter),
                booking.getBookingTime().format(fullFormatter),
                currencyFormat.format(booking.getTotalAmount()),
                seatInfo,
                qrCodeHtml
        );
    }
}
