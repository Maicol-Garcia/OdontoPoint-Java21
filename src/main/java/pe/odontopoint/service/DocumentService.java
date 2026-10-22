package pe.odontopoint.service;

import java.io.*;
import java.security.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.odontopoint.dto.DocumentView;
import pe.odontopoint.entity.*;
import pe.odontopoint.exception.*;
import pe.odontopoint.repository.*;
import pe.odontopoint.util.ClinicalDates;

@Service
@Transactional(readOnly = true)
public class DocumentService {
  private final SignedDocumentRepository documents;
  private final PatientHistoryRepository histories;
  private final AuditService audit;

  public List<DocumentView> list(Long history) {
    return documents.findByHistoryIdOrderByUploadedAtDesc(history).stream()
        .map(this::view)
        .toList();
  }

  public SignedDocument find(Long id) {
    var d = documents.findById(id).orElseThrow(NotFoundException::new);
    d.getContent();
    return d;
  }

  @Transactional
  public DocumentView upload(Long history, MultipartFile file, String description, String actor)
      throws IOException {
    var h = histories.findById(history).orElseThrow(NotFoundException::new);
    if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024)
      throw new BusinessException("Seleccione una imagen PNG o JPG de hasta 5 MB.");
    if (description != null && description.length() > 250)
      throw new BusinessException("La descripción admite hasta 250 caracteres.");
    byte[] bytes = file.getBytes();
    String format;
    try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      var readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext()) throw new BusinessException("El archivo no es una imagen válida.");
      var reader = readers.next();
      try {
        format = reader.getFormatName().toLowerCase(Locale.ROOT);
        reader.setInput(stream);
        if (!Set.of("png", "jpeg", "jpg").contains(format)
            || (long) reader.getWidth(0) * reader.getHeight(0) > 16000000)
          throw new BusinessException("Use PNG o JPG de hasta 16 megapíxeles.");
        if (reader.read(0) == null) throw new BusinessException("Imagen dañada.");
      } finally {
        reader.dispose();
      }
    } catch (IOException ex) {
      throw new BusinessException("La imagen está dañada o incompleta.");
    }
    var d = new SignedDocument();
    d.setHistory(h);
    String name =
        file.getOriginalFilename() == null
            ? "documento-firmado"
            : file.getOriginalFilename().replaceAll("[\\\\/\r\n]", "_");
    d.setOriginalName(name.substring(0, Math.min(180, name.length())));
    d.setDescription(description);
    d.setContent(bytes);
    d.setMediaType(format.equals("png") ? "image/png" : "image/jpeg");
    d.setUploadedAt(ClinicalDates.now());
    d.setUploadedBy(actor);
    try {
      d.setSha256(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
    documents.saveAndFlush(d);
    audit.record(history, "DOCUMENTO FIRMADO ADJUNTADO", actor);
    return view(d);
  }

  private DocumentView view(SignedDocument d) {
    return new DocumentView(
        d.getId(),
        d.getOriginalName(),
        d.getDescription(),
        ClinicalDates.format(d.getUploadedAt()),
        d.getUploadedBy(),
        d.getSha256());
  }

  public DocumentService(
      final SignedDocumentRepository documents,
      final PatientHistoryRepository histories,
      final AuditService audit) {
    this.documents = documents;
    this.histories = histories;
    this.audit = audit;
  }
}
