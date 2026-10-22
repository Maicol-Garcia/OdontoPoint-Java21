package pe.odontopoint.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.odontopoint.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  String FILTRO = """
       from Payment p join p.history h left join p.receiptFile c
       where p.paymentDate between :desde and :hasta
       and (:sinBusqueda = true or lower(h.fullName) like :busqueda escape '!'
            or h.dni like :busqueda escape '!' or h.id = :historia)
      """;

  @Query("""
      select coalesce(sum(p.amount), 0) as total, count(p.id) as cantidad,
             count(distinct h.id) as pacientes, count(c.id) as comprobantes
      """ + FILTRO)
  IngresoProyecciones.Total resumir(
      @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
      @Param("sinBusqueda") boolean sinBusqueda, @Param("busqueda") String busqueda,
      @Param("historia") Long historia);

  @Query("""
      select p.paymentDate as fecha, sum(p.amount) as total, count(p.id) as cantidad
      """ + FILTRO + " group by p.paymentDate order by p.paymentDate")
  List<IngresoProyecciones.Dia> agruparPorDia(
      @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
      @Param("sinBusqueda") boolean sinBusqueda, @Param("busqueda") String busqueda,
      @Param("historia") Long historia);

  @Query(value = """
      select p.id as id, h.id as historiaId, h.fullName as paciente, h.dni as dni,
             p.paymentDate as fecha, p.amount as importe, p.signer as firmante,
             c.id as comprobanteId
      """ + FILTRO + " order by p.paymentDate desc, p.id desc",
      countQuery = "select count(p.id)" + FILTRO)
  Page<IngresoProyecciones.Pago> buscarIngresos(
      @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
      @Param("sinBusqueda") boolean sinBusqueda, @Param("busqueda") String busqueda,
      @Param("historia") Long historia, Pageable pagina);
}
