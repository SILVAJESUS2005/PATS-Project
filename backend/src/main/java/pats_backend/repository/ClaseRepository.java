package pats_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pats_backend.model.Clase;
import java.util.Optional;
import java.util.List;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    
    // Buscar una clase por su código único de acceso
    Optional<Clase> findByCodigoAcceso(String codigoAcceso);
    
    // Buscar todas las clases creadas por un docente específico
    List<Clase> findByDocenteId(Long docenteId);
    
    // Buscar todas las clases en las que está inscrito un alumno específico
    List<Clase> findByAlumnosId(Long alumnoId);
}
