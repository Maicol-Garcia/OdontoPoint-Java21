package pe.odontopoint.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "contadores_secuencia")
public class SequenceCounter {
  @Id
  @Column(name = "clave_contador", length = 255)
  private String counterKey;

  @Column(name = "siguiente_valor", nullable = false)
  private long nextValue;

  public String getCounterKey() {
    return this.counterKey;
  }

  public long getNextValue() {
    return this.nextValue;
  }

  public void setCounterKey(final String counterKey) {
    this.counterKey = counterKey;
  }

  public void setNextValue(final long nextValue) {
    this.nextValue = nextValue;
  }
}
