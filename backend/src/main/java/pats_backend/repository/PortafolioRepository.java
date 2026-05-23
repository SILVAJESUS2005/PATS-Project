package pats_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pats_backend.model.Portafolio;
import java.util.List;

@Repository
public interface PortafolioRepository extends JpaRepository<Portafolio, Long> {
    
    // Buscar todas las asignaciones de portafolio que pertenecen a una clase específica
    List<Portafolio> findByClaseId(Long claseId);
}
