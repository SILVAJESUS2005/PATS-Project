package pats_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CrearClaseDTO {

    @NotBlank(message = "El nombre de la clase es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de la clase debe tener entre 3 y 100 caracteres")
    private String nombre;

    private String descripcion;

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
