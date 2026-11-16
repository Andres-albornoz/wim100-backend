package cl.duoc.wim100.Repository;

import cl.duoc.wim100.Model.Candidata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidataRepository extends JpaRepository<Candidata, Long> {

    List<Candidata> findAllByOrderByFechaRegistroDesc();

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
}
