package pe.odontopoint.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import pe.odontopoint.entity.EstadoCita;

public record CitaSolicitud(
    @NotNull @PositiveOrZero Long version,
    @NotNull @Positive Long historiaId,
    Long servicioId,
    @NotNull LocalDateTime inicio,
    @NotNull LocalDateTime fin,
    @NotNull EstadoCita estado,
    @NotBlank @Size(max = 500) String motivo,
    @Size(max = 180) String profesional,
    @Size(max = 1000) String notas) {}
