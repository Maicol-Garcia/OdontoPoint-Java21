package pe.odontopoint.service;

import java.io.*;
import java.time.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import pe.odontopoint.dto.HistoryView;
import pe.odontopoint.mapper.ReportMapper;
import pe.odontopoint.util.ClinicalDates;

@Service
public class ExcelService {
  private final ReportMapper mapper;

  public byte[] export(List<HistoryView> histories) throws IOException {
    try (var workbook = new XSSFWorkbook();
        var out = new ByteArrayOutputStream()) {
      var header = workbook.createCellStyle();
      header.setFillForegroundColor(IndexedColors.TEAL.getIndex());
      header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      var font = workbook.createFont();
      font.setBold(true);
      font.setColor(IndexedColors.WHITE.getIndex());
      header.setFont(font);
      header.setWrapText(true);
      var date = workbook.createCellStyle();
      date.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
      var money = workbook.createCellStyle();
      money.setDataFormat(workbook.createDataFormat().getFormat("\"S/\" #,##0.00"));
      var patients =
          sheet(
              workbook,
              "Pacientes",
              header,
              "Historia",
              "Paciente",
              "DNI",
              "Nacimiento",
              "Fecha de registro",
              "Teléfono",
              "Presupuesto (S/)",
              "Pagado (S/)",
              "Saldo (S/)");
      var clinical = sheet(workbook, "Historia clínica", header, "Historia", "Campo", "Valor");
      var conditions =
          sheet(workbook, "Antecedentes", header, "Historia", "Antecedente", "Respuesta");
      var teeth =
          sheet(workbook, "Odontograma", header, "Historia", "Diente", "Superficie", "Color");
      var treatments =
          sheet(workbook, "Tratamientos", header, "Historia", "Tratamiento", "Importe (S/)");
      var payments =
          sheet(
              workbook,
              "Pagos",
              header,
              "Historia",
              "Fecha",
              "Importe (S/)",
              "Acta o comprobante",
              "Firmante");
      for (var h : histories) {
        var row = patients.createRow(patients.getLastRowNum() + 1);
        text(row, 0, h.number());
        text(row, 1, h.form().getFullName());
        text(row, 2, h.form().getDni());
        row.createCell(3).setCellValue(ClinicalDates.parse(h.form().getBirthDate(), "Nacimiento"));
        row.getCell(3).setCellStyle(date);
        row.createCell(4)
            .setCellValue(LocalDate.parse(h.createdAt().substring(0, 10), ClinicalDates.DATE));
        row.getCell(4).setCellStyle(date);
        text(row, 5, h.form().getPhone());
        number(row, 6, h.total(), money);
        number(row, 7, h.paid(), money);
        row.createCell(8)
            .setCellFormula("G" + (row.getRowNum() + 1) + "-H" + (row.getRowNum() + 1));
        row.getCell(8).setCellStyle(money);
        mapper.map(h).fields.forEach((k, v) -> add(clinical, h.number(), k, v));
        h.form().getConditions().forEach((k, v) -> add(conditions, h.number(), k, v));
        h.form()
            .getTeeth()
            .forEach(
                (k, v) -> {
                  var a = k.split(":");
                  add(teeth, h.number(), a[0], a[1], v);
                });
        for (var t : h.form().getTreatments()) {
          var r = add(treatments, h.number(), t.description());
          number(r, 2, t.amount(), money);
        }
        for (var a : h.form().getPayments()) {
          var r = add(payments, h.number(), "", "", a.receiptName(), a.signer());
          r.getCell(1).setCellValue(ClinicalDates.parse(a.date(), "Pago"));
          r.getCell(1).setCellStyle(date);
          number(r, 2, a.amount(), money);
        }
      }
      for (Sheet s : workbook) {
        s.createFreezePane(0, 1);
        s.setAutoFilter(
            new CellRangeAddress(
                0, Math.max(0, s.getLastRowNum()), 0, s.getRow(0).getLastCellNum() - 1));
        for (int c = 0; c < s.getRow(0).getLastCellNum(); c++)
          s.setColumnWidth(c, c == 1 ? 34 * 256 : 24 * 256);
        s.setDefaultRowHeightInPoints(22);
      }
      clinical.setColumnWidth(2, 80 * 256);
      treatments.setColumnWidth(1, 55 * 256);
      workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
      workbook.write(out);
      return out.toByteArray();
    }
  }

  private Sheet sheet(XSSFWorkbook w, String name, CellStyle style, String... labels) {
    var s = w.createSheet(name);
    var r = s.createRow(0);
    r.setHeightInPoints(32);
    for (int i = 0; i < labels.length; i++) {
      text(r, i, labels[i]);
      r.getCell(i).setCellStyle(style);
    }
    return s;
  }

  private Row add(Sheet s, String... values) {
    var r = s.createRow(s.getLastRowNum() + 1);
    for (int i = 0; i < values.length; i++) text(r, i, values[i]);
    return r;
  }

  private void text(Row r, int i, String value) {
    r.createCell(i, CellType.STRING).setCellValue(value == null ? "" : value);
  }

  private void number(Row r, int i, java.math.BigDecimal value, CellStyle style) {
    var c = r.getCell(i);
    if (c == null) c = r.createCell(i);
    c.setCellValue(value.doubleValue());
    c.setCellStyle(style);
  }

  public ExcelService(final ReportMapper mapper) {
    this.mapper = mapper;
  }
}
