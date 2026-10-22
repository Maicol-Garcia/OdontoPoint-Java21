package pe.odontopoint.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.odontopoint.entity.PatientHistory;

public interface PatientHistoryRepository extends JpaRepository<PatientHistory, Long> {
  @Query(
      "select h from PatientHistory h where lower(h.fullName) like lower(concat('%',:q,'%')) or"
          + " h.dni like concat('%',:q,'%') or cast(h.id as string) like concat('%',:q,'%')")
  Page<PatientHistory> search(@Param("q") String q, Pageable pageable);

  boolean existsByDniAndIdNot(String dni, Long id);

  long countByCreatedAtGreaterThanEqual(java.time.LocalDateTime date);

  @Query("select coalesce(max(h.id), 0) from PatientHistory h")
  Long maxId();
}
