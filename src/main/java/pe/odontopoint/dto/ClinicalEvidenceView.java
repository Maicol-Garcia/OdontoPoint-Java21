package pe.odontopoint.dto;

public record ClinicalEvidenceView(
    Long id,
    String type,
    String title,
    String description,
    String originalName,
    String uploadedAt,
    String uploadedBy,
    String sha256) {}
