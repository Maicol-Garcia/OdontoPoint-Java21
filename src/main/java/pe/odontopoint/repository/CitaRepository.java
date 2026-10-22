package pe.odontopoint.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.odontopoint.entity.Cita;

public interface CitaRepository extends JpaRepository<Cita, Long> {
  @Query(
      "select c from Cita c join fetch c.historia left join fetch c.servicio where c.inicio <"
          + " :hasta and c.fin > :desde order by c.inicio")
  List<Cita> intervalo(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

  @Query(
      "select count(c) from Cita c where c.estado <> pe.odontopoint.entity.EstadoCita.CANCELADA and"
          + " c.inicio < :fin and c.fin > :inicio and (:id is null or c.id <> :id)")
  long cruces(
      @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("id") Long id);
}
