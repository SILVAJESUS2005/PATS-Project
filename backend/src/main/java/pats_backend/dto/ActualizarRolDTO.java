package pats_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ActualizarRolDTO {

    @NotBlank(message = "El rol no puede estar vacío")
    @Pattern(regexp = "^(ALUMNO|DOCENTE)$", message = "El rol debe ser ALUMNO o DOCENTE")
    private String rol;

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
