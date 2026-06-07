package oficina.mecanica.backendOficina.DTO;

import java.math.BigDecimal;

public class DashboardPontoFinanceiroDTO {

    private String label;
    private BigDecimal valor;

    public DashboardPontoFinanceiroDTO() {
    }

    public DashboardPontoFinanceiroDTO(String label, BigDecimal valor) {
        this.label = label;
        this.valor = valor;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
