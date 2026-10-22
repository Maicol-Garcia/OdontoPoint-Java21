package pe.odontopoint.service;

import java.math.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import pe.odontopoint.dto.*;
import pe.odontopoint.exception.BusinessException;
import pe.odontopoint.repository.*;
import pe.odontopoint.util.ClinicalDates;

/** Dinero efectivamente registrado en pagos, agrupado por fecha de pago. */
@Service
@Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
public class IngresosService {
  private static final Locale ESPANOL = Locale.forLanguageTag("es-PE");
  private static final BigDecimal CERO = new BigDecimal("0.00");
  private final PaymentRepository pagos;

  public IngresosService(PaymentRepository pagos) { this.pagos = pagos; }

  public record Consulta(String tipo, LocalDate desde, LocalDate hasta,
      LocalDate anteriorDesde, LocalDate anteriorHasta, String etiqueta,
      boolean mensual, String texto, String patron, Long historia) {
    public boolean sinBusqueda() { return texto.isEmpty(); }
  }

  public Consulta resolver(ConsultaIngresos filtro) {
    LocalDate hoy = ClinicalDates.today();
    String tipo = Objects.requireNonNullElse(filtro.getTipo(), "MES").toUpperCase(Locale.ROOT);
    int anio = filtro.getAnio() == null ? hoy.getYear() : filtro.getAnio();
    if (anio < 1900 || anio > 2100) throw new BusinessException("El año debe estar entre 1900 y 2100.");
    LocalDate desde, hasta, anteriorDesde, anteriorHasta;
    String etiqueta;
    boolean mensual;
    switch (tipo) {
      case "MES" -> {
        int mes = filtro.getMes() == null ? hoy.getMonthValue() : filtro.getMes();
        if (mes < 1 || mes > 12) throw new BusinessException("Seleccione un mes válido.");
        desde = LocalDate.of(anio, mes, 1); hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        anteriorDesde = desde.minusMonths(1); anteriorHasta = desde.minusDays(1);
        etiqueta = desde.format(DateTimeFormatter.ofPattern("MMMM 'de' uuuu", ESPANOL));
        mensual = false;
      }
      case "TRIMESTRE" -> {
        int trimestre = filtro.getTrimestre() == null ? (hoy.getMonthValue() - 1) / 3 + 1 : filtro.getTrimestre();
        if (trimestre < 1 || trimestre > 4) throw new BusinessException("Seleccione un trimestre válido.");
        desde = LocalDate.of(anio, (trimestre - 1) * 3 + 1, 1);
        hasta = desde.plusMonths(3).minusDays(1);
        anteriorDesde = desde.minusMonths(3); anteriorHasta = desde.minusDays(1);
        etiqueta = "Trimestre " + trimestre + " de " + anio; mensual = true;
      }
      case "ANIO" -> {
        desde = LocalDate.of(anio, 1, 1); hasta = LocalDate.of(anio, 12, 31);
        anteriorDesde = desde.minusYears(1); anteriorHasta = hasta.minusYears(1);
        etiqueta = "Año " + anio; mensual = true;
      }
      case "PERSONALIZADO" -> {
        desde = ClinicalDates.parse(filtro.getDesde(), "Fecha inicial");
        hasta = ClinicalDates.parse(filtro.getHasta(), "Fecha final");
        long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
        if (dias < 1 || dias > 366 || desde.getYear() < 1900 || hasta.getYear() > 2100)
          throw new BusinessException("Seleccione un rango ordenado de hasta 366 días, entre 1900 y 2100.");
        anteriorHasta = desde.minusDays(1); anteriorDesde = desde.minusDays(dias);
        etiqueta = "Período personalizado"; mensual = dias > 62;
      }
      default -> throw new BusinessException("Tipo de período inválido. Use MES, TRIMESTRE, ANIO o PERSONALIZADO.");
    }
    String texto = Objects.requireNonNullElse(filtro.getQ(), "").strip();
    if (texto.length() > 120) throw new BusinessException("La búsqueda admite hasta 120 caracteres.");
    if (filtro.getPagina() < 0 || filtro.getTamano() < 1 || filtro.getTamano() > 100)
      throw new BusinessException("La página debe ser positiva y su tamaño debe estar entre 1 y 100.");
    String patron = "%" + texto.toLowerCase(Locale.ROOT)
        .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    Long historia = null;
    String numero = texto.toUpperCase(Locale.ROOT).replaceFirst("^HC-?", "");
    if (numero.matches("[0-9]{1,18}")) historia = Long.valueOf(numero);
    return new Consulta(tipo, desde, hasta, anteriorDesde, anteriorHasta, etiqueta, mensual, texto, patron, historia);
  }

  public IngresosVista consultar(ConsultaIngresos filtro) {
    Consulta c = resolver(filtro);
    var actual = total(c, c.desde(), c.hasta());
    var anterior = total(c, c.anteriorDesde(), c.anteriorHasta());
    BigDecimal total = moneda(actual.getTotal()), previo = moneda(anterior.getTotal());
    BigDecimal diferencia = total.subtract(previo);
    BigDecimal porcentaje = previo.signum() == 0 ? null :
        diferencia.multiply(BigDecimal.valueOf(100)).divide(previo, 2, RoundingMode.HALF_UP);
    var resumen = new IngresosVista.Resumen(total, actual.getCantidad(), actual.getPacientes(),
        actual.getCantidad() == 0 ? CERO : total.divide(BigDecimal.valueOf(actual.getCantidad()), 2, RoundingMode.HALF_UP),
        actual.getComprobantes(), actual.getCantidad() - actual.getComprobantes(), previo, diferencia, porcentaje);
    return new IngresosVista(
        new IngresosVista.Periodo(c.tipo(), c.etiqueta(), ClinicalDates.format(c.desde()), ClinicalDates.format(c.hasta()),
            ClinicalDates.format(c.anteriorDesde()), ClinicalDates.format(c.anteriorHasta()), c.mensual() ? "MES" : "DIA"),
        resumen, serie(c), detalle(c, filtro.getPagina(), filtro.getTamano()), c.texto(),
        ClinicalDates.format(ClinicalDates.now()) + " · Lima");
  }

  private IngresoProyecciones.Total total(Consulta c, LocalDate desde, LocalDate hasta) {
    return pagos.resumir(desde, hasta, c.sinBusqueda(), c.patron(), c.historia());
  }

  public IngresosVista.Detalle detalle(Consulta c, int pagina, int tamano) {
    if (pagina < 0 || tamano < 1 || tamano > 500)
      throw new BusinessException("Paginación de pagos inválida.");
    var resultado = pagos.buscarIngresos(c.desde(), c.hasta(), c.sinBusqueda(), c.patron(), c.historia(),
        PageRequest.of(pagina, tamano));
    var filas = resultado.getContent().stream().map(p -> new IngresosVista.Pago(
        p.getId(), p.getHistoriaId(), "HC-" + String.format(Locale.ROOT, "%06d", p.getHistoriaId()),
        p.getPaciente(), p.getDni(), ClinicalDates.format(p.getFecha()), moneda(p.getImporte()),
        Objects.requireNonNullElse(p.getFirmante(), ""), p.getComprobanteId() != null)).toList();
    return new IngresosVista.Detalle(filas, pagina, tamano, resultado.getTotalElements(), resultado.getTotalPages());
  }

  private List<IngresosVista.Punto> serie(Consulta c) {
    var dias = pagos.agruparPorDia(c.desde(), c.hasta(), c.sinBusqueda(), c.patron(), c.historia());
    Map<LocalDate, BigDecimal> importes = new HashMap<>();
    Map<LocalDate, Long> cantidades = new HashMap<>();
    for (var dia : dias) {
      LocalDate clave = c.mensual() ? dia.getFecha().withDayOfMonth(1) : dia.getFecha();
      importes.merge(clave, dia.getTotal(), BigDecimal::add);
      cantidades.merge(clave, dia.getCantidad(), Long::sum);
    }
    List<IngresosVista.Punto> resultado = new ArrayList<>();
    LocalDate primerDia = c.mensual() ? c.desde().withDayOfMonth(1) : c.desde();
    DateTimeFormatter formato = DateTimeFormatter.ofPattern(c.mensual() ? "MMM uuuu" : "dd/MM", ESPANOL);
    for (LocalDate d = primerDia; !d.isAfter(c.hasta()); d = c.mensual() ? d.plusMonths(1) : d.plusDays(1))
      resultado.add(new IngresosVista.Punto(d.toString(), d.format(formato),
          moneda(importes.getOrDefault(d, CERO)), cantidades.getOrDefault(d, 0L)));
    return resultado;
  }

  private BigDecimal moneda(BigDecimal valor) {
    return valor == null ? CERO : valor.setScale(2, RoundingMode.HALF_UP);
  }
}
