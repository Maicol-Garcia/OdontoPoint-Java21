package pe.odontopoint.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.odontopoint.entity.SequenceCounter;

public interface SequenceCounterRepository extends JpaRepository<SequenceCounter, String> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from SequenceCounter c where c.counterKey=:key")
  SequenceCounter locked(@Param("key") String key);
}
