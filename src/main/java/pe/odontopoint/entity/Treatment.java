package pe.odontopoint.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tratamientos")
public class Treatment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_historia", nullable = false)
  private PatientHistory history;

  @Column(name = "descripcion", nullable = false, length = 1000)
  private String description;

  @Column(name = "importe", nullable = false, precision = 12, scale = 2)
  private java.math.BigDecimal amount;

  public Long getId() {
    return this.id;
  }

  public PatientHistory getHistory() {
    return this.history;
  }

  public String getDescription() {
    return this.description;
  }

  public java.math.BigDecimal getAmount() {
    return this.amount;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public void setHistory(final PatientHistory history) {
    this.history = history;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setAmount(final java.math.BigDecimal amount) {
    this.amount = amount;
  }
}
