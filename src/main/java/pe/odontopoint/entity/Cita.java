package pe.odontopoint.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private long version;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "historia_id", nullable = false)
  private PatientHistory historia;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "servicio_id")
  private ServicioCatalogo servicio;

  @Column(nullable = false)
  private LocalDateTime inicio;

  @Column(nullable = false)
  private LocalDateTime fin;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoCita estado;

  @Column(nullable = false, length = 500)
  private String motivo;

  @Column(length = 180)
  private String profesional;

  @Column(length = 1000)
  private String notas;

  public Long getId() {
    return id;
  }

  public void setId(Long valor) {
    this.id = valor;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long valor) {
    this.version = valor;
  }

  public PatientHistory getHistoria() {
    return historia;
  }

  public void setHistoria(PatientHistory valor) {
    this.historia = valor;
  }

  public ServicioCatalogo getServicio() {
    return servicio;
  }

  public void setServicio(ServicioCatalogo valor) {
    this.servicio = valor;
  }

  public LocalDateTime getInicio() {
    return inicio;
  }

  public void setInicio(LocalDateTime valor) {
    this.inicio = valor;
  }

  public LocalDateTime getFin() {
    return fin;
  }

  public void setFin(LocalDateTime valor) {
    this.fin = valor;
  }

  public EstadoCita getEstado() {
    return estado;
  }

  public void setEstado(EstadoCita valor) {
    this.estado = valor;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String valor) {
    this.motivo = valor;
  }

  public String getProfesional() {
    return profesional;
  }

  public void setProfesional(String valor) {
    this.profesional = valor;
  }

  public String getNotas() {
    return notas;
  }

  public void setNotas(String valor) {
    this.notas = valor;
  }
}
