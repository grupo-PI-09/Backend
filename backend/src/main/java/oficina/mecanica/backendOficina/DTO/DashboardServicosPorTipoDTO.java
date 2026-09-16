package oficina.mecanica.backendOficina.DTO;

import java.util.List;

/**
 * Gráfico "Preventiva vs corretiva — esperado x realizado" do mês atual.
 * Esperado = ordens do tipo abertas no mês (exceto canceladas);
 * realizado = ordens do tipo finalizadas no mês.
 */
public class DashboardServicosPorTipoDTO {

    private String periodo;
    private List<DashboardServicoPorTipoDTO> tipos;

    public DashboardServicosPorTipoDTO() {
    }

    public DashboardServicosPorTipoDTO(String periodo, List<DashboardServicoPorTipoDTO> tipos) {
        this.periodo = periodo;
        this.tipos = tipos;
    }

    public String getPeriodo() { return periodo; }
    public List<DashboardServicoPorTipoDTO> getTipos() { return tipos; }
}
