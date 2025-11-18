package com.example.movieticket.service.impl;

import com.example.movieticket.model.Booking;
import com.example.movieticket.service.QRCodeService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QRCodeServiceImpl implements QRCodeService {

    private static final int QR_CODE_SIZE = 300;
    private static final String QR_CODE_FORMAT = "PNG";

    @Override
    public String generateQRCode(Booking booking) {
        try {
            // Build QR code content
            String qrContent = buildQRContent(booking);
            
            // Create QR code writer
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            
            // Generate QR code
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);
            
            // Convert to BufferedImage
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            // Convert to Base64 string
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, QR_CODE_FORMAT, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            return "data:image/png;base64," + base64Image;
        } catch (Exception e) {
            log.error("Error generating QR code for booking {}", booking.getId(), e);
            return null;
        }
    }
    @Override
    public String buildQRContent(Booking booking) {
        StringBuilder content = new StringBuilder();
        content.append("BOOKING ID: ").append(booking.getId()).append("\n");
        content.append("MOVIE: ").append(booking.getShow().getMovie().getTitle()).append("\n");
        content.append("SCREEN: ").append(booking.getShow().getScreen().getName()).append("\n");
        content.append("DATE: ").append(booking.getShow().getShowDate()).append("\n");
        content.append("TIME: ").append(booking.getShow().getStartTime()).append("\n");
        content.append("SEATS: ");
        
        if (booking.getBookingSeats() != null && !booking.getBookingSeats().isEmpty()) {
            String seats = booking.getBookingSeats().stream()
                    .map(bs -> bs.getSeat().getSeatRow() + String.valueOf(bs.getSeat().getNumber()))
                    .collect(Collectors.joining(", "));
            content.append(seats);
        }
        
        content.append("\n");
        content.append("AMOUNT: ").append(booking.getTotalAmount()).append(" VND");
        
        return content.toString();
    }
}

