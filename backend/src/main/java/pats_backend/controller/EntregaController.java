package pats_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import pats_backend.model.Clase;
import pats_backend.model.Entrega;
import pats_backend.model.Usuario;
import pats_backend.model.Portafolio;
import pats_backend.dto.CalificarEntregaDTO;
import pats_backend.repository.EntregaRepository;
import pats_backend.repository.UsuarioRepository;
import pats_backend.repository.PortafolioRepository;
import pats_backend.service.FileStorageService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/entregas")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class EntregaController {

    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PortafolioRepository portafolioRepository;
    private final FileStorageService fileStorageService;

    public EntregaController(EntregaRepository entregaRepository,
            UsuarioRepository usuarioRepository,
            PortafolioRepository portafolioRepository,
            FileStorageService fileStorageService) {
        this.entregaRepository = entregaRepository;
        this.usuarioRepository = usuarioRepository;
        this.portafolioRepository = portafolioRepository;
        this.fileStorageService = fileStorageService;
    }

    // =========================================================================
    // TAREA 1: Subir Archivo (MultipartFile) y crear la Entrega (Alumno)
    // =========================================================================
    @PostMapping("/subir")
    public ResponseEntity<?> subirEvidencia(@RequestParam("file") MultipartFile file,
            @RequestParam("portafolioId") Long portafolioId,
            HttpSession session) {
        // Validar sesión
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión");
        }

        Usuario alumno = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        Portafolio portafolio = portafolioRepository.findById(portafolioId).orElse(null);

        if (alumno == null || portafolio == null) {
            return ResponseEntity.badRequest().body("Usuario o Portafolio no válido.");
        }

        try {
            // Guardar el archivo localmente en el servidor
            String fileName = fileStorageService.storeFile(file);

            // Generar URL para acceder al archivo (Endpoint de descarga)
            String fileUrl = "/api/entregas/descargar/" + fileName;

            // Guardar el registro en la base de datos (entregas)
            Entrega entrega = new Entrega();
            entrega.setArchivoUrl(fileUrl); // Se guarda la URL de la evidencia
            entrega.setAlumno(alumno);
            entrega.setPortafolio(portafolio);

            entregaRepository.save(entrega);

            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Evidencia subida correctamente");
            respuesta.put("archivoUrl", fileUrl);

            return ResponseEntity.ok(respuesta);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fallo al subir archivo: " + ex.getMessage());
        }
    }

    // =========================================================================
    // TAREA 2: Descargar/Visualizar el Archivo (Docente / Alumno)
    // =========================================================================
    @GetMapping("/descargar/{fileName:.+}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable String fileName, HttpServletRequest request) {
        // Cargar archivo como recurso
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Intentar determinar el tipo de contenido del archivo
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // No se pudo determinar el tipo
        }

        // Tipo por defecto si no se puede determinar
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // =========================================================================
    // Endpoint existente: Calificar Entrega
    // =========================================================================
    @PutMapping("/{id}/calificar")
    public ResponseEntity<?> calificarEntrega(@PathVariable Long id,
            @Valid @RequestBody CalificarEntregaDTO dto,
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión");
        }

        Usuario docente = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        Entrega entrega = entregaRepository.findById(id).orElse(null);

        if (docente == null || entrega == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Docente o entrega no existe");
        }

        Clase clase = entrega.getPortafolio().getClase();
        if (!clase.getDocente().getId().equals(docente.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }

        entrega.setCalificacion(dto.getCalificacion());
        entrega.setComentarios(dto.getComentarios());

        Entrega entregaCalificada = entregaRepository.save(entrega);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Entrega calificada exitosamente");
        respuesta.put("calificacion", entregaCalificada.getCalificacion());
        respuesta.put("comentarios", entregaCalificada.getComentarios());

        return ResponseEntity.ok(respuesta);
    }

    // =========================================================================
    // TAREA 3: Ver Lista de Alumnos y Entregas de un Portafolio (Docente)
    // =========================================================================
    @GetMapping("/portafolio/{portafolioId}")
    public ResponseEntity<?> obtenerEntregasPorPortafolio(@PathVariable Long portafolioId, HttpSession session) {
        
        Usuario usuarioSession = (Usuario) session.getAttribute("usuario");
        if (usuarioSession == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes iniciar sesión");
        }

        Usuario docente = usuarioRepository.findById(usuarioSession.getId()).orElse(null);
        Portafolio portafolio = portafolioRepository.findById(portafolioId).orElse(null);

        if (docente == null || portafolio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Portafolio no encontrado");
        }

        // Verificar que el maestro en sesión es el creador de la clase a la que pertenece el portafolio
        if (!portafolio.getClase().getDocente().getId().equals(docente.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado. No eres el maestro de esta clase.");
        }

        // Obtener todos los alumnos inscritos en la clase
        java.util.Set<Usuario> alumnos = portafolio.getClase().getAlumnos();
        
        // Obtener todas las entregas que ya se han hecho para este portafolio
        java.util.List<Entrega> entregas = entregaRepository.findByPortafolioId(portafolioId);

        java.util.List<Map<String, Object>> respuesta = new java.util.ArrayList<>();

        // Mapear cada alumno con su respectiva entrega (si la tiene)
        for (Usuario alumno : alumnos) {
            Map<String, Object> item = new HashMap<>();
            item.put("alumnoId", alumno.getId());
            item.put("nombreAlumno", alumno.getNombre());
            item.put("correo", alumno.getCorreo());
            
            // Buscar si este alumno en particular ya entregó
            Entrega entregaAlumno = entregas.stream()
                .filter(e -> e.getAlumno().getId().equals(alumno.getId()))
                .findFirst()
                .orElse(null);
            
            if (entregaAlumno != null) {
                item.put("entregado", true);
                item.put("entregaId", entregaAlumno.getId());
                item.put("archivoUrl", entregaAlumno.getArchivoUrl());
                item.put("calificacion", entregaAlumno.getCalificacion());
                item.put("fechaEntrega", entregaAlumno.getFechaEntrega());
                item.put("comentarios", entregaAlumno.getComentarios());
            } else {
                item.put("entregado", false);
            }
            respuesta.add(item);
        }

        return ResponseEntity.ok(respuesta);
    }
}
