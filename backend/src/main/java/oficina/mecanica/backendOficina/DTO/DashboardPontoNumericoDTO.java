package oficina.mecanica.backendOficina.DTO;

public class DashboardPontoNumericoDTO {

    private String label;
    private long valor;

    public DashboardPontoNumericoDTO() {
    }

    public DashboardPontoNumericoDTO(String label, long valor) {
        this.label = label;
        this.valor = valor;
    }

    public String getLabel() {
        return label;
    }

    public long getValor() {
        return valor;
    }
}
