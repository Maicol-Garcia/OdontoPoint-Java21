package pe.odontopoint.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Proyecciones de consulta: se leen importes y metadatos, nunca el BLOB de la captura. */
public final class IngresoProyecciones {
  private IngresoProyecciones() {}

  public interface Total {
    BigDecimal getTotal();
    long getCantidad();
    long getPacientes();
    long getComprobantes();
  }

  public interface Dia {
    LocalDate getFecha();
    BigDecimal getTotal();
    long getCantidad();
  }

  public interface Pago {
    Long getId();
    Long getHistoriaId();
    String getPaciente();
    String getDni();
    LocalDate getFecha();
    BigDecimal getImporte();
    String getFirmante();
    Long getComprobanteId();
  }
}
