package pe.odontopoint.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.odontopoint.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {
  java.util.Optional<AppUser> findByUsername(String username);
}
