package pe.odontopoint.dto;

public record PaymentReceiptView(
    Long paymentId, String originalName, String uploadedAt, String uploadedBy, String sha256) {}
