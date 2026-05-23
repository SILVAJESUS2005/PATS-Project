package pats_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UnirseClaseDTO {

    @NotBlank(message = "El código de acceso de la clase es obligatorio")
    private String codigoAcceso;

    // Getters y Setters
    public String getCodigoAcceso() {
        return codigoAcceso;
    }

    public void setCodigoAcceso(String codigoAcceso) {
        this.codigoAcceso = codigoAcceso;
    }
}
