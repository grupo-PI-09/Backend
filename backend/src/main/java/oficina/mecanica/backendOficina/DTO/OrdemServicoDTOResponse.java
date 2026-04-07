package oficina.mecanica.backendOficina.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrdemServicoDTOResponse {

    private Long id;
    private Long clienteId;
    private String nomeCliente;
    private Long veiculoId;
    private String placaVeiculo;
    private Long usuarioId;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String status;
    private String problemaRelatado;
    private String diagnostico;
    private Integer quilometragem;
    private BigDecimal valorEstimado;
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String observacoes;

    public OrdemServicoDTOResponse() {
    }

    public OrdemServicoDTOResponse(Long id, Long clienteId, String nomeCliente,
                                   Long veiculoId, String placaVeiculo,
                                   Long usuarioId, LocalDateTime dataAbertura,
                                   LocalDateTime dataFechamento, String status,
                                   String problemaRelatado, String diagnostico,
                                   Integer quilometragem, BigDecimal valorEstimado,
                                   BigDecimal valorTotal, String formaPagamento,
                                   String observacoes) {
        this.id = id;
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.veiculoId = veiculoId;
        this.placaVeiculo = placaVeiculo;
        this.usuarioId = usuarioId;
        this.dataAbertura = dataAbertura;
        this.dataFechamento = dataFechamento;
        this.status = status;
        this.problemaRelatado = problemaRelatado;
        this.diagnostico = diagnostico;
        this.quilometragem = quilometragem;
        this.valorEstimado = valorEstimado;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.observacoes = observacoes;
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public Long getVeiculoId() { return veiculoId; }
    public String getPlacaVeiculo() { return placaVeiculo; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public String getStatus() { return status; }
    public String getProblemaRelatado() { return problemaRelatado; }
    public String getDiagnostico() { return diagnostico; }
    public Integer getQuilometragem() { return quilometragem; }
    public BigDecimal getValorEstimado() { return valorEstimado; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public String getFormaPagamento() { return formaPagamento; }
    public String getObservacoes() { return observacoes; }
}