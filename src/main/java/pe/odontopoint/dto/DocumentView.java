package pe.odontopoint.dto;

public record DocumentView(
    Long id,
    String originalName,
    String description,
    String uploadedAt,
    String uploadedBy,
    String sha256) {}
