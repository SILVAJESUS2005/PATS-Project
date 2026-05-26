package pats_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pats_backend.model.Entrega;

import pats_backend.model.Portafolio;
import pats_backend.model.Usuario;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    boolean existsByPortafolioAndAlumno(Portafolio portafolio, Usuario alumno);
}
