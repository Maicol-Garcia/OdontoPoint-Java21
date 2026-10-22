package pe.odontopoint.controller;

import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pe.odontopoint.dto.DocumentView;
import pe.odontopoint.service.DocumentService;

@RestController
public class DocumentController {
  private final DocumentService documents;

  @GetMapping("/api/patients/{id}/documents")
  public List<DocumentView> list(@PathVariable Long id) {
    return documents.list(id);
  }

  @PostMapping("/api/patients/{id}/documents")
  public ResponseEntity<DocumentView> upload(
      @PathVariable Long id,
      @RequestParam("file") MultipartFile file,
      @RequestParam(defaultValue = "") String description,
      java.security.Principal actor)
      throws java.io.IOException {
    return ResponseEntity.status(201)
        .body(documents.upload(id, file, description, actor.getName()));
  }

  @GetMapping("/documents/{id}/content")
  public ResponseEntity<byte[]> content(@PathVariable Long id) {
    var d = documents.find(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(d.getMediaType()))
        .header(
            "Content-Disposition",
            ContentDisposition.inline()
                .filename(d.getOriginalName(), java.nio.charset.StandardCharsets.UTF_8)
                .build()
                .toString())
        .cacheControl(CacheControl.noStore())
        .body(d.getContent());
  }

  public DocumentController(final DocumentService documents) {
    this.documents = documents;
  }
}
