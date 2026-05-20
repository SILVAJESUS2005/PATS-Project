package pats_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import pats_backend.dto.LoginDTO;
import pats_backend.dto.RegistroUsuarioDTO;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import pats_backend.model.Usuario;
import pats_backend.repository.UsuarioRepository;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // Configuración de CORS segura para Vue
public class AuthController {
    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroUsuarioDTO registroDTO, BindingResult result) {

        // 1. Validar errores de servidor (Campos vacíos, formato)
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
            // Retorna un JSON con los errores dinámicos y código 400 Bad Request
            return ResponseEntity.badRequest().body(errores);
        }



        // 2. Guardar en Base de Datos (usando el repositorio inyectado)
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(registroDTO.getNombre());
        // Asumiendo que el modelo Usuario no tiene apellidos, si no lo tiene, omitimos o concatenamos
        // ¡Ojo! El DTO tiene apellidos pero el Modelo Usuario solo tiene nombre. Los concatenaremos:
        nuevoUsuario.setNombre(registroDTO.getNombre() + " " + registroDTO.getApellidos());
        nuevoUsuario.setCorreo(registroDTO.getCorreo());
        nuevoUsuario.setPassword(registroDTO.getPassword());
        nuevoUsuario.setRol("USER"); // Rol por defecto
        
        usuarioRepository.save(nuevoUsuario);

        // 3. Respuesta de éxito
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario registrado correctamente en PATS");
        return ResponseEntity.ok(respuesta);



    }

    // Metodo inicio sesion
    // Si las credenciales son correctas, se guarda el usuario en una sesión
    // usando HttpSession para que el servidor recuerde quién inició sesión.
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginDTO loginDTO, HttpSession session) {

        Optional<Usuario> usuarioOptional = usuarioRepository.findByCorreo(loginDTO.getCorreo());

        if (usuarioOptional.isPresent()) {

            Usuario usuarioEncontrado = usuarioOptional.get();

            if (usuarioEncontrado.getPassword().equals(loginDTO.getPassword())) {

                // Guardar usuario en sesión
                session.setAttribute("usuario", usuarioEncontrado);

                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Bienvenido a PATS");
                respuesta.put("nombre", usuarioEncontrado.getNombre());
                respuesta.put("correo", usuarioEncontrado.getCorreo());
                respuesta.put("rol", usuarioEncontrado.getRol());

                return ResponseEntity.ok(respuesta);
            }
        }

        return ResponseEntity.status(401).body("Correo o contraseña incorrectos");
    }
    @GetMapping("/logout")
    public ResponseEntity<?> cerrarSesion(HttpSession session) {
        session.invalidate();

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Sesión cerrada correctamente");

        return ResponseEntity.ok(respuesta);
    }
}