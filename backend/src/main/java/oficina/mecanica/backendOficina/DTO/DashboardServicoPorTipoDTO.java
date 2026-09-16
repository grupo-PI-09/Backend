package oficina.mecanica.backendOficina.DTO;

/** Uma barra do gráfico "Preventiva vs corretiva — esperado x realizado". */
public class DashboardServicoPorTipoDTO {

    private String tipo;
    private String label;
    private long esperado;
    private long realizado;

    public DashboardServicoPorTipoDTO() {
    }

    public DashboardServicoPorTipoDTO(String tipo, String label, long esperado, long realizado) {
        this.tipo = tipo;
        this.label = label;
        this.esperado = esperado;
        this.realizado = realizado;
    }

    public String getTipo() { return tipo; }
    public String getLabel() { return label; }
    public long getEsperado() { return esperado; }
    public long getRealizado() { return realizado; }
}
