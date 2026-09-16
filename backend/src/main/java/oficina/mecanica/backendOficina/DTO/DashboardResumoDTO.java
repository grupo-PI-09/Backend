package oficina.mecanica.backendOficina.DTO;

import java.util.List;

/** Resposta de GET /dashboard/resumo — painel financeiro. */
public class DashboardResumoDTO {

    private DashboardFaturamentoDTO faturamento;
    private long totalClientes;
    private long novosClientesMes;
    /** Mês atual do ano anterior e do ano atual, nesta ordem. */
    private List<DashboardPontoFinanceiroDTO> faturamentoMensalComparativo;
    private DashboardEvolucaoFaturamentoDTO evolucaoFaturamento;
    private DashboardServicosPorTipoDTO servicosPorTipo;

    public DashboardResumoDTO() {
    }

    public DashboardResumoDTO(DashboardFaturamentoDTO faturamento, long totalClientes, long novosClientesMes,
                              List<DashboardPontoFinanceiroDTO> faturamentoMensalComparativo,
                              DashboardEvolucaoFaturamentoDTO evolucaoFaturamento,
                              DashboardServicosPorTipoDTO servicosPorTipo) {
        this.faturamento = faturamento;
        this.totalClientes = totalClientes;
        this.novosClientesMes = novosClientesMes;
        this.faturamentoMensalComparativo = faturamentoMensalComparativo;
        this.evolucaoFaturamento = evolucaoFaturamento;
        this.servicosPorTipo = servicosPorTipo;
    }

    public DashboardFaturamentoDTO getFaturamento() { return faturamento; }
    public long getTotalClientes() { return totalClientes; }
    public long getNovosClientesMes() { return novosClientesMes; }
    public List<DashboardPontoFinanceiroDTO> getFaturamentoMensalComparativo() { return faturamentoMensalComparativo; }
    public DashboardEvolucaoFaturamentoDTO getEvolucaoFaturamento() { return evolucaoFaturamento; }
    public DashboardServicosPorTipoDTO getServicosPorTipo() { return servicosPorTipo; }
}
