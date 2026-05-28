package pats_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

import pats_backend.model.Usuario;
import pats_backend.dto.ActualizarPerfilDTO;
import pats_backend.dto.ActualizarRolDTO;
import pats_backend.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarPerfil(@Valid @RequestBody ActualizarPerfilDTO dto, 
                                              BindingResult result, 
                                              HttpSession session) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errores);
        }

        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión para actualizar tu perfil.");
        }

        Usuario usuarioDb = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        if (usuarioDb == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        // Custom validation: Si es ALUMNO, la matrícula no puede estar vacía
        if ("ALUMNO".equals(usuarioDb.getRol()) && (dto.getMatricula() == null || dto.getMatricula().trim().isEmpty())) {
            Map<String, String> errores = new HashMap<>();
            errores.put("matricula", "La matrícula no puede estar vacía para un alumno.");
            return ResponseEntity.badRequest().body(errores);
        }

        usuarioDb.setNombre(dto.getNombre());
        if ("ALUMNO".equals(usuarioDb.getRol())) {
            usuarioDb.setMatricula(dto.getMatricula());
        } else if ("DOCENTE".equals(usuarioDb.getRol())) {
            usuarioDb.setMatricula("DOCENTE_CONFIRMADO");
        }

        usuarioRepository.save(usuarioDb);
        
        // Actualizamos la sesión con los nuevos datos
        session.setAttribute("usuario", usuarioDb);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Perfil actualizado correctamente");
        respuesta.put("nombre", usuarioDb.getNombre());
        respuesta.put("matricula", usuarioDb.getMatricula());

        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/rol")
    public ResponseEntity<?> actualizarRol(@Valid @RequestBody ActualizarRolDTO dto, 
                                           BindingResult result, 
                                           HttpSession session) {
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errores);
        }

        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión.");
        }

        Usuario usuarioDb = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        if (usuarioDb == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        usuarioDb.setRol(dto.getRol());
        usuarioRepository.save(usuarioDb);
        
        session.setAttribute("usuario", usuarioDb);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Rol actualizado correctamente");
        respuesta.put("rol", usuarioDb.getRol());

        return ResponseEntity.ok(respuesta);
    }
}
