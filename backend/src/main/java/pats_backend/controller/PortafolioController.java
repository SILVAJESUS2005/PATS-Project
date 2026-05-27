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
import pats_backend.repository.EntregaRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portafolios")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PortafolioController {

    private final PortafolioRepository portafolioRepository;
    private final ClaseRepository claseRepository;
    private final UsuarioRepository usuarioRepository;
    private final EntregaRepository entregaRepository;

    public PortafolioController(PortafolioRepository portafolioRepository, 
                                 ClaseRepository claseRepository, 
                                 UsuarioRepository usuarioRepository,
                                 EntregaRepository entregaRepository) {
        this.portafolioRepository = portafolioRepository;
        this.claseRepository = claseRepository;
        this.usuarioRepository = usuarioRepository;
        this.entregaRepository = entregaRepository;
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

    @GetMapping("/clase/{claseId}")
    public ResponseEntity<?> obtenerPortafoliosPorClase(@PathVariable Long claseId, HttpSession session) {
        
        // 1. Validar sesión
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión para realizar esta acción");
        }

        // Obtener el usuario fresco de Azure
        Usuario usuario = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El usuario no existe en el sistema");
        }

        // 2. Buscar la clase en Azure
        Clase clase = claseRepository.findById(claseId).orElse(null);
        if (clase == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "La clase especificada no existe");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // 3. Control de Seguridad:
        // Solo el docente creador o los alumnos matriculados en la clase pueden ver las asignaciones
        boolean esDocenteCreador = clase.getDocente().getId().equals(usuario.getId());
        boolean esAlumnoInscrito = clase.getAlumnos().stream().anyMatch(a -> a.getId().equals(usuario.getId()));

        if (!esDocenteCreador && !esAlumnoInscrito) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Acceso denegado: No perteneces a esta clase");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        // 4. Obtener todos los portafolios asociados de Azure
        List<Portafolio> portafolios = portafolioRepository.findByClaseId(claseId);

        // 5. Mapear a respuesta segura libre de recursión circular JSON
        List<Map<String, Object>> respuesta = new ArrayList<>();
        for (Portafolio p : portafolios) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("titulo", p.getTitulo());
            item.put("descripcion", p.getDescripcion());
            item.put("fechaCreacion", p.getFechaCreacion());
            item.put("fechaLimite", p.getFechaLimite());
            
            // Validar estado de entrega para el alumno
            if ("USER".equals(usuario.getRol())) {
                boolean entregado = entregaRepository.existsByPortafolioAndAlumno(p, usuario);
                item.put("entregado", entregado);
            }
            
            respuesta.add(item);
        }

        return ResponseEntity.ok(respuesta);
    }
}
