package br.com.olivaras.api.order.service;

import br.com.olivaras.api.order.dto.OrderAddressResponse;
import br.com.olivaras.api.order.dto.OrderItemResponse;
import br.com.olivaras.api.order.entity.StoreOrderEntity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class BoletoPdfService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss z")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    public byte[] create(StoreOrderEntity order, List<OrderItemResponse> items) {
        List<String> lines = new ArrayList<>();
        OrderAddressResponse address = OrderAddressResponse.from(order);
        lines.add("Pedido: " + order.getId());
        lines.add("Referencia: " + order.getPaymentReference());
        lines.add("Status: " + order.getStatus() + " | Pagamento: " + order.getPaymentStatus());
        lines.add("Vencimento simulado: " + format(order.getPaymentExpiresAt()));
        lines.add("Valor total: " + order.getTotalGalleons() + " galeoes");
        lines.add("Codigo de pagamento ficticio: " + order.getPaymentInstructions());
        lines.add("");
        lines.add("Itens do pedido:");
        for (OrderItemResponse item : items) {
            lines.add(item.name() + " | quantidade: " + item.quantity()
                    + " | unitario: " + item.unitPriceGalleons()
                    + " galeoes | total: " + item.lineTotalGalleons() + " galeoes");
        }
        lines.add("");
        lines.add("Endereco de entrega:");
        lines.add(address.recipient());
        lines.add(address.street() + ", " + address.number()
                + (address.complement() == null ? "" : " - " + address.complement()));
        lines.add(address.district() + " - " + address.city() + "/" + address.state());
        lines.add("CEP: " + address.zipCode());
        if (address.reference() != null) lines.add("Referencia: " + address.reference());
        lines.add("");
        lines.add("DOCUMENTO FICTICIO PARA SIMULACAO ACADEMICA. NAO E UM BOLETO BANCARIO REAL.");
        lines.add("Nenhuma cobranca real sera efetuada.");

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (int start = 0; start < lines.size(); start += 44) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    float y = 770;
                    int end = Math.min(start + 44, lines.size());
                    for (int index = start; index < end; index++) {
                        boolean heading = index == 0;
                        content.beginText();
                        content.setFont(new PDType1Font(heading
                                ? Standard14Fonts.FontName.HELVETICA_BOLD
                                : Standard14Fonts.FontName.HELVETICA), heading ? 16 : 10);
                        content.newLineAtOffset(48, y);
                        content.showText(ascii(lines.get(index)));
                        content.endText();
                        y -= heading ? 30 : 16;
                    }
                }
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Nao foi possivel gerar o boleto simulado", exception);
        }
    }

    private String format(java.time.Instant instant) {
        return instant == null ? "N/A" : DATE_FORMAT.format(instant);
    }

    private String ascii(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.replaceAll("[^\\x20-\\x7E]", "?");
    }
}
