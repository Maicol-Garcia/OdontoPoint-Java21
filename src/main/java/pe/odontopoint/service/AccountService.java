package pe.odontopoint.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.odontopoint.dto.SetupRequest;
import pe.odontopoint.entity.AppUser;
import pe.odontopoint.exception.BusinessException;
import pe.odontopoint.repository.*;

@Service
public class AccountService {
  private final UserRepository users;
  private final SequenceCounterRepository counters;
  private final PasswordEncoder encoder;
  private final AuditService audit;

  public boolean needsSetup() {
    return users.count() == 0;
  }

  @Transactional
  public void setup(SetupRequest form) {
    counters.locked("configuracion");
    if (!needsSetup()) throw new BusinessException("El administrador ya fue configurado.");
    if (!form.getPassword().equals(form.getConfirmPassword()))
      throw new BusinessException("Las contraseñas no coinciden.");
    var u = new AppUser();
    u.setDisplayName(form.getDisplayName().trim());
    u.setUsername(form.getUsername().toLowerCase(java.util.Locale.ROOT));
    u.setPasswordHash(encoder.encode(form.getPassword()));
    users.saveAndFlush(u);
    audit.record(null, "ADMINISTRADOR CREADO", u.getUsername());
  }

  @Transactional
  public void changePassword(String username, String current, String next, String confirm) {
    var u = users.findByUsername(username).orElseThrow();
    if (!encoder.matches(current, u.getPasswordHash()))
      throw new BusinessException("La contraseña actual es incorrecta.");
    if (next.length() < 10 || next.length() > 72 || !next.equals(confirm))
      throw new BusinessException(
          "La nueva contraseña debe tener entre 10 y 72 caracteres y coincidir con la"
              + " confirmación.");
    u.setPasswordHash(encoder.encode(next));
    audit.record(null, "CONTRASEÑA ACTUALIZADA", username);
  }

  public AccountService(
      final UserRepository users,
      final SequenceCounterRepository counters,
      final PasswordEncoder encoder,
      final AuditService audit) {
    this.users = users;
    this.counters = counters;
    this.encoder = encoder;
    this.audit = audit;
  }
}
