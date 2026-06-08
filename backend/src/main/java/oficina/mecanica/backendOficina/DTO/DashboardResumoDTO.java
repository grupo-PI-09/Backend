package oficina.mecanica.backendOficina.DTO;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResumoDTO {

    private long totalClientes;
    private long totalVeiculos;
    private long totalOrdensServico;
    private long totalOrdensAbertasEmAndamento;
    private long totalOrdensFinalizadas;
    private long ordensFinalizadasMes;
    private long novosClientesMes;
    private long proximasRevisoes;
    private long notificacoesEnviadas;
    private BigDecimal faturamentoTotal;
    private BigDecimal faturamentoMes;
    private List<DashboardOrdemDTO> ultimasOrdens;
    private List<DashboardOrdemDTO> servicosProximosRevisao;
    private List<DashboardPontoNumericoDTO> finalizacoesUltimosMeses;
    private List<DashboardPontoFinanceiroDTO> faturamentoUltimosMeses;
    private List<DashboardRevisaoMensalDTO> revisoesPreventivasUltimosMeses;

    public DashboardResumoDTO() {
    }

    public DashboardResumoDTO(long totalClientes, long totalVeiculos, long totalOrdensServico,
                              long totalOrdensAbertasEmAndamento, long totalOrdensFinalizadas,
                              long ordensFinalizadasMes, long novosClientesMes, long proximasRevisoes,
                              long notificacoesEnviadas, BigDecimal faturamentoTotal, BigDecimal faturamentoMes,
                              List<DashboardOrdemDTO> ultimasOrdens,
                              List<DashboardOrdemDTO> servicosProximosRevisao,
                              List<DashboardPontoNumericoDTO> finalizacoesUltimosMeses,
                              List<DashboardPontoFinanceiroDTO> faturamentoUltimosMeses,
                              List<DashboardRevisaoMensalDTO> revisoesPreventivasUltimosMeses) {
        this.totalClientes = totalClientes;
        this.totalVeiculos = totalVeiculos;
        this.totalOrdensServico = totalOrdensServico;
        this.totalOrdensAbertasEmAndamento = totalOrdensAbertasEmAndamento;
        this.totalOrdensFinalizadas = totalOrdensFinalizadas;
        this.ordensFinalizadasMes = ordensFinalizadasMes;
        this.novosClientesMes = novosClientesMes;
        this.proximasRevisoes = proximasRevisoes;
        this.notificacoesEnviadas = notificacoesEnviadas;
        this.faturamentoTotal = faturamentoTotal;
        this.faturamentoMes = faturamentoMes;
        this.ultimasOrdens = ultimasOrdens;
        this.servicosProximosRevisao = servicosProximosRevisao;
        this.finalizacoesUltimosMeses = finalizacoesUltimosMeses;
        this.faturamentoUltimosMeses = faturamentoUltimosMeses;
        this.revisoesPreventivasUltimosMeses = revisoesPreventivasUltimosMeses;
    }

    public long getTotalClientes() { return totalClientes; }
    public long getTotalVeiculos() { return totalVeiculos; }
    public long getTotalOrdensServico() { return totalOrdensServico; }
    public long getTotalOrdensAbertasEmAndamento() { return totalOrdensAbertasEmAndamento; }
    public long getTotalOrdensAbertas() { return totalOrdensAbertasEmAndamento; }
    public long getTotalOrdensFinalizadas() { return totalOrdensFinalizadas; }
    public long getOrdensFinalizadasMes() { return ordensFinalizadasMes; }
    public long getNovosClientesMes() { return novosClientesMes; }
    public long getProximasRevisoes() { return proximasRevisoes; }
    public long getNotificacoesEnviadas() { return notificacoesEnviadas; }
    public BigDecimal getFaturamentoTotal() { return faturamentoTotal; }
    public BigDecimal getFaturamentoMes() { return faturamentoMes; }
    public List<DashboardOrdemDTO> getUltimasOrdens() { return ultimasOrdens; }
    public List<DashboardOrdemDTO> getServicosProximosRevisao() { return servicosProximosRevisao; }
    public List<DashboardPontoNumericoDTO> getFinalizacoesUltimosMeses() { return finalizacoesUltimosMeses; }
    public List<DashboardPontoFinanceiroDTO> getFaturamentoUltimosMeses() { return faturamentoUltimosMeses; }
    public List<DashboardRevisaoMensalDTO> getRevisoesPreventivasUltimosMeses() { return revisoesPreventivasUltimosMeses; }
}
