package pe.odontopoint.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evidencias_clinicas")
public class ClinicalEvidence {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_historia", nullable = false)
  private PatientHistory history;

  @Column(name = "tipo_evidencia", nullable = false, length = 30)
  private String type;

  @Column(name = "titulo", nullable = false, length = 180)
  private String title;

  @Column(name = "descripcion", length = 500)
  private String description;

  @Column(name = "nombre_original", nullable = false, length = 200)
  private String originalName;

  @Column(name = "tipo_archivo", nullable = false, length = 100)
  private String mediaType;

  @Column(name = "huella_sha256", nullable = false, length = 64)
  private String sha256;

  @Column(name = "fecha_carga", nullable = false)
  private java.time.LocalDateTime uploadedAt;

  @Column(name = "cargado_por", nullable = false, length = 255)
  private String uploadedBy;

  @Lob
  @Basic(fetch = FetchType.LAZY)
  @Column(name = "contenido", nullable = false, columnDefinition = "MEDIUMBLOB")
  private byte[] content;

  public Long getId() {
    return this.id;
  }

  public PatientHistory getHistory() {
    return this.history;
  }

  public String getType() {
    return this.type;
  }

  public String getTitle() {
    return this.title;
  }

  public String getDescription() {
    return this.description;
  }

  public String getOriginalName() {
    return this.originalName;
  }

  public String getMediaType() {
    return this.mediaType;
  }

  public String getSha256() {
    return this.sha256;
  }

  public java.time.LocalDateTime getUploadedAt() {
    return this.uploadedAt;
  }

  public String getUploadedBy() {
    return this.uploadedBy;
  }

  public byte[] getContent() {
    return this.content;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public void setHistory(final PatientHistory history) {
    this.history = history;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setOriginalName(final String originalName) {
    this.originalName = originalName;
  }

  public void setMediaType(final String mediaType) {
    this.mediaType = mediaType;
  }

  public void setSha256(final String sha256) {
    this.sha256 = sha256;
  }

  public void setUploadedAt(final java.time.LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public void setUploadedBy(final String uploadedBy) {
    this.uploadedBy = uploadedBy;
  }

  public void setContent(final byte[] content) {
    this.content = content;
  }
}
