package pe.odontopoint.service;

import java.io.*;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import pe.odontopoint.dto.*;
import pe.odontopoint.exception.BusinessException;
import pe.odontopoint.util.ClinicalDates;

/** Exportación por lotes con el mismo filtro y la misma transacción de lectura. */
@Service
public class IngresosExcelService {
  private final IngresosService ingresos;
  public IngresosExcelService(IngresosService ingresos) { this.ingresos = ingresos; }

  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public byte[] exportar(ConsultaIngresos filtro) throws IOException {
    var consulta = ingresos.resolver(filtro);
    var vista = ingresos.consultar(filtro);
    if (vista.detalle().totalRegistros() > 50000)
      throw new BusinessException("La exportación admite hasta 50 000 pagos. Reduzca el período o filtre por paciente.");
    var libro = new SXSSFWorkbook(200);
    libro.setCompressTempFiles(true);
    try (var salida = new ByteArrayOutputStream()) {
      var cabecera = libro.createCellStyle();
      cabecera.setFillForegroundColor(IndexedColors.TEAL.getIndex());
      cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      var fuente = libro.createFont(); fuente.setBold(true); fuente.setColor(IndexedColors.WHITE.getIndex());
      cabecera.setFont(fuente); cabecera.setWrapText(true);
      var moneda = libro.createCellStyle();
      moneda.setDataFormat(libro.createDataFormat().getFormat("\"S/\" #,##0.00"));
      var fecha = libro.createCellStyle();
      fecha.setDataFormat(libro.createDataFormat().getFormat("dd/mm/yyyy"));

      var resumen = hoja(libro, "Resumen", cabecera, "Concepto", "Valor");
      texto(resumen, "Período", vista.periodo().etiqueta());
      texto(resumen, "Desde", vista.periodo().desde()); texto(resumen, "Hasta", vista.periodo().hasta());
      texto(resumen, "Paciente / DNI / historia", vista.busqueda().isBlank() ? "Todos" : vista.busqueda());
      dato(resumen, "Total cobrado", vista.resumen().total(), moneda);
      dato(resumen, "Cantidad de pagos", BigDecimal.valueOf(vista.resumen().pagos()), null);
      dato(resumen, "Pacientes con pagos", BigDecimal.valueOf(vista.resumen().pacientes()), null);
      dato(resumen, "Promedio por pago", vista.resumen().promedio(), moneda);
      dato(resumen, "Con captura de comprobante", BigDecimal.valueOf(vista.resumen().conComprobante()), null);
      dato(resumen, "Sin captura de comprobante", BigDecimal.valueOf(vista.resumen().sinComprobante()), null);
      texto(resumen, "Período anterior", vista.periodo().anteriorDesde() + " al " + vista.periodo().anteriorHasta());
      dato(resumen, "Cobrado en período anterior", vista.resumen().totalAnterior(), moneda);
      dato(resumen, "Diferencia", vista.resumen().diferencia(), moneda);
      texto(resumen, "Variación porcentual", vista.resumen().variacionPorcentual() == null ?
          "Sin base porcentual (período anterior sin ingresos)" : vista.resumen().variacionPorcentual().toPlainString() + " %");
      texto(resumen, "Generado", vista.actualizado());
      texto(resumen, "Criterio", "Importes de pagos guardados, agrupados por fecha de pago.");
      resumen.setColumnWidth(0, 34 * 256); resumen.setColumnWidth(1, 72 * 256);

      var evolucion = hoja(libro, "Evolución", cabecera, "Período", "Cobrado (S/)", "Pagos");
      for (var punto : vista.serie()) {
        var fila = evolucion.createRow(evolucion.getLastRowNum() + 1);
        fila.createCell(0).setCellValue(punto.etiqueta());
        numero(fila, 1, punto.total(), moneda); fila.createCell(2).setCellValue(punto.pagos());
      }
      evolucion.setColumnWidth(0, 22 * 256); evolucion.setColumnWidth(1, 24 * 256); evolucion.setColumnWidth(2, 14 * 256);
      filtrar(evolucion, 3);

      var detalle = hoja(libro, "Detalle de pagos", cabecera,
          "ID pago", "Fecha de pago", "Historia", "Paciente", "DNI", "Importe (S/)", "Firmante", "Captura de comprobante");
      int pagina = 0, filaNumero = 1;
      IngresosVista.Detalle lote;
      do {
        lote = ingresos.detalle(consulta, pagina++, 500);
        for (var pago : lote.pagos()) {
          var fila = detalle.createRow(filaNumero++);
          fila.createCell(0).setCellValue(pago.id());
          fila.createCell(1).setCellValue(ClinicalDates.parse(pago.fecha(), "Fecha de pago"));
          fila.getCell(1).setCellStyle(fecha);
          fila.createCell(2).setCellValue(pago.historia());
          // Literal text prevents patient names from becoming spreadsheet formulas.
          fila.createCell(3).setCellValue(pago.paciente()); fila.createCell(4).setCellValue(pago.dni());
          numero(fila, 5, pago.importe(), moneda);
          fila.createCell(6).setCellValue(pago.firmante());
          fila.createCell(7).setCellValue(pago.tieneComprobante() ? "Adjunta" : "Sin captura");
        }
      } while (pagina < lote.totalPaginas());
      int[] anchos = {14, 20, 20, 42, 16, 24, 30, 26};
      for (int i = 0; i < anchos.length; i++) detalle.setColumnWidth(i, anchos[i] * 256);
      filtrar(detalle, anchos.length);
      libro.write(salida);
      return salida.toByteArray();
    } finally {
      libro.dispose();
      libro.close();
    }
  }

  private Sheet hoja(Workbook libro, String nombre, CellStyle estilo, String... columnas) {
    var hoja = libro.createSheet(nombre); var fila = hoja.createRow(0);
    fila.setHeightInPoints(30);
    for (int i = 0; i < columnas.length; i++) {
      var celda = fila.createCell(i); celda.setCellValue(columnas[i]); celda.setCellStyle(estilo);
    }
    hoja.createFreezePane(0, 1); return hoja;
  }
  private void texto(Sheet hoja, String concepto, String valor) {
    var fila = hoja.createRow(hoja.getLastRowNum() + 1);
    fila.createCell(0).setCellValue(concepto); fila.createCell(1).setCellValue(valor);
  }
  private void dato(Sheet hoja, String concepto, BigDecimal valor, CellStyle estilo) {
    var fila = hoja.createRow(hoja.getLastRowNum() + 1); fila.createCell(0).setCellValue(concepto);
    numero(fila, 1, valor, estilo);
  }
  private void numero(Row fila, int columna, BigDecimal valor, CellStyle estilo) {
    var celda = fila.createCell(columna); celda.setCellValue(valor.doubleValue());
    if (estilo != null) celda.setCellStyle(estilo);
  }
  private void filtrar(Sheet hoja, int columnas) {
    if (hoja.getLastRowNum() > 0) hoja.setAutoFilter(new CellRangeAddress(0, hoja.getLastRowNum(), 0, columnas - 1));
  }
}
