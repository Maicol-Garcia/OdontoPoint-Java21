package pe.odontopoint.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.odontopoint.entity.ServicioCatalogo;

public interface ServicioCatalogoRepository extends JpaRepository<ServicioCatalogo, Long> {
  List<ServicioCatalogo> findAllByOrderByNombreAsc();
}
