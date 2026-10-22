package pe.odontopoint.service;

import org.springframework.stereotype.Service;
import pe.odontopoint.entity.AuditEvent;
import pe.odontopoint.repository.AuditEventRepository;
import pe.odontopoint.util.ClinicalDates;

@Service
public class AuditService {
  private final AuditEventRepository repository;

  public java.util.List<java.util.Map<String, Object>> list(Long id) {
    return repository.findTop25ByHistoryIdOrderByIdDesc(id).stream()
        .map(
            a ->
                java.util.Map.<String, Object>of(
                    "action",
                    a.getAction(),
                    "actor",
                    a.getActor(),
                    "date",
                    ClinicalDates.format(a.getOccurredAt())))
        .toList();
  }

  public void record(Long history, String action, String actor) {
    var e = new AuditEvent();
    e.setHistoryId(history);
    e.setAction(action);
    e.setActor(actor);
    e.setOccurredAt(ClinicalDates.now());
    repository.save(e);
  }

  public AuditService(final AuditEventRepository repository) {
    this.repository = repository;
  }
}
