package pe.odontopoint.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class AppUser {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nombre_usuario", nullable = false, unique = true, length = 80)
  private String username;

  @Column(name = "nombre_completo", nullable = false, length = 120)
  private String displayName;

  @Column(name = "contrasena_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "rol", nullable = false, length = 255)
  private String role = "ADMIN";

  public Long getId() {
    return this.id;
  }

  public String getUsername() {
    return this.username;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public String getPasswordHash() {
    return this.passwordHash;
  }

  public String getRole() {
    return this.role;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public void setPasswordHash(final String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public void setRole(final String role) {
    this.role = role;
  }
}
