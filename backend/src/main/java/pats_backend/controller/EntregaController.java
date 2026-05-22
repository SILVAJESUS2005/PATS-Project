package pats_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import pats_backend.model.Clase;
import pats_backend.model.Entrega;
import pats_backend.model.Usuario;
import pats_backend.dto.CalificarEntregaDTO;
import pats_backend.repository.EntregaRepository;
import pats_backend.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/entregas")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EntregaController {

    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;

    public EntregaController(EntregaRepository entregaRepository, UsuarioRepository usuarioRepository) {
        this.entregaRepository = entregaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PutMapping("/{id}/calificar")
    public ResponseEntity<?> calificarEntrega(@PathVariable Long id, 
                                              @Valid @RequestBody CalificarEntregaDTO dto, 
                                              BindingResult result, 
                                              HttpSession session) {
        
        // 1. Validar errores de entrada de la nota
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errores);
        }

        // 2. Validar sesión activa
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión para realizar esta acción");
        }

        // Obtener el docente fresco de Azure
        Usuario docente = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        if (docente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El usuario docente no existe en el sistema");
        }

        // 3. Buscar la entrega en Azure
        Entrega entrega = entregaRepository.findById(id).orElse(null);
        if (entrega == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "La entrega especificada no existe");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // 4. Control de Seguridad: Solo el docente creador de la clase puede calificar
        Clase clase = entrega.getPortafolio().getClase();
        if (!clase.getDocente().getId().equals(docente.getId())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Acceso denegado: Solo el docente que imparte esta clase puede calificar las tareas");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        // 5. Asignar calificación y comentarios
        entrega.setCalificacion(dto.getCalificacion());
        entrega.setComentarios(dto.getComentarios());

        // Guardar cambios en Azure MySQL
        Entrega entregaCalificada = entregaRepository.save(entrega);

        // 6. Construir respuesta estructurada
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Entrega calificada exitosamente");
        respuesta.put("entregaId", entregaCalificada.getId());
        respuesta.put("alumno", entregaCalificada.getAlumno().getNombre());
        respuesta.put("tituloTarea", entregaCalificada.getPortafolio().getTitulo());
        respuesta.put("calificacion", entregaCalificada.getCalificacion());
        respuesta.put("comentarios", entregaCalificada.getComentarios());
        respuesta.put("fechaEntrega", entregaCalificada.getFechaEntrega());

        return ResponseEntity.ok(respuesta);
    }
}
