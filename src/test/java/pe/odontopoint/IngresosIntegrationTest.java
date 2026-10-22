package pe.odontopoint;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.odontopoint.dto.*;
import pe.odontopoint.entity.*;
import pe.odontopoint.exception.BusinessException;
import pe.odontopoint.repository.*;
import pe.odontopoint.service.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:ingresostests;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("h2")
@AutoConfigureMockMvc
class IngresosIntegrationTest {
  @Autowired IngresosService ingresos;
  @Autowired IngresosExcelService excel;
  @Autowired HistoryService historias;
  @Autowired PaymentRepository pagos;
  @Autowired PaymentReceiptRepository comprobantes;
  @Autowired PatientHistoryRepository pacientes;
  @Autowired PaymentReceiptService capturas;
  @Autowired MockMvc mvc;
  Long ana, bruno, pagoEnero;

  @BeforeEach
  void preparar() throws Exception {
    comprobantes.deleteAll(); pagos.deleteAll(); pacientes.deleteAll();
    ana = paciente("Ana Rivera", "70000001");
    bruno = paciente("Bruno 100%_Demo", "70000002");
    pago(ana, "2023-12-31", "50.00");
    pagoEnero = pago(ana, "2024-01-01", "100.10");
    pago(ana, "2024-01-31", "0.20");
    pago(ana, "2024-02-29", "25.30");
    pago(bruno, "2024-03-31", "200.40");
    pago(bruno, "2024-04-01", "80.00");
    pago(bruno, "2025-01-01", "900.00");
    var bytes = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
    capturas.upload(pagoEnero, new MockMultipartFile("file", "captura.png", "image/png", bytes.toByteArray()), "prueba");
  }

  Long paciente(String nombre, String dni) {
    var f = new HistoryRequest();
    f.setFullName(nombre); f.setDni(dni); f.setSex("Femenino");
    f.setBirthDate("12/04/1990"); f.setConsentStatus("Pendiente"); f.setReason("Prueba de ingresos");
    f.setTreatments(List.of(new HistoryRequest.TreatmentInput("Presupuesto", new BigDecimal("5000.00"))));
    return historias.save(null, f, "prueba").id();
  }

  Long pago(Long historia, String fecha, String importe) {
    var p = new Payment(); p.setHistory(pacientes.findById(historia).orElseThrow());
    p.setPaymentDate(LocalDate.parse(fecha)); p.setAmount(new BigDecimal(importe)); p.setSigner("Prueba");
    return pagos.saveAndFlush(p).getId();
  }

  ConsultaIngresos filtro(String tipo) {
    var f = new ConsultaIngresos(); f.setTipo(tipo); f.setAnio(2024); f.setMes(1); f.setTrimestre(1);
    return f;
  }
  void importe(String esperado, BigDecimal actual) {
    assertEquals(0, new BigDecimal(esperado).compareTo(actual), "Importe exacto esperado: " + esperado);
  }

  @Test
  void mesIncluyeAmbosLimitesYUtilizaLaFechaDelPago() {
    var v = ingresos.consultar(filtro("MES"));
    importe("100.30", v.resumen().total());
    assertEquals(2, v.resumen().pagos()); assertEquals(1, v.resumen().pacientes());
    importe("50.15", v.resumen().promedio());
    importe("50.00", v.resumen().totalAnterior());
    importe("100.60", v.resumen().variacionPorcentual());
    assertEquals(1, v.resumen().conComprobante()); assertEquals(1, v.resumen().sinComprobante());
    assertEquals("01/01/2024", v.periodo().desde()); assertEquals("31/01/2024", v.periodo().hasta());
    assertEquals(31, v.serie().size());
    importe("100.30", v.serie().stream().map(IngresosVista.Punto::total).reduce(BigDecimal.ZERO, BigDecimal::add));
    assertEquals("31/01/2024", v.detalle().pagos().getFirst().fecha());
    // The treatment budgets do not become revenue until a payment is recorded.
    assertTrue(v.resumen().total().compareTo(new BigDecimal("10000")) < 0);
  }

  @Test
  void trimestreYAnioRespetanCambiosDePeriodoYAnioBisiesto() {
    var trimestre = ingresos.consultar(filtro("TRIMESTRE"));
    importe("326.00", trimestre.resumen().total());
    assertEquals(4, trimestre.resumen().pagos()); assertEquals(2, trimestre.resumen().pacientes());
    assertEquals(3, trimestre.serie().size());
    assertEquals("01/10/2023", trimestre.periodo().anteriorDesde());
    assertEquals("31/12/2023", trimestre.periodo().anteriorHasta());
    var anual = ingresos.consultar(filtro("ANIO"));
    importe("406.00", anual.resumen().total()); assertEquals(12, anual.serie().size());
    importe("25.30", anual.serie().get(1).total());
    assertEquals(5, anual.resumen().pagos()); importe("50.00", anual.resumen().totalAnterior());
    var f = filtro("MES"); f.setMes(2);
    assertEquals(29, ingresos.consultar(f).serie().size());
  }

  @Test
  void periodosVaciosYBaseCeroNoProducenPorcentajesInfinitos() {
    var f = filtro("MES"); f.setAnio(2020); f.setMes(2);
    var v = ingresos.consultar(f);
    importe("0.00", v.resumen().total()); assertEquals(0, v.resumen().pagos());
    assertEquals(0, v.resumen().pacientes()); importe("0.00", v.resumen().promedio());
    assertNull(v.resumen().variacionPorcentual()); assertEquals(29, v.serie().size());
    assertTrue(v.serie().stream().allMatch(p -> p.total().signum() == 0));
    f.setAnio(2023); f.setMes(12);
    assertNull(ingresos.consultar(f).resumen().variacionPorcentual());
  }

  @Test
  void rangoPersonalizadoYValidacionEstrica() {
    var f = filtro("PERSONALIZADO"); f.setDesde("31/01/2024"); f.setHasta("29/02/2024");
    var v = ingresos.consultar(f); importe("25.50", v.resumen().total());
    assertEquals(30, v.serie().size());
    assertEquals("01/01/2024", v.periodo().anteriorDesde());
    assertEquals("30/01/2024", v.periodo().anteriorHasta());
    f.setDesde("31/02/2024");
    assertThrows(BusinessException.class, () -> ingresos.consultar(f));
    f.setDesde("01/03/2024");
    assertThrows(BusinessException.class, () -> ingresos.consultar(f));
    f.setDesde("01/01/2023"); f.setHasta("31/12/2024");
    assertThrows(BusinessException.class, () -> ingresos.consultar(f));
    var invalido = filtro("TRIMESTRE"); invalido.setTrimestre(5);
    assertThrows(BusinessException.class, () -> ingresos.consultar(invalido));
  }

  @Test
  void busquedaAplicaALosTotalesSerieYDetalleConComodinesLiterales() {
    var f = filtro("ANIO"); f.setQ("aNa");
    var v = ingresos.consultar(f); importe("125.60", v.resumen().total());
    assertEquals(3, v.detalle().totalRegistros());
    f.setQ("70000002"); importe("280.40", ingresos.consultar(f).resumen().total());
    f.setQ(String.format("HC-%06d", ana)); importe("125.60", ingresos.consultar(f).resumen().total());
    f.setQ("%"); importe("280.40", ingresos.consultar(f).resumen().total());
    f.setQ("_Demo"); importe("280.40", ingresos.consultar(f).resumen().total());
    f.setQ("%_inexistente"); importe("0.00", ingresos.consultar(f).resumen().total());
  }

  @Test
  void paginacionEsEstableYExcelIncluyeTodosLosPagosFiltrados() throws Exception {
    var f = filtro("ANIO"); f.setTamano(2); f.setPagina(1);
    var v = ingresos.consultar(f);
    assertEquals(5, v.detalle().totalRegistros()); assertEquals(3, v.detalle().totalPaginas());
    assertEquals(2, v.detalle().pagos().size()); importe("406.00", v.resumen().total());
    Set<Long> ids = new HashSet<>();
    for (int i = 0; i < 3; i++) {
      f.setPagina(i); ingresos.consultar(f).detalle().pagos().forEach(p -> assertTrue(ids.add(p.id())));
    }
    assertEquals(5, ids.size());
    try (var libro = new XSSFWorkbook(new ByteArrayInputStream(excel.exportar(f)))) {
      assertEquals(3, libro.getNumberOfSheets());
      assertEquals(5, libro.getSheet("Detalle de pagos").getLastRowNum());
      assertEquals(12, libro.getSheet("Evolución").getLastRowNum());
      var fecha = libro.getSheet("Detalle de pagos").getRow(1).getCell(1);
      assertEquals(CellType.NUMERIC, fecha.getCellType());
      assertEquals("dd/mm/yyyy", fecha.getCellStyle().getDataFormatString());
    }
  }

  @Test
  void exportacionGuardaTextoComoLiteralYReflejaCambiosDePagos() throws Exception {
    var id = paciente("=1+1", "70000003"); var pago = pago(id, "2024-06-01", "0.10");
    var f = filtro("ANIO"); f.setQ("70000003");
    importe("0.10", ingresos.consultar(f).resumen().total());
    try (var libro = new XSSFWorkbook(new ByteArrayInputStream(excel.exportar(f)))) {
      var nombre = libro.getSheet("Detalle de pagos").getRow(1).getCell(3);
      assertEquals(CellType.STRING, nombre.getCellType()); assertEquals("=1+1", nombre.getStringCellValue());
    }
    var entidad = pagos.findById(pago).orElseThrow(); entidad.setAmount(new BigDecimal("0.30"));
    pagos.saveAndFlush(entidad); importe("0.30", ingresos.consultar(f).resumen().total());
    pagos.deleteById(pago); importe("0.00", ingresos.consultar(f).resumen().total());
  }

  @Test
  void rutasExigenSesionYRespondenConFiltrosValidosYErroneos() throws Exception {
    mvc.perform(get("/api/ingresos")).andExpect(status().isUnauthorized());
    mvc.perform(get("/exports/ingresos.xlsx")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/ingresos").with(user("admin"))).andExpect(status().isOk());
    mvc.perform(get("/api/ingresos").with(user("admin"))
        .param("tipo", "MES").param("anio", "2024").param("mes", "1"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.resumen.total").value(100.30))
        .andExpect(jsonPath("$.resumen.conComprobante").value(1));
    mvc.perform(get("/api/ingresos").with(user("admin")).param("tipo", "OTRO"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/ingresos").with(user("admin")).param("anio", "texto"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/ingresos").with(user("admin")).param("pagina", "-1"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/ingresos").with(user("admin")).param("tamano", "1000"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/exports/ingresos.xlsx").with(user("admin"))
        .param("tipo", "ANIO").param("anio", "2024"))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
  }
}
