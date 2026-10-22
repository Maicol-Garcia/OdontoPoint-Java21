package pe.odontopoint.controller;

import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.odontopoint.dto.*;
import pe.odontopoint.service.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
  private final HistoryService histories;
  private final AuditService events;

  @GetMapping("/{id}")
  public HistoryView find(@PathVariable Long id) {
    return histories.find(id);
  }

  @PostMapping
  public ResponseEntity<HistoryView> create(
      @Valid @RequestBody HistoryRequest form, java.security.Principal actor) {
    return ResponseEntity.status(201).body(histories.save(null, form, actor.getName()));
  }

  @PutMapping("/{id}")
  public HistoryView update(
      @PathVariable Long id,
      @Valid @RequestBody HistoryRequest form,
      java.security.Principal actor) {
    return histories.save(id, form, actor.getName());
  }

  @GetMapping("/{id}/audit")
  public List<Map<String, Object>> audit(@PathVariable Long id) {
    histories.find(id);
    return events.list(id);
  }

  public PatientController(final HistoryService histories, final AuditService events) {
    this.histories = histories;
    this.events = events;
  }
}
