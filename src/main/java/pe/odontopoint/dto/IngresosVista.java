package pe.odontopoint.dto;

import java.math.BigDecimal;
import java.util.List;

/** Contrato de salida; nunca expone entidades ni carga imágenes de comprobantes. */
public record IngresosVista(
    Periodo periodo,
    Resumen resumen,
    List<Punto> serie,
    Detalle detalle,
    String busqueda,
    String actualizado) {

  public record Periodo(String tipo, String etiqueta, String desde, String hasta,
      String anteriorDesde, String anteriorHasta, String agrupacion) {}

  public record Resumen(BigDecimal total, long pagos, long pacientes,
      BigDecimal promedio, long conComprobante, long sinComprobante,
      BigDecimal totalAnterior, BigDecimal diferencia, BigDecimal variacionPorcentual) {}

  public record Punto(String clave, String etiqueta, BigDecimal total, long pagos) {}

  public record Pago(Long id, Long historiaId, String historia, String paciente,
      String dni, String fecha, BigDecimal importe, String firmante, boolean tieneComprobante) {}

  public record Detalle(List<Pago> pagos, int pagina, int tamano, long totalRegistros,
      int totalPaginas) {}
}
