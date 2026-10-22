package pe.odontopoint.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import pe.odontopoint.dto.*;
import pe.odontopoint.repository.PatientHistoryRepository;
import pe.odontopoint.service.AgendaService;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {
  private final AgendaService agenda;
  private final PatientHistoryRepository historias;

  public AgendaController(AgendaService agenda, PatientHistoryRepository historias) {
    this.agenda = agenda;
    this.historias = historias;
  }

  @GetMapping
  public List<CitaVista> listar(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
    return agenda.listar(desde, hasta);
  }

  @PostMapping
  public CitaVista crear(@Valid @RequestBody CitaSolicitud datos, Principal usuario) {
    return agenda.guardar(null, datos, usuario.getName());
  }

  @PutMapping("/{id}")
  public CitaVista editar(
      @PathVariable Long id, @Valid @RequestBody CitaSolicitud datos, Principal usuario) {
    return agenda.guardar(id, datos, usuario.getName());
  }

  public record PacienteOpcion(Long id, String nombre, String dni) {}

  @GetMapping("/pacientes")
  public List<PacienteOpcion> pacientes(@RequestParam(defaultValue = "") String q) {
    return historias
        .search(q, PageRequest.of(0, 20))
        .map(h -> new PacienteOpcion(h.getId(), h.getFullName(), h.getDni()))
        .getContent();
  }
}
