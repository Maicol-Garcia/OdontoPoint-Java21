package pe.odontopoint.service;

import java.time.*;
import java.util.List;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.odontopoint.dto.*;
import pe.odontopoint.entity.*;
import pe.odontopoint.exception.*;
import pe.odontopoint.repository.*;

@Service
public class AgendaService {
  private final CitaRepository citas;
  private final PatientHistoryRepository historias;
  private final ServicioCatalogoRepository servicios;
  private final SequenceCounterRepository contadores;
  private final AuditService auditoria;

  public AgendaService(
      CitaRepository citas,
      PatientHistoryRepository historias,
      ServicioCatalogoRepository servicios,
      SequenceCounterRepository contadores,
      AuditService auditoria) {
    this.citas = citas;
    this.historias = historias;
    this.servicios = servicios;
    this.contadores = contadores;
    this.auditoria = auditoria;
  }

  @Transactional(readOnly = true)
  public List<CitaVista> listar(LocalDate desde, LocalDate hasta) {
    if (!hasta.isAfter(desde) || hasta.isAfter(desde.plusDays(62)))
      throw new BusinessException("Consulte un intervalo entre 1 y 62 días.");
    return citas.intervalo(desde.atStartOfDay(), hasta.atStartOfDay()).stream()
        .map(CitaVista::desde)
        .toList();
  }

  @Transactional
  public CitaVista guardar(Long id, CitaSolicitud d, String usuario) {
    if (!d.fin().isAfter(d.inicio())
        || Duration.between(d.inicio(), d.fin()).toMinutes() > 480
        || !d.inicio().toLocalDate().equals(d.fin().toLocalDate()))
      throw new BusinessException(
          "La cita debe terminar después de su inicio, el mismo día y durar como máximo 8 horas.");
    // Serializa las reservas del consultorio, incluso desde dos sesiones simultáneas.
    if (contadores.locked("agenda") == null)
      throw new BusinessException("Reinicie el sistema para inicializar la agenda.");
    var c =
        id == null
            ? new Cita()
            : citas.findById(id).orElseThrow(() -> new NotFoundException("Cita no encontrada."));
    if (id != null && c.getVersion() != d.version())
      throw new OptimisticLockingFailureException("La cita cambió.");
    if (d.estado() != EstadoCita.CANCELADA && citas.cruces(d.inicio(), d.fin(), id) > 0)
      throw new BusinessException(
          "El horario se cruza con otra cita. Seleccione un horario disponible.");
    var h =
        historias
            .findById(d.historiaId())
            .orElseThrow(() -> new NotFoundException("Paciente no encontrado."));
    ServicioCatalogo s = null;
    if (d.servicioId() != null) {
      s =
          servicios
              .findById(d.servicioId())
              .orElseThrow(() -> new NotFoundException("Servicio no encontrado."));
      if (!s.getActivo() && (c.getServicio() == null || !s.getId().equals(c.getServicio().getId())))
        throw new BusinessException("Seleccione un servicio activo.");
    }
    c.setHistoria(h);
    c.setServicio(s);
    c.setInicio(d.inicio());
    c.setFin(d.fin());
    c.setEstado(d.estado());
    c.setMotivo(d.motivo().trim());
    c.setProfesional(d.profesional());
    c.setNotas(d.notas());
    citas.saveAndFlush(c);
    auditoria.record(h.getId(), "Cita " + c.getId() + ": " + d.estado(), usuario);
    return CitaVista.desde(c);
  }
}
