package pe.odontopoint.service;

import java.io.*;
import java.security.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.odontopoint.dto.ClinicalEvidenceView;
import pe.odontopoint.entity.ClinicalEvidence;
import pe.odontopoint.exception.*;
import pe.odontopoint.repository.*;
import pe.odontopoint.util.ClinicalDates;

@Service
@Transactional(readOnly = true)
public class ClinicalEvidenceService {
  private static final Set<String> TYPES = Set.of("TRATAMIENTO", "OTRO");
  private final ClinicalEvidenceRepository evidences;
  private final PatientHistoryRepository histories;
  private final AuditService audit;

  public List<ClinicalEvidenceView> list(Long history, String type) {
    histories.findById(history).orElseThrow(NotFoundException::new);
    // Sin filtro mostramos TODAS las evidencias ya guardadas, incluidas las antiguas
    // de tipo PAGO creadas por versiones anteriores. La carga nueva sigue limitada
    // a TRATAMIENTO y OTRO, por lo que los comprobantes actuales se gestionan en Pagos.
    var list =
        type == null || type.isBlank()
            ? evidences.findByHistoryIdOrderByUploadedAtDesc(history)
            : evidences.findByHistoryIdAndTypeOrderByUploadedAtDesc(history, normalizeType(type));
    return list.stream().map(this::view).toList();
  }

  public ClinicalEvidence find(Long id) {
    var evidence = evidences.findById(id).orElseThrow(NotFoundException::new);
    evidence.getContent();
    return evidence;
  }

  @Transactional
  public ClinicalEvidenceView upload(
      Long history, String type, String title, String description, MultipartFile file, String actor)
      throws IOException {
    var h = histories.findById(history).orElseThrow(NotFoundException::new);
    String normalizedType = normalizeType(type);
    String cleanTitle = title == null ? "" : title.trim();
    String cleanDescription = description == null ? "" : description.trim();
    if (cleanTitle.isBlank() || cleanTitle.length() > 180)
      throw new BusinessException("Ingrese un título de hasta 180 caracteres.");
    if (cleanDescription.length() > 500)
      throw new BusinessException("La descripción admite hasta 500 caracteres.");
    if (file.isEmpty() || file.getSize() > 8 * 1024 * 1024)
      throw new BusinessException("Seleccione una imagen PNG, JPG, JPEG o JFIF de hasta 8 MB.");
    byte[] bytes = file.getBytes();
    String format = validateImage(bytes);
    var evidence = new ClinicalEvidence();
    evidence.setHistory(h);
    evidence.setType(normalizedType);
    evidence.setTitle(cleanTitle);
    evidence.setDescription(cleanDescription);
    String name =
        file.getOriginalFilename() == null ? "evidencia-clinica" : file.getOriginalFilename();
    name = name.replaceAll("[\\\\/\\r\\n]", "_");
    evidence.setOriginalName(name.substring(0, Math.min(200, name.length())));
    evidence.setMediaType(format.equals("png") ? "image/png" : "image/jpeg");
    evidence.setContent(bytes);
    evidence.setUploadedAt(ClinicalDates.now());
    evidence.setUploadedBy(actor);
    evidence.setSha256(sha256(bytes));
    evidences.saveAndFlush(evidence);
    audit.record(history, "EVIDENCIA " + normalizedType + " ADJUNTADA", actor);
    return view(evidence);
  }

  @Transactional
  public void delete(Long id, String actor) {
    var evidence = evidences.findById(id).orElseThrow(NotFoundException::new);
    Long historyId = evidence.getHistory().getId();
    String type = evidence.getType();
    evidences.delete(evidence);
    audit.record(historyId, "EVIDENCIA " + type + " ELIMINADA", actor);
  }

  private String normalizeType(String type) {
    String value = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    if (!TYPES.contains(value)) throw new BusinessException("Tipo de evidencia no válido.");
    return value;
  }

  private String validateImage(byte[] bytes) {
    try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
      var readers = ImageIO.getImageReaders(stream);
      if (!readers.hasNext()) throw new BusinessException("El archivo no es una imagen válida.");
      var reader = readers.next();
      try {
        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
        reader.setInput(stream);
        long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
        if (!Set.of("png", "jpeg", "jpg").contains(format) || pixels > 20000000)
          throw new BusinessException("Use PNG, JPG, JPEG o JFIF de hasta 20 megapíxeles.");
        if (reader.read(0) == null) throw new BusinessException("Imagen dañada.");
        return format;
      } finally {
        reader.dispose();
      }
    } catch (IOException ex) {
      throw new BusinessException("La imagen está dañada o incompleta.");
    }
  }

  private String sha256(byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private ClinicalEvidenceView view(ClinicalEvidence e) {
    return new ClinicalEvidenceView(
        e.getId(),
        e.getType(),
        e.getTitle(),
        e.getDescription(),
        e.getOriginalName(),
        ClinicalDates.format(e.getUploadedAt()),
        e.getUploadedBy(),
        e.getSha256());
  }

  public ClinicalEvidenceService(
      final ClinicalEvidenceRepository evidences,
      final PatientHistoryRepository histories,
      final AuditService audit) {
    this.evidences = evidences;
    this.histories = histories;
    this.audit = audit;
  }
}
