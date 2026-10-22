package pe.odontopoint;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.odontopoint.dto.*;
import pe.odontopoint.entity.*;
import pe.odontopoint.exception.BusinessException;
import pe.odontopoint.repository.*;
import pe.odontopoint.service.*;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:agendatests;DB_CLOSE_DELAY=-1",
      "spring.jpa.hibernate.ddl-auto=create-drop"
    })
@ActiveProfiles("h2")
@AutoConfigureMockMvc
class AgendaIntegrationTest {
  @Autowired AgendaService agenda;
  @Autowired CatalogoService catalogo;
  @Autowired HistoryService historias;
  @Autowired CitaRepository citas;
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  Long paciente;

  @BeforeEach
  void preparar() {
    citas.deleteAll();
    var d = new HistoryRequest();
    d.setFullName("Paciente agenda");
    d.setDni(String.format("%08d", System.nanoTime() % 100000000));
    d.setSex("Femenino");
    d.setBirthDate("12/04/1990");
    d.setReason("Consulta");
    d.setConsentStatus("Pendiente");
    paciente = historias.save(null, d, "prueba").id();
  }

  CitaSolicitud datos(String inicio, String fin, EstadoCita estado, long version, Long servicio) {
    return new CitaSolicitud(
        version,
        paciente,
        servicio,
        LocalDateTime.parse("2026-11-10T" + inicio),
        LocalDateTime.parse("2026-11-10T" + fin),
        estado,
        "Evaluación",
        "Profesional",
        "Notas");
  }

  @Test
  void reservarReprogramarCancelarYEvitarCruces() {
    var c = agenda.guardar(null, datos("09:00", "09:30", EstadoCita.PROGRAMADA, 0, null), "prueba");
    assertThrows(
        BusinessException.class,
        () ->
            agenda.guardar(
                null, datos("09:15", "09:45", EstadoCita.PROGRAMADA, 0, null), "prueba"));
    agenda.guardar(null, datos("09:30", "10:00", EstadoCita.CONFIRMADA, 0, null), "prueba");
    var editada =
        agenda.guardar(
            c.id(), datos("08:00", "08:30", EstadoCita.PROGRAMADA, c.version(), null), "prueba");
    assertThrows(
        org.springframework.dao.OptimisticLockingFailureException.class,
        () ->
            agenda.guardar(
                c.id(), datos("08:00", "08:30", EstadoCita.ATENDIDA, c.version(), null), "prueba"));
    agenda.guardar(
        c.id(), datos("08:00", "08:30", EstadoCita.CANCELADA, editada.version(), null), "prueba");
    agenda.guardar(null, datos("08:00", "08:30", EstadoCita.PROGRAMADA, 0, null), "prueba");
    assertEquals(3, agenda.listar(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 11)).size());
  }

  @Test
  void dosReservasSimultaneasSoloGuardanUna() throws Exception {
    var pool = Executors.newFixedThreadPool(2);
    var salida = new CountDownLatch(1);
    Callable<Boolean> trabajo =
        () -> {
          salida.await();
          try {
            agenda.guardar(null, datos("12:00", "12:30", EstadoCita.PROGRAMADA, 0, null), "prueba");
            return true;
          } catch (BusinessException e) {
            return false;
          }
        };
    try {
      var a = pool.submit(trabajo);
      var b = pool.submit(trabajo);
      salida.countDown();
      assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
      assertEquals(1, citas.count());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void catalogoDesactivadoConservaCitasPeroNoAdmiteNuevas() {
    var s =
        catalogo.guardar(
            null,
            new ServicioSolicitud(0L, "Limpieza", "Servicio", new BigDecimal("80.00"), 30, true),
            "prueba");
    var c =
        agenda.guardar(
            null, datos("10:00", "10:30", EstadoCita.PROGRAMADA, 0, s.getId()), "prueba");
    catalogo.guardar(
        s.getId(),
        new ServicioSolicitud(
            s.getVersion(), s.getNombre(), s.getDescripcion(), s.getPrecio(), 30, false),
        "prueba");
    agenda.guardar(
        c.id(), datos("10:00", "10:30", EstadoCita.ATENDIDA, c.version(), s.getId()), "prueba");
    assertThrows(
        BusinessException.class,
        () ->
            agenda.guardar(
                null, datos("11:00", "11:30", EstadoCita.PROGRAMADA, 0, s.getId()), "prueba"));
  }

  @Test
  void validacionSeguridadYPantallas() throws Exception {
    mvc.perform(get("/api/agenda?desde=2026-11-01&hasta=2026-12-01"))
        .andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/agenda")
                .with(user("admin"))
                .contentType("application/json")
                .content(
                    json.writeValueAsString(
                        datos("09:00", "09:30", EstadoCita.PROGRAMADA, 0, null))))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/agenda")
                .with(user("admin"))
                .with(csrf())
                .contentType("application/json")
                .content(
                    json.writeValueAsString(
                        datos("10:00", "09:30", EstadoCita.PROGRAMADA, 0, null))))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/catalogo")
                .with(user("admin"))
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"version\":0,\"nombre\":\"Prueba\",\"precio\":-1,\"duracionMinutos\":30,\"activo\":true}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/agenda").with(user("admin"))).andExpect(status().isOk());
    mvc.perform(get("/catalogo").with(user("admin"))).andExpect(status().isOk());
  }
}
