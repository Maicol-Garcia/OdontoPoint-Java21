package pe.odontopoint.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ServicioSolicitud(
    @NotNull @PositiveOrZero Long version,
    @NotBlank @Size(max = 120) String nombre,
    @Size(max = 1000) String descripcion,
    @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precio,
    @Min(5) @Max(480) int duracionMinutos,
    boolean activo) {}
