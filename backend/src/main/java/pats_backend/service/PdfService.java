package pats_backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import pats_backend.model.Entrega;
import pats_backend.model.EvidenciaArchivo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private final FileStorageService fileStorageService;

    public PdfService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public byte[] generatePortafolioPdf(Entrega entrega) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Fuentes
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.BLACK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLUE);

        // 1. Portada
        Paragraph institucion = new Paragraph("Instituto Tecnológico - PATS", titleFont);
        institucion.setAlignment(Element.ALIGN_CENTER);
        institucion.setSpacingAfter(20);
        document.add(institucion);

        Paragraph titulo = new Paragraph(entrega.getPortafolio().getTitulo(), subtitleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(40);
        document.add(titulo);

        String materia = "Materia: " + (entrega.getPortafolio().getClase() != null ? entrega.getPortafolio().getClase().getNombre() : "N/A");
        String docente = "Docente: " + (entrega.getPortafolio().getClase() != null ? entrega.getPortafolio().getClase().getDocente().getNombre() : "N/A");
        String alumnoStr = "Alumno: " + entrega.getAlumno().getNombre();
        String matriculaStr = "Matrícula: " + (entrega.getAlumno().getMatricula() != null ? entrega.getAlumno().getMatricula() : "N/A");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String fechaStr = "Fecha de Entrega: " + entrega.getFechaEntrega().format(formatter);

        document.add(new Paragraph(materia, normalFont));
        document.add(new Paragraph(docente, normalFont));
        document.add(new Paragraph(alumnoStr, normalFont));
        document.add(new Paragraph(matriculaStr, normalFont));
        document.add(new Paragraph(fechaStr, normalFont));

        document.newPage();

        // 2. Introducción
        document.add(new Paragraph("Introducción", headerFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(entrega.getIntroduccion() != null ? entrega.getIntroduccion() : "Sin introducción.", normalFont));
        
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // 3. Evidencias
        document.add(new Paragraph("Evidencias", headerFont));
        document.add(new Paragraph(" "));

        List<EvidenciaArchivo> archivos = entrega.getArchivos();
        if (archivos != null && !archivos.isEmpty()) {
            for (EvidenciaArchivo archivo : archivos) {
                document.add(new Paragraph("Categoría: " + archivo.getCategoriaEvidencia().name(), boldFont));
                document.add(new Paragraph(" "));
                try {
                    String fileDir = System.getProperty("user.dir") + "/uploads/";
                    File file = new File(fileDir + archivo.getArchivoUrl());
                    if (file.exists()) {
                        Image img = Image.getInstance(file.getAbsolutePath());
                        // Scale image to fit page width
                        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                                - document.rightMargin() - 0) / img.getWidth()) * 100;
                        
                        if (scaler < 100) {
                            img.scalePercent(scaler);
                        }
                        
                        document.add(img);
                    } else {
                        document.add(new Paragraph("[Imagen no encontrada en el servidor: " + archivo.getArchivoUrl() + "]", normalFont));
                    }
                } catch (Exception e) {
                    document.add(new Paragraph("[Error al cargar imagen: " + archivo.getArchivoUrl() + "]", normalFont));
                }
                document.add(new Paragraph(" "));
            }
        } else {
            document.add(new Paragraph("No hay evidencias adjuntas.", normalFont));
        }

        document.newPage();

        // 4. Conclusión
        document.add(new Paragraph("Conclusión", headerFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(entrega.getConclusion() != null ? entrega.getConclusion() : "Sin conclusión.", normalFont));

        document.close();

        return out.toByteArray();
    }
}
