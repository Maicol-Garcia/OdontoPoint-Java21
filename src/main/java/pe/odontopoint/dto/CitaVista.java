package pe.odontopoint.dto;

import java.time.LocalDateTime;
import pe.odontopoint.entity.*;

public record CitaVista(
    Long id,
    long version,
    Long historiaId,
    String paciente,
    Long servicioId,
    String servicio,
    LocalDateTime inicio,
    LocalDateTime fin,
    EstadoCita estado,
    String motivo,
    String profesional,
    String notas) {
  public static CitaVista desde(Cita c) {
    return new CitaVista(
        c.getId(),
        c.getVersion(),
        c.getHistoria().getId(),
        c.getHistoria().getFullName(),
        c.getServicio() == null ? null : c.getServicio().getId(),
        c.getServicio() == null ? "Consulta" : c.getServicio().getNombre(),
        c.getInicio(),
        c.getFin(),
        c.getEstado(),
        c.getMotivo(),
        c.getProfesional(),
        c.getNotas());
  }
}
