package pe.odontopoint.service;

import java.util.List;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.odontopoint.dto.*;
import pe.odontopoint.entity.*;
import pe.odontopoint.exception.*;
import pe.odontopoint.repository.*;

@Service
public class CatalogoService {
  private final ServicioCatalogoRepository repositorio;
  private final AuditService auditoria;

  public CatalogoService(ServicioCatalogoRepository repositorio, AuditService auditoria) {
    this.repositorio = repositorio;
    this.auditoria = auditoria;
  }

  @Transactional(readOnly = true)
  public List<ServicioCatalogo> listar() {
    return repositorio.findAllByOrderByNombreAsc();
  }

  @Transactional
  public ServicioCatalogo guardar(Long id, ServicioSolicitud datos, String usuario) {
    var s =
        id == null
            ? new ServicioCatalogo()
            : repositorio
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Servicio no encontrado."));
    if (id != null && s.getVersion() != datos.version())
      throw new OptimisticLockingFailureException("El servicio cambió.");
    s.setNombre(datos.nombre().trim());
    s.setDescripcion(datos.descripcion());
    s.setPrecio(datos.precio());
    s.setDuracionMinutos(datos.duracionMinutos());
    s.setActivo(datos.activo());
    repositorio.saveAndFlush(s);
    auditoria.record(
        null, "Servicio " + s.getId() + ": " + (id == null ? "creado" : "actualizado"), usuario);
    return s;
  }
}
