package pe.odontopoint.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.odontopoint.entity.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
  java.util.List<AuditEvent> findTop25ByHistoryIdOrderByIdDesc(Long historyId);
}
