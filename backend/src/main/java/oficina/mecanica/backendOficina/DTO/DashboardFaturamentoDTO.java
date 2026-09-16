package oficina.mecanica.backendOficina.DTO;

/** Card "Faturamento" com o toggle mensal / semestral / anual. */
public class DashboardFaturamentoDTO {

    private DashboardFaturamentoPeriodoDTO mensal;
    private DashboardFaturamentoPeriodoDTO semestral;
    private DashboardFaturamentoPeriodoDTO anual;

    public DashboardFaturamentoDTO() {
    }

    public DashboardFaturamentoDTO(DashboardFaturamentoPeriodoDTO mensal,
                                   DashboardFaturamentoPeriodoDTO semestral,
                                   DashboardFaturamentoPeriodoDTO anual) {
        this.mensal = mensal;
        this.semestral = semestral;
        this.anual = anual;
    }

    public DashboardFaturamentoPeriodoDTO getMensal() { return mensal; }
    public DashboardFaturamentoPeriodoDTO getSemestral() { return semestral; }
    public DashboardFaturamentoPeriodoDTO getAnual() { return anual; }
}
