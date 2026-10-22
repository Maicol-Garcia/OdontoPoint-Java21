package pe.odontopoint.controller;

import java.io.IOException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.odontopoint.dto.*;
import pe.odontopoint.service.*;

@RestController
public class IngresosController {
  private final IngresosService ingresos;
  private final IngresosExcelService excel;
  public IngresosController(IngresosService ingresos, IngresosExcelService excel) {
    this.ingresos = ingresos; this.excel = excel;
  }

  @GetMapping("/api/ingresos")
  public ResponseEntity<IngresosVista> consultar(@ModelAttribute ConsultaIngresos filtro) {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ingresos.consultar(filtro));
  }

  @GetMapping("/exports/ingresos.xlsx")
  public ResponseEntity<byte[]> exportar(@ModelAttribute ConsultaIngresos filtro) throws IOException {
    var periodo = ingresos.resolver(filtro);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
            .filename("OdontoPoint-ingresos-" + periodo.desde() + "-a-" + periodo.hasta() + ".xlsx").build().toString())
        .cacheControl(CacheControl.noStore()).body(excel.exportar(filtro));
  }
}
