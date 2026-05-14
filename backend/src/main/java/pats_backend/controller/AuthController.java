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

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite que Vue.js se conecte
public class AuthController {

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



        // 2. Aquí iría tu lógica para guardar en Base de Datos (Service)
        // authService.registrarNuevoUsuario(registroDTO);

        // 3. Respuesta de éxito
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Usuario registrado correctamente en PATS");
        return ResponseEntity.ok(respuesta);



    }
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody LoginDTO loginDTO) {
        // Simulación de validación (Más adelante usarás la base de datos)
        if ("admin@pats.com".equals(loginDTO.getCorreo()) && "123456".equals(loginDTO.getPassword())) {
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Bienvenido a PATS");
            respuesta.put("rol", "ALUMNO"); // Dato simulado del PDF
            return ResponseEntity.ok(respuesta);
        }

        return ResponseEntity.status(401).body("Correo o contraseña incorrectos");
    }

}