package pe.odontopoint.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.odontopoint.entity.ClinicalEvidence;

public interface ClinicalEvidenceRepository extends JpaRepository<ClinicalEvidence, Long> {
  List<ClinicalEvidence> findByHistoryIdOrderByUploadedAtDesc(Long historyId);

  List<ClinicalEvidence> findByHistoryIdAndTypeInOrderByUploadedAtDesc(
      Long historyId, Collection<String> types);

  List<ClinicalEvidence> findByHistoryIdAndTypeOrderByUploadedAtDesc(Long historyId, String type);
}
