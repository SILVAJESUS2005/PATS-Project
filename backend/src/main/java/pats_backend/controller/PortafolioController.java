package pats_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import pats_backend.model.Clase;
import pats_backend.model.Portafolio;
import pats_backend.model.Usuario;
import pats_backend.dto.CrearPortafolioDTO;
import pats_backend.repository.ClaseRepository;
import pats_backend.repository.PortafolioRepository;
import pats_backend.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/portafolios")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PortafolioController {

    private final PortafolioRepository portafolioRepository;
    private final ClaseRepository claseRepository;
    private final UsuarioRepository usuarioRepository;

    public PortafolioController(PortafolioRepository portafolioRepository, 
                                 ClaseRepository claseRepository, 
                                 UsuarioRepository usuarioRepository) {
        this.portafolioRepository = portafolioRepository;
        this.claseRepository = claseRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearAsignacion(@Valid @RequestBody CrearPortafolioDTO crearPortafolioDTO, 
                                             BindingResult result, 
                                             HttpSession session) {
        
        // 1. Validar errores de validación de entrada
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errores);
        }

        // 2. Obtener el usuario autenticado de la sesión
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión para realizar esta acción");
        }

        // Obtener el docente fresco de la base de datos de Azure
        Usuario docente = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        if (docente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El usuario docente no existe en el sistema");
        }

        // 3. Buscar la clase asociada en Azure
        Clase clase = claseRepository.findById(crearPortafolioDTO.getClaseId()).orElse(null);
        if (clase == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "La clase especificada no existe");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // 4. Validar Seguridad: Solo el docente que CREÓ la clase puede crear tareas en ella
        if (!clase.getDocente().getId().equals(docente.getId())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Acceso denegado: Solo el docente creador de esta clase puede añadir tareas");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        // 5. Crear e inicializar la asignación de Portafolio
        Portafolio portafolio = new Portafolio();
        portafolio.setTitulo(crearPortafolioDTO.getTitulo());
        portafolio.setDescripcion(crearPortafolioDTO.getDescripcion());
        portafolio.setFechaLimite(crearPortafolioDTO.getFechaLimite());
        portafolio.setClase(clase);

        // Guardar la tarea en la base de datos en Azure MySQL
        Portafolio portafolioGuardado = portafolioRepository.save(portafolio);

        // 6. Respuesta limpia y estructurada
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Asignación de portafolio creada con éxito");
        respuesta.put("id", portafolioGuardado.getId());
        respuesta.put("titulo", portafolioGuardado.getTitulo());
        respuesta.put("descripcion", portafolioGuardado.getDescripcion());
        respuesta.put("fechaCreacion", portafolioGuardado.getFechaCreacion());
        respuesta.put("fechaLimite", portafolioGuardado.getFechaLimite());
        respuesta.put("claseId", clase.getId());
        respuesta.put("nombreClase", clase.getNombre());

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
