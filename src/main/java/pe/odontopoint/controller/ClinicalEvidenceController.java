package pe.odontopoint.controller;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.odontopoint.dto.ClinicalEvidenceView;
import pe.odontopoint.service.ClinicalEvidenceService;

@RestController
public class ClinicalEvidenceController {
  private final ClinicalEvidenceService evidences;

  @GetMapping("/api/patients/{id}/evidences")
  public List<ClinicalEvidenceView> list(
      @PathVariable Long id, @RequestParam(required = false) String type) {
    return evidences.list(id, type);
  }

  @PostMapping("/api/patients/{id}/evidences")
  public ResponseEntity<ClinicalEvidenceView> upload(
      @PathVariable Long id,
      @RequestParam String type,
      @RequestParam String title,
      @RequestParam(defaultValue = "") String description,
      @RequestParam("file") MultipartFile file,
      java.security.Principal actor)
      throws java.io.IOException {
    return ResponseEntity.status(201)
        .body(evidences.upload(id, type, title, description, file, actor.getName()));
  }

  @DeleteMapping("/api/evidences/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id, java.security.Principal actor) {
    evidences.delete(id, actor.getName());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/evidences/{id}/content")
  public ResponseEntity<byte[]> content(@PathVariable Long id) {
    var evidence = evidences.find(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(evidence.getMediaType()))
        .header(
            "Content-Disposition",
            ContentDisposition.inline()
                .filename(evidence.getOriginalName(), java.nio.charset.StandardCharsets.UTF_8)
                .build()
                .toString())
        .cacheControl(CacheControl.noStore())
        .body(evidence.getContent());
  }

  public ClinicalEvidenceController(final ClinicalEvidenceService evidences) {
    this.evidences = evidences;
  }
}
