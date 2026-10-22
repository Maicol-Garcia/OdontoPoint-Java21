package pe.odontopoint.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.odontopoint.entity.SignedDocument;

public interface SignedDocumentRepository extends JpaRepository<SignedDocument, Long> {
  java.util.List<SignedDocument> findByHistoryIdOrderByUploadedAtDesc(Long historyId);

  long countByHistoryId(Long historyId);
}
