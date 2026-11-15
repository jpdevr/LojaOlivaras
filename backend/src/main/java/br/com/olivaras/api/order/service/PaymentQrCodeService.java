package br.com.olivaras.api.order.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PaymentQrCodeService {
    public String toDataUri(String content) {
        try {
            var hints = Map.<EncodeHintType, Object>of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.MARGIN, 2,
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            var matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", output);
                return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(output.toByteArray());
            }
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("Nao foi possivel gerar o QR code simulado", exception);
        }
    }
}
