package oficina.mecanica.backendOficina.DTO;

import java.time.LocalDateTime;

public class AgendamentoDTOResponse {

    private Long id;
    private Long clienteId;
    private String nomeCliente;
    private Long veiculoId;
    private String placaVeiculo;
    private String veiculoLabel;
    private Long usuarioId;
    private LocalDateTime dataHora;
    private Integer duracaoHoras;
    private String tipoServico;
    private String servico;
    private String observacoes;
    private String status;
    private LocalDateTime dataCriacao;

    public AgendamentoDTOResponse() {
    }

    public AgendamentoDTOResponse(Long id, Long clienteId, String nomeCliente,
                                   Long veiculoId, String placaVeiculo, String veiculoLabel,
                                   Long usuarioId, LocalDateTime dataHora, Integer duracaoHoras,
                                   String tipoServico, String servico, String observacoes,
                                   String status, LocalDateTime dataCriacao) {
        this.id = id;
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.veiculoId = veiculoId;
        this.placaVeiculo = placaVeiculo;
        this.veiculoLabel = veiculoLabel;
        this.usuarioId = usuarioId;
        this.dataHora = dataHora;
        this.duracaoHoras = duracaoHoras;
        this.tipoServico = tipoServico;
        this.servico = servico;
        this.observacoes = observacoes;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public Long getVeiculoId() { return veiculoId; }
    public String getPlacaVeiculo() { return placaVeiculo; }
    public String getVeiculoLabel() { return veiculoLabel; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public Integer getDuracaoHoras() { return duracaoHoras; }
    public String getTipoServico() { return tipoServico; }
    public String getServico() { return servico; }
    public String getObservacoes() { return observacoes; }
    public String getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
}
