package pats_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import pats_backend.dto.LoginDTO;
import pats_backend.dto.RegistroUsuarioDTO;
import pats_backend.dto.GoogleTokenDTO;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import pats_backend.model.Usuario;
import pats_backend.repository.UsuarioRepository;
import java.util.Optional;
import java.util.Collections;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // Configuración de CORS segura para Vue
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final String CLIENT_ID = "294864417020-usltovtittatrqk6rl8d107tqgc6ne3h.apps.googleusercontent.com";

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroUsuarioDTO registroDTO,
            BindingResult result) {

        // 1. Validar errores de servidor (Campos vacíos, formato)
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errores);
        }

        // 2. Guardar en Base de Datos
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(registroDTO.getNombre() + " " + registroDTO.getApellidos());
        nuevoUsuario.setCorreo(registroDTO.getCorreo());

        // Encriptar contraseña con BCrypt
        nuevoUsuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        
        // Si no se especifica rol, asignamos "USER" (Alumno) por defecto
        String rol = registroDTO.getRol();
        if (rol == null || rol.trim().isEmpty()) {
            rol = "USER";
        } else {
            rol = rol.trim().toUpperCase();
        }
        nuevoUsuario.setRol(rol);

        usuarioRepository.save(nuevoUsuario);

        // 3. Respuesta de éxito
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario registrado correctamente en PATS");
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginDTO loginDTO, HttpSession session) {

        Optional<Usuario> usuarioOptional = usuarioRepository.findByCorreo(loginDTO.getCorreo());

        if (usuarioOptional.isPresent()) {
            Usuario usuarioEncontrado = usuarioOptional.get();

            // Verificar contraseña con BCrypt
            if (passwordEncoder.matches(loginDTO.getPassword(), usuarioEncontrado.getPassword())) {

                // Guardar usuario en sesión
                session.setAttribute("usuario", usuarioEncontrado);

                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Bienvenido a PATS");
                respuesta.put("nombre", usuarioEncontrado.getNombre());
                respuesta.put("correo", usuarioEncontrado.getCorreo());
                respuesta.put("rol", usuarioEncontrado.getRol());
                respuesta.put("matricula", usuarioEncontrado.getMatricula() != null ? usuarioEncontrado.getMatricula() : "");

                return ResponseEntity.ok(respuesta);
            }
        }

        return ResponseEntity.status(401).body("Correo o contraseña incorrectos");
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginConGoogle(@RequestBody GoogleTokenDTO tokenDTO, HttpSession session) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken idToken = verifier.verify(tokenDTO.getToken());
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                Optional<Usuario> usuarioOptional = usuarioRepository.findByCorreo(email);
                Usuario usuario;

                if (usuarioOptional.isPresent()) {
                    usuario = usuarioOptional.get();
                } else {
                    // Si no existe, lo registramos automáticamente
                    usuario = new Usuario();
                    usuario.setNombre(name);
                    usuario.setCorreo(email);
                    usuario.setPassword(""); // Sin contraseña (inicia sesión con Google)
                    usuario.setRol("PENDIENTE");
                    usuario = usuarioRepository.save(usuario);
                }

                session.setAttribute("usuario", usuario);

                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("mensaje", "Bienvenido a PATS (Google)");
                respuesta.put("nombre", usuario.getNombre());
                respuesta.put("correo", usuario.getCorreo());
                respuesta.put("rol", usuario.getRol());

                return ResponseEntity.ok(respuesta);
            } else {
                return ResponseEntity.status(401).body("Token de Google inválido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al verificar token de Google: " + e.getMessage());
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> cerrarSesion(HttpSession session) {
        session.invalidate();

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Sesión cerrada correctamente");

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual(HttpSession session) {
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(401).body("No hay sesión activa");
        }
        
        Optional<Usuario> usuarioDb = usuarioRepository.findById(usuarioSession.getId());
        if (usuarioDb.isPresent()) {
            return ResponseEntity.ok(usuarioDb.get());
        }
        
        return ResponseEntity.status(401).body("Usuario no encontrado en DB");
    }
}