package com.example.movieticket.service.impl;

import com.example.movieticket.model.Booking;
import com.example.movieticket.model.BookingSeat;
import com.example.movieticket.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
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
                seatInfo
        );
    }
}
