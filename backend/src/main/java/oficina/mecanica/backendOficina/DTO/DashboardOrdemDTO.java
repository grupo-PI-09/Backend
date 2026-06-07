package oficina.mecanica.backendOficina.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardOrdemDTO {

    private Long id;
    private String cliente;
    private String veiculo;
    private String placaVeiculo;
    private String modeloVeiculo;
    private String status;
    private LocalDateTime data;
    private LocalDateTime dataProximaRevisao;
    private BigDecimal valorTotal;

    public DashboardOrdemDTO() {
    }

    public DashboardOrdemDTO(Long id, String cliente, String veiculo, String placaVeiculo,
                             String modeloVeiculo, String status, LocalDateTime data,
                             LocalDateTime dataProximaRevisao, BigDecimal valorTotal) {
        this.id = id;
        this.cliente = cliente;
        this.veiculo = veiculo;
        this.placaVeiculo = placaVeiculo;
        this.modeloVeiculo = modeloVeiculo;
        this.status = status;
        this.data = data;
        this.dataProximaRevisao = dataProximaRevisao;
        this.valorTotal = valorTotal;
    }

    public Long getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public String getVeiculo() {
        return veiculo;
    }

    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public String getModeloVeiculo() {
        return modeloVeiculo;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getData() {
        return data;
    }

    public LocalDateTime getDataProximaRevisao() {
        return dataProximaRevisao;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }
}
