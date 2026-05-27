package pats_backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "evidencias_archivos")
public class EvidenciaArchivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "archivo_url", nullable = false)
    private String archivoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_evidencia", nullable = false)
    private CategoriaEvidencia categoriaEvidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrega_id", nullable = false)
    private Entrega entrega;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArchivoUrl() {
        return archivoUrl;
    }

    public void setArchivoUrl(String archivoUrl) {
        this.archivoUrl = archivoUrl;
    }

    public CategoriaEvidencia getCategoriaEvidencia() {
        return categoriaEvidencia;
    }

    public void setCategoriaEvidencia(CategoriaEvidencia categoriaEvidencia) {
        this.categoriaEvidencia = categoriaEvidencia;
    }

    public Entrega getEntrega() {
        return entrega;
    }

    public void setEntrega(Entrega entrega) {
        this.entrega = entrega;
    }
}
