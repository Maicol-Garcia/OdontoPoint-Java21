package pe.odontopoint.dto;

import java.math.BigDecimal;

public record HistoryView(
    Long id,
    String number,
    long version,
    String createdAt,
    String updatedAt,
    int age,
    BigDecimal total,
    BigDecimal paid,
    BigDecimal balance,
    HistoryRequest form) {}
