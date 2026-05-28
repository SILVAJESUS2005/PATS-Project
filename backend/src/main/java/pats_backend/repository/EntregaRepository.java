package pats_backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pats_backend.model.Entrega;
import pats_backend.model.Portafolio;
import pats_backend.model.Usuario;

import java.util.Optional;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    boolean existsByPortafolioAndAlumno(Portafolio portafolio, Usuario alumno);
    Optional<Entrega> findByPortafolioAndAlumno(Portafolio portafolio, Usuario alumno);
    List<Entrega> findByPortafolioId(Long portafolioId);
    List<Entrega> findByAlumnoAndPortafolioClaseId(Usuario alumno, Long claseId);
}
