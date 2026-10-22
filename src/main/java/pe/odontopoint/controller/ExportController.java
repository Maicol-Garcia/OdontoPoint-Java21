package pe.odontopoint.controller;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.odontopoint.mapper.ReportMapper;
import pe.odontopoint.service.*;

@RestController
public class ExportController {
  private final HistoryService histories;
  private final ReportService reports;
  private final ExcelService excel;
  private final ReportMapper mapper;

  @GetMapping("/exports/patients.xlsx")
  public ResponseEntity<byte[]> all() throws Exception {
    return file(
        excel.export(histories.all()),
        "OdontoPoint-pacientes.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
  }

  @GetMapping("/exports/patients/{id}.xlsx")
  public ResponseEntity<byte[]> one(@PathVariable Long id) throws Exception {
    var h = histories.find(id);
    return file(
        excel.export(List.of(h)),
        h.number() + ".xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
  }

  @GetMapping("/exports/patients/{id}.pdf")
  public ResponseEntity<byte[]> pdf(@PathVariable Long id) throws Exception {
    var h = histories.find(id);
    return file(reports.export(mapper.map(h)), h.number() + ".pdf", "application/pdf");
  }

  private ResponseEntity<byte[]> file(byte[] bytes, String name, String type) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(type))
        .header(
            "Content-Disposition",
            ContentDisposition.attachment().filename(name).build().toString())
        .cacheControl(CacheControl.noStore())
        .body(bytes);
  }

  public ExportController(
      final HistoryService histories,
      final ReportService reports,
      final ExcelService excel,
      final ReportMapper mapper) {
    this.histories = histories;
    this.reports = reports;
    this.excel = excel;
    this.mapper = mapper;
  }
}
