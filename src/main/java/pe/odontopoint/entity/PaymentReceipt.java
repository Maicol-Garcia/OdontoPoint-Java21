package pe.odontopoint.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes_pago")
public class PaymentReceipt {
  @Id
  @Column(name = "id_pago")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "id_pago", nullable = false)
  private Payment payment;

  @Column(name = "nombre_original", nullable = false, length = 200)
  private String originalName;

  @Column(name = "tipo_archivo", nullable = false, length = 100)
  private String mediaType;

  @Column(name = "huella_sha256", nullable = false, length = 64)
  private String sha256;

  @Column(name = "fecha_carga", nullable = false)
  private LocalDateTime uploadedAt;

  @Column(name = "cargado_por", nullable = false, length = 255)
  private String uploadedBy;

  @Lob
  @Column(name = "contenido", nullable = false, columnDefinition = "MEDIUMBLOB")
  private byte[] content;

  public Long getId() {
    return this.id;
  }

  public Payment getPayment() {
    return this.payment;
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

  public LocalDateTime getUploadedAt() {
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

  public void setPayment(final Payment payment) {
    this.payment = payment;
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

  public void setUploadedAt(final LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public void setUploadedBy(final String uploadedBy) {
    this.uploadedBy = uploadedBy;
  }

  public void setContent(final byte[] content) {
    this.content = content;
  }
}
