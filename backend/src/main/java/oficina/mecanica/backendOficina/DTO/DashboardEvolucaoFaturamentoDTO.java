package oficina.mecanica.backendOficina.DTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gráfico "Evolução do faturamento": ano atual x ano anterior, de janeiro até o
 * mês atual. As listas têm o mesmo tamanho de {@code meses}.
 */
public class DashboardEvolucaoFaturamentoDTO {

    private int anoAtual;
    private int anoAnterior;
    private List<String> meses;
    private List<BigDecimal> mensalAnoAtual;
    private List<BigDecimal> mensalAnoAnterior;
    private List<BigDecimal> acumuladoAnoAtual;
    private List<BigDecimal> acumuladoAnoAnterior;

    public DashboardEvolucaoFaturamentoDTO() {
    }

    public DashboardEvolucaoFaturamentoDTO(int anoAtual, int anoAnterior, List<String> meses,
                                           List<BigDecimal> mensalAnoAtual, List<BigDecimal> mensalAnoAnterior,
                                           List<BigDecimal> acumuladoAnoAtual, List<BigDecimal> acumuladoAnoAnterior) {
        this.anoAtual = anoAtual;
        this.anoAnterior = anoAnterior;
        this.meses = meses;
        this.mensalAnoAtual = mensalAnoAtual;
        this.mensalAnoAnterior = mensalAnoAnterior;
        this.acumuladoAnoAtual = acumuladoAnoAtual;
        this.acumuladoAnoAnterior = acumuladoAnoAnterior;
    }

    public int getAnoAtual() { return anoAtual; }
    public int getAnoAnterior() { return anoAnterior; }
    public List<String> getMeses() { return meses; }
    public List<BigDecimal> getMensalAnoAtual() { return mensalAnoAtual; }
    public List<BigDecimal> getMensalAnoAnterior() { return mensalAnoAnterior; }
    public List<BigDecimal> getAcumuladoAnoAtual() { return acumuladoAnoAtual; }
    public List<BigDecimal> getAcumuladoAnoAnterior() { return acumuladoAnoAnterior; }
}
