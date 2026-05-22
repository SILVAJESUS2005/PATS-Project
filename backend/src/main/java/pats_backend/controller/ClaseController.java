package pats_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import pats_backend.model.Clase;
import pats_backend.model.Usuario;
import pats_backend.dto.CrearClaseDTO;
import pats_backend.dto.UnirseClaseDTO;
import pats_backend.repository.ClaseRepository;
import pats_backend.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/clases")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ClaseController {

    private final ClaseRepository claseRepository;
    private final UsuarioRepository usuarioRepository;

    public ClaseController(ClaseRepository claseRepository, UsuarioRepository usuarioRepository) {
        this.claseRepository = claseRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearClase(@Valid @RequestBody CrearClaseDTO crearClaseDTO, 
                                       BindingResult result, 
                                       HttpSession session) {
        
        // 1. Validar errores de entrada en los campos (nombre vacío o muy corto)
        if (result.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errores);
        }

        // 2. Obtener el usuario autenticado de la sesión HTTP
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión para realizar esta acción");
        }

        // Obtener la entidad de usuario actualizada desde la base de datos de Azure
        Usuario docente = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        if (docente == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El usuario docente no existe en el sistema");
        }

        // 3. Crear la clase y generar el código de acceso único (tipo PATS-XYZ123)
        Clase nuevaClase = new Clase();
        nuevaClase.setNombre(crearClaseDTO.getNombre());
        nuevaClase.setDescripcion(crearClaseDTO.getDescripcion());
        nuevaClase.setDocente(docente);

        // Generar un código único que no colisione en Azure
        String codigoAcceso;
        do {
            codigoAcceso = generarCodigoAcceso6Caracteres();
        } while (claseRepository.findByCodigoAcceso(codigoAcceso).isPresent());

        nuevaClase.setCodigoAcceso(codigoAcceso);

        // Guardar la clase de forma asíncrona en tu base de datos de Azure
        Clase claseGuardada = claseRepository.save(nuevaClase);

        // 4. Retornar una respuesta formateada de forma segura (sin contraseñas)
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Clase creada correctamente en PATS");
        respuesta.put("id", claseGuardada.getId());
        respuesta.put("nombre", claseGuardada.getNombre());
        respuesta.put("descripcion", claseGuardada.getDescripcion());
        respuesta.put("codigoAcceso", claseGuardada.getCodigoAcceso());

        Map<String, Object> docenteInfo = new HashMap<>();
        docenteInfo.put("id", docente.getId());
        docenteInfo.put("nombre", docente.getNombre());
        docenteInfo.put("correo", docente.getCorreo());
        docenteInfo.put("rol", docente.getRol());
        respuesta.put("docente", docenteInfo);

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/unirse")
    public ResponseEntity<?> unirseAClase(@Valid @RequestBody UnirseClaseDTO unirseClaseDTO,
                                         BindingResult result,
                                         HttpSession session) {

        // 1. Validar errores de entrada (código de acceso vacío)
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

        // Obtener el alumno fresco de la base de datos de Azure
        Usuario alumno = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        if (alumno == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("El usuario alumno no existe en el sistema");
        }

        // 3. Buscar la clase en Azure por el código de acceso
        Clase clase = claseRepository.findByCodigoAcceso(unirseClaseDTO.getCodigoAcceso()).orElse(null);
        if (clase == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Código de acceso inválido: La clase no existe");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        // 4. Verificar si el alumno ya está inscrito en la clase
        if (clase.getAlumnos().stream().anyMatch(a -> a.getId().equals(alumno.getId()))) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ya estás inscrito en esta clase");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 5. Vincular al alumno en la relación ManyToMany (tabla clase_alumnos)
        clase.getAlumnos().add(alumno);
        claseRepository.save(clase); // JPA y Hibernate persistirán la relación automáticamente en Azure

        // 6. Respuesta limpia de éxito
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Te has unido exitosamente a la clase " + clase.getNombre());
        respuesta.put("claseId", clase.getId());
        respuesta.put("nombreClase", clase.getNombre());
        respuesta.put("descripcionClase", clase.getDescripcion());
        respuesta.put("docente", clase.getDocente().getNombre());

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Método auxiliar para generar 6 caracteres alfanuméricos aleatorios
     * en mayúsculas antecedidos por el prefijo de tu aplicación "PATS-".
     */
    private String generarCodigoAcceso6Caracteres() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("PATS-");
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }
        return sb.toString();
    }
}
