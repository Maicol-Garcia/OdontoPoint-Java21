package pe.odontopoint.service;

import java.nio.file.*;
import java.util.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.springframework.stereotype.Service;
import pe.odontopoint.report.*;

@Service
public class ReportService {
  private final JasperReport template;

  public ReportService() throws JRException {
    try (var in = getClass().getResourceAsStream("/reports/history.jrxml")) {
      template = JasperCompileManager.compileReport(in);
    } catch (java.io.IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public byte[] export(ReportData h) throws Exception {
    if (h.id == 0) throw new IllegalArgumentException("Guarde la historia antes de exportar.");
    java.util.List<Map<String, ?>> rows = new ArrayList<>();
    add(rows, "1. ANAMNESIS", "");
    for (String k :
        java.util.List.of(
            "Nombre completo",
            "Sexo",
            "Edad",
            "DNI",
            "Fecha de nacimiento",
            "Domicilio",
            "Lugar de procedencia",
            "Ocupación",
            "Estado civil",
            "Teléfono",
            "Apoderado: nombre",
            "Apoderado: DNI",
            "Apoderado: teléfono",
            "Motivo de consulta")) add(rows, k, h.value(k));
    add(rows, "2. ENFERMEDAD ACTUAL", "");
    add(rows, "Signos y síntomas principales", h.value("Signos y síntomas principales"));
    add(rows, "Tiempo de enfermedad", h.value("Tiempo de enfermedad"));
    add(rows, "3. ANTECEDENTES PERSONALES PATOLÓGICOS", "");
    for (String k : HistoryService.CONDITIONS)
      add(rows, k, h.conditions.getOrDefault(k, "No informado"));
    add(rows, "Alergia: ¿a qué?", h.value("Alergia: ¿a qué?"));
    add(rows, "Otros antecedentes", h.value("Otros antecedentes"));
    add(rows, "EXAMEN Y DIAGNÓSTICO", "");
    add(rows, "Examen clínico", h.value("Examen clínico"));
    add(rows, "Diagnóstico", h.value("Diagnóstico"));
    add(rows, "4. TRATAMIENTO Y PRESUPUESTO", "");
    for (var t : h.treatments) add(rows, t.description(), "S/ " + t.amount().toPlainString());
    add(
        rows,
        "Presupuesto total / pagado / saldo",
        "S/ " + h.total() + " / S/ " + h.paid() + " / S/ " + h.total().subtract(h.paid()));
    add(rows, "5. PAGOS REALIZADOS", "");
    for (var a : h.payments)
      add(
          rows,
          a.date() + " · S/ " + a.amount(),
          "Acta/comprobante: " + a.receipt() + " · Firmante registrado: " + a.signer());
    add(rows, "6. CONSENTIMIENTO INFORMADO", "");
    add(rows, "Estado del consentimiento", h.value("Estado del consentimiento"));
    add(
        rows,
        "Procedimiento, riesgos, beneficios y alternativas",
        h.value("Detalle del consentimiento"));
    add(rows, "Fecha del consentimiento", h.value("Fecha del consentimiento"));
    add(rows, "Revocatoria y fecha", h.value("Revocatoria"));
    add(rows, "Profesional tratante", h.value("Profesional tratante"));
    add(rows, "Firma del operador / DNI", "____________________________________________");
    add(
        rows,
        "Firma del paciente o representante / DNI / huella",
        "____________________________________________");
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("NUMBER", h.number());
    parameters.put("NAME", h.value("Nombre completo"));
    parameters.put(
        "DATE", "Registro: " + formatted(h.created) + " · Actualización: " + formatted(h.updated));
    parameters.put("ODONTOGRAM", new OdontogramRenderer(h).image());
    parameters.put("NOTES", h.value("Observaciones del odontograma"));
    parameters.put(
        "LEGEND",
        "Negro: "
            + h.value("Leyenda negro")
            + " · Amarillo: "
            + h.value("Leyenda amarillo")
            + " · Verde: "
            + h.value("Leyenda verde"));
    var print =
        JasperFillManager.fillReport(template, parameters, new JRMapCollectionDataSource(rows));
    return JasperExportManager.exportReportToPdf(print);
  }

  private String formatted(String date) {
    return date;
  }

  private void add(java.util.List<Map<String, ?>> rows, String label, String value) {
    rows.add(
        Map.of(
            "label",
            label,
            "value",
            value == null || value.isBlank() ? "—" : value,
            "heading",
            label.matches("[1-7][.].*") || label.equals("EXAMEN Y DIAGNÓSTICO")));
  }
}
