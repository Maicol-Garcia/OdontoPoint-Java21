package pe.odontopoint.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "catalogo_servicios")
public class ServicioCatalogo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private long version;

  @Column(nullable = false, length = 120)
  private String nombre;

  @Column(length = 1000)
  private String descripcion;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal precio;

  @Column(name = "duracion_minutos", nullable = false)
  private int duracionMinutos;

  @Column(nullable = false)
  private boolean activo;

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

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String valor) {
    this.nombre = valor;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String valor) {
    this.descripcion = valor;
  }

  public BigDecimal getPrecio() {
    return precio;
  }

  public void setPrecio(BigDecimal valor) {
    this.precio = valor;
  }

  public int getDuracionMinutos() {
    return duracionMinutos;
  }

  public void setDuracionMinutos(int valor) {
    this.duracionMinutos = valor;
  }

  public boolean getActivo() {
    return activo;
  }

  public void setActivo(boolean valor) {
    this.activo = valor;
  }
}
