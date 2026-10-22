package pe.odontopoint.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import pe.odontopoint.dto.ServicioSolicitud;
import pe.odontopoint.entity.ServicioCatalogo;
import pe.odontopoint.service.CatalogoService;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {
  private final CatalogoService catalogo;

  public CatalogoController(CatalogoService catalogo) {
    this.catalogo = catalogo;
  }

  @GetMapping
  public List<ServicioCatalogo> listar() {
    return catalogo.listar();
  }

  @PostMapping
  public ServicioCatalogo crear(@Valid @RequestBody ServicioSolicitud datos, Principal usuario) {
    return catalogo.guardar(null, datos, usuario.getName());
  }

  @PutMapping("/{id}")
  public ServicioCatalogo editar(
      @PathVariable Long id, @Valid @RequestBody ServicioSolicitud datos, Principal usuario) {
    return catalogo.guardar(id, datos, usuario.getName());
  }
}
