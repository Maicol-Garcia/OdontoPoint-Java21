package pe.odontopoint;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import pe.odontopoint.dto.*;
import pe.odontopoint.mapper.ReportMapper;
import pe.odontopoint.repository.*;
import pe.odontopoint.service.*;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:webtests;DB_CLOSE_DELAY=-1",
      "spring.jpa.hibernate.ddl-auto=create-drop"
    })
@org.springframework.test.context.ActiveProfiles("h2")
@AutoConfigureMockMvc
class WebIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired HistoryService histories;
  @Autowired PatientHistoryRepository repository;
  @Autowired SequenceCounterRepository counters;
  @Autowired SignedDocumentRepository documents;
  @Autowired AuditEventRepository audit;
  @Autowired UserRepository users;
  @Autowired ExcelService excel;
  @Autowired ReportService reports;
  @Autowired ReportMapper mapper;

  @BeforeEach
  void reset() {
    documents.deleteAll();
    repository.deleteAll();
    audit.deleteAll();
    users.deleteAll();
    var c = counters.findById("historia").orElseThrow();
    c.setNextValue(0);
    counters.save(c);
  }

  HistoryRequest sample(String dni) {
    var f = new HistoryRequest();
    f.setFullName("Paciente de prueba");
    f.setDni(dni);
    f.setSex("Femenino");
    f.setBirthDate("12/04/1990");
    f.setReason("Evaluación dental de prueba");
    f.setClinicalExam("Examen de prueba completo");
    f.setConsentStatus("Pendiente");
    f.setBlackLegend("Hallazgo");
    f.setYellowLegend("Pendiente");
    f.setGreenLegend("Realizado");
    return f;
  }

  @Test
  void setupLoginAndLogout() throws Exception {
    mvc.perform(get("/setup"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("Crear cuenta")));
    mvc.perform(
            post("/setup")
                .with(csrf())
                .param("displayName", "Administrador")
                .param("username", "admin")
                .param("password", "PruebaSegura123")
                .param("confirmPassword", "PruebaSegura123"))
        .andExpect(redirectedUrl("/login?created"));
    assertEquals(1, users.count());
    assertNotEquals(
        "PruebaSegura123", users.findByUsername("admin").orElseThrow().getPasswordHash());
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", "admin")
                .param("password", "PruebaSegura123"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/"));
    mvc.perform(
            post("/login").with(csrf()).param("username", "admin").param("password", "incorrecta"))
        .andExpect(redirectedUrl("/login?error"));
    mvc.perform(get("/setup")).andExpect(redirectedUrl("/login"));
    mvc.perform(post("/logout").with(user("admin")).with(csrf()))
        .andExpect(redirectedUrl("/login?logout"));
  }

  @Test
  void unauthenticatedCannotReadData() throws Exception {
    mvc.perform(get("/api/patients/1")).andExpect(status().isUnauthorized());
    mvc.perform(get("/documents/1/content")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/exports/patients.xlsx")).andExpect(status().is3xxRedirection());
    mvc.perform(
            post("/api/patients")
                .with(user("admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsBytes(sample("12345678"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void pagesRender() throws Exception {
    mvc.perform(get("/").with(user("admin")))
        .andExpect(status().isOk())
        .andExpect(
            content().string(org.hamcrest.Matchers.containsString("Directorio de pacientes")));
    mvc.perform(get("/patients/new").with(user("admin")))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("dd/mm/aaaa")));
    mvc.perform(get("/account").with(user("admin"))).andExpect(status().isOk());
  }

  @Test
  void saveAndUpdateDatesAndMarks() throws Exception {
    var f = sample("12345678");
    f.getTeeth().put("16:O", "NEGRO");
    var response =
        mvc.perform(
                post("/api/patients")
                    .with(user("admin"))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsBytes(f)))
            .andExpect(status().isCreated())
            .andReturn();
    var view = json.readValue(response.getResponse().getContentAsByteArray(), HistoryView.class);
    assertEquals("HC-000001", view.number());
    assertTrue(view.createdAt().matches("[0-9]{2}/[0-9]{2}/[0-9]{4} .*"));
    f.setVersion(view.version());
    f.setAddress("Ayacucho");
    var updated = histories.save(view.id(), f, "admin");
    assertEquals(view.createdAt(), updated.createdAt());
    assertEquals("NEGRO", updated.form().getTeeth().get("16:O"));
    assertThrows(Exception.class, () -> histories.save(view.id(), f, "admin"));
    assertEquals(2, audit.findTop25ByHistoryIdOrderByIdDesc(view.id()).size());
  }

  @Test
  void duplicateAndInvalidDoNotConsumeNumbers() {
    histories.save(null, sample("12345678"), "admin");
    assertThrows(Exception.class, () -> histories.save(null, sample("12345678"), "admin"));
    var invalid = sample("12345679");
    invalid.setBirthDate("31/02/2000");
    assertThrows(Exception.class, () -> histories.save(null, invalid, "admin"));
    assertEquals(2, histories.save(null, sample("12345680"), "admin").id());
  }

  @Test
  void validatesMinorAndMoney() {
    var f = sample("12345678");
    f.setBirthDate(
        pe.odontopoint.util.ClinicalDates.format(
            pe.odontopoint.util.ClinicalDates.today().minusYears(10)));
    assertThrows(Exception.class, () -> histories.save(null, f, "admin"));
    f.setGuardianName("Apoderado");
    f.setGuardianDni("87654321");
    f.setGuardianPhone("999999999");
    assertNotNull(histories.save(null, f, "admin"));
    var adult = sample("22345678");
    adult
        .getPayments()
        .add(
            new HistoryRequest.PaymentInput(
                null, "01/01/2020", new BigDecimal("50.00"), "Prueba", "", false));
    assertThrows(Exception.class, () -> histories.save(null, adult, "admin"));
  }

  @Test
  void concurrentHistoryNumbers() throws Exception {
    var pool = Executors.newFixedThreadPool(4);
    try {
      List<Future<HistoryView>> jobs = new ArrayList<>();
      for (int i = 0; i < 8; i++) {
        final int n = i;
        jobs.add(
            pool.submit(
                () -> histories.save(null, sample(String.format("%08d", 10000000 + n)), "admin")));
      }
      var ids = new HashSet<Long>();
      for (var f : jobs) ids.add(f.get(30, TimeUnit.SECONDS).id());
      assertEquals(8, ids.size());
      assertTrue(ids.containsAll(java.util.stream.LongStream.rangeClosed(1, 8).boxed().toList()));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void uploadsRealImageRejectsFakeAndRequiresSession() throws Exception {
    var h = histories.save(null, sample("12345678"), "admin");
    var image =
        new java.awt.image.BufferedImage(100, 80, java.awt.image.BufferedImage.TYPE_INT_RGB);
    var out = new ByteArrayOutputStream();
    javax.imageio.ImageIO.write(image, "png", out);
    var bytes = out.toByteArray();
    mvc.perform(
            multipart("/api/patients/" + h.id() + "/documents")
                .file(new MockMultipartFile("file", "firmado.png", "image/png", bytes))
                .param("description", "Consentimiento firmado")
                .with(user("admin"))
                .with(csrf()))
        .andExpect(status().isCreated());
    var doc = documents.findAll().get(0);
    mvc.perform(get("/documents/" + doc.getId() + "/content").with(user("admin")))
        .andExpect(status().isOk())
        .andExpect(content().bytes(bytes));
    mvc.perform(
            multipart("/api/patients/" + h.id() + "/documents")
                .file(
                    new MockMultipartFile(
                        "file", "falso.png", "image/png", "<script>alert(1)</script>".getBytes()))
                .with(user("admin"))
                .with(csrf()))
        .andExpect(status().isBadRequest());
    assertEquals(1, documents.count());
  }

  @Test
  void excelAndPdfExports() throws Exception {
    var f = sample("12345678");
    f.setFullName("=SUM(1,2)");
    f.getTreatments()
        .add(new HistoryRequest.TreatmentInput("Restauración", new BigDecimal("150.00")));
    f.getPayments()
        .add(
            new HistoryRequest.PaymentInput(
                null, "01/01/2020", new BigDecimal("50.00"), "Paciente", "", false));
    var h = histories.save(null, f, "admin");
    byte[] data = excel.export(List.of(h));
    try (var wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new ByteArrayInputStream(data))) {
      assertEquals(6, wb.getNumberOfSheets());
      var r = wb.getSheet("Pacientes").getRow(1);
      assertEquals(org.apache.poi.ss.usermodel.CellType.STRING, r.getCell(1).getCellType());
      assertEquals("=SUM(1,2)", r.getCell(1).getStringCellValue());
      assertEquals("dd/mm/yyyy", r.getCell(3).getCellStyle().getDataFormatString());
      assertEquals(100, r.getCell(8).getNumericCellValue());
    }
    var pdf = reports.export(mapper.map(h));
    assertEquals("%PDF", new String(pdf, 0, 4));
    var dir = java.nio.file.Path.of("target/qa");
    java.nio.file.Files.createDirectories(dir);
    java.nio.file.Files.write(dir.resolve("historia-web.pdf"), pdf);
    java.nio.file.Files.write(dir.resolve("pacientes.xlsx"), data);
    mvc.perform(get("/exports/patients/" + h.id() + ".xlsx").with(user("admin")))
        .andExpect(status().isOk());
  }
}
