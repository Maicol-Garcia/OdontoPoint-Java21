package pe.odontopoint.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "eventos_auditoria")
public class AuditEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "id_historia")
  private Long historyId;

  @Column(name = "accion", nullable = false, length = 255)
  private String action;

  @Column(name = "usuario_responsable", nullable = false, length = 255)
  private String actor;

  @Column(name = "fecha_evento", nullable = false)
  private java.time.LocalDateTime occurredAt;

  public Long getId() {
    return this.id;
  }

  public Long getHistoryId() {
    return this.historyId;
  }

  public String getAction() {
    return this.action;
  }

  public String getActor() {
    return this.actor;
  }

  public java.time.LocalDateTime getOccurredAt() {
    return this.occurredAt;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public void setHistoryId(final Long historyId) {
    this.historyId = historyId;
  }

  public void setAction(final String action) {
    this.action = action;
  }

  public void setActor(final String actor) {
    this.actor = actor;
  }

  public void setOccurredAt(final java.time.LocalDateTime occurredAt) {
    this.occurredAt = occurredAt;
  }
}
