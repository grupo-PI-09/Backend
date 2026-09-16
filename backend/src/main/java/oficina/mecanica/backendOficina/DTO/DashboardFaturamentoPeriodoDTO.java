package oficina.mecanica.backendOficina.DTO;

import java.math.BigDecimal;

/**
 * Faturamento de um período (mensal, semestral ou anual) comparado com o mesmo
 * período do ano anterior, até o mesmo dia ("até agora").
 */
public class DashboardFaturamentoPeriodoDTO {

    private String periodo;
    private String rotulo;
    private BigDecimal valor;
    private String rotuloPeriodoAnterior;
    private BigDecimal valorPeriodoAnterior;
    /** Variação percentual em relação ao período anterior; null quando não há base de comparação. */
    private Double variacaoPercentual;

    public DashboardFaturamentoPeriodoDTO() {
    }

    public DashboardFaturamentoPeriodoDTO(String periodo, String rotulo, BigDecimal valor,
                                          String rotuloPeriodoAnterior, BigDecimal valorPeriodoAnterior,
                                          Double variacaoPercentual) {
        this.periodo = periodo;
        this.rotulo = rotulo;
        this.valor = valor;
        this.rotuloPeriodoAnterior = rotuloPeriodoAnterior;
        this.valorPeriodoAnterior = valorPeriodoAnterior;
        this.variacaoPercentual = variacaoPercentual;
    }

    public String getPeriodo() { return periodo; }
    public String getRotulo() { return rotulo; }
    public BigDecimal getValor() { return valor; }
    public String getRotuloPeriodoAnterior() { return rotuloPeriodoAnterior; }
    public BigDecimal getValorPeriodoAnterior() { return valorPeriodoAnterior; }
    public Double getVariacaoPercentual() { return variacaoPercentual; }
}
