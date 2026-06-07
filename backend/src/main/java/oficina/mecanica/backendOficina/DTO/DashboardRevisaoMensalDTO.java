package oficina.mecanica.backendOficina.DTO;

public class DashboardRevisaoMensalDTO {

    private String label;
    private long realizadas;
    private long estimadas;

    public DashboardRevisaoMensalDTO() {
    }

    public DashboardRevisaoMensalDTO(String label, long realizadas, long estimadas) {
        this.label = label;
        this.realizadas = realizadas;
        this.estimadas = estimadas;
    }

    public String getLabel() {
        return label;
    }

    public long getRealizadas() {
        return realizadas;
    }

    public long getEstimadas() {
        return estimadas;
    }
}
