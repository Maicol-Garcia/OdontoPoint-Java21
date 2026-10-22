package pe.odontopoint.dto;

import jakarta.validation.constraints.*;

public class SetupRequest {
  @NotBlank
  @Size(max = 120)
  private String displayName;

  @Pattern(
      regexp = "[a-zA-Z0-9._-]{3,40}",
      message = "Usuario: 3 a 40 letras, números, punto, guion o guion bajo.")
  private String username;

  @NotBlank
  @Size(min = 10, max = 72, message = "La contraseña debe tener entre 10 y 72 caracteres.")
  private String password;

  private String confirmPassword;

  public SetupRequest() {}

  public String getDisplayName() {
    return this.displayName;
  }

  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }

  public String getConfirmPassword() {
    return this.confirmPassword;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public void setConfirmPassword(final String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object o) {
    if (o == this) return true;
    if (!(o instanceof SetupRequest)) return false;
    final SetupRequest other = (SetupRequest) o;
    if (!other.canEqual((java.lang.Object) this)) return false;
    final java.lang.Object this$displayName = this.getDisplayName();
    final java.lang.Object other$displayName = other.getDisplayName();
    if (this$displayName == null
        ? other$displayName != null
        : !this$displayName.equals(other$displayName)) return false;
    final java.lang.Object this$username = this.getUsername();
    final java.lang.Object other$username = other.getUsername();
    if (this$username == null ? other$username != null : !this$username.equals(other$username))
      return false;
    final java.lang.Object this$password = this.getPassword();
    final java.lang.Object other$password = other.getPassword();
    if (this$password == null ? other$password != null : !this$password.equals(other$password))
      return false;
    final java.lang.Object this$confirmPassword = this.getConfirmPassword();
    final java.lang.Object other$confirmPassword = other.getConfirmPassword();
    if (this$confirmPassword == null
        ? other$confirmPassword != null
        : !this$confirmPassword.equals(other$confirmPassword)) return false;
    return true;
  }

  protected boolean canEqual(final java.lang.Object other) {
    return other instanceof SetupRequest;
  }

  @java.lang.Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $displayName = this.getDisplayName();
    result = result * PRIME + ($displayName == null ? 43 : $displayName.hashCode());
    final java.lang.Object $username = this.getUsername();
    result = result * PRIME + ($username == null ? 43 : $username.hashCode());
    final java.lang.Object $password = this.getPassword();
    result = result * PRIME + ($password == null ? 43 : $password.hashCode());
    final java.lang.Object $confirmPassword = this.getConfirmPassword();
    result = result * PRIME + ($confirmPassword == null ? 43 : $confirmPassword.hashCode());
    return result;
  }

  @java.lang.Override
  public java.lang.String toString() {
    return "SetupRequest(displayName="
        + this.getDisplayName()
        + ", username="
        + this.getUsername()
        + ", password="
        + this.getPassword()
        + ", confirmPassword="
        + this.getConfirmPassword()
        + ")";
  }
}
