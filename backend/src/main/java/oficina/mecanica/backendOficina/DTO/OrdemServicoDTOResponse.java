package oficina.mecanica.backendOficina.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private String tipoServico;
    private String problemaRelatado;
    private String diagnostico;
    private Integer quilometragem;
    private BigDecimal valorEstimado;
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String observacoes;
    private LocalDateTime dataProximaRevisao;
    private boolean mensagemFinalizacaoEnviada;
    private boolean lembreteRevisaoAgendado;
    private boolean lembreteRevisaoEnviadoImediatamente;
    private LocalDateTime dataAgendamentoRevisao;
    private List<String> avisos = new ArrayList<>();

    public OrdemServicoDTOResponse() {
    }

    public OrdemServicoDTOResponse(Long id, Long clienteId, String nomeCliente,
                                   Long veiculoId, String placaVeiculo,
                                   Long usuarioId, LocalDateTime dataAbertura,
                                   LocalDateTime dataFechamento, String status,
                                   String tipoServico, String problemaRelatado, String diagnostico,
                                   Integer quilometragem, BigDecimal valorEstimado,
                                   BigDecimal valorTotal, String formaPagamento,
                                   String observacoes, LocalDateTime dataProximaRevisao) {
        this.id = id;
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.veiculoId = veiculoId;
        this.placaVeiculo = placaVeiculo;
        this.usuarioId = usuarioId;
        this.dataAbertura = dataAbertura;
        this.dataFechamento = dataFechamento;
        this.status = status;
        this.tipoServico = tipoServico;
        this.problemaRelatado = problemaRelatado;
        this.diagnostico = diagnostico;
        this.quilometragem = quilometragem;
        this.valorEstimado = valorEstimado;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.observacoes = observacoes;
        this.dataProximaRevisao = dataProximaRevisao;
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
    public String getTipoServico() { return tipoServico; }
    public String getProblemaRelatado() { return problemaRelatado; }
    public String getDiagnostico() { return diagnostico; }
    public Integer getQuilometragem() { return quilometragem; }
    public BigDecimal getValorEstimado() { return valorEstimado; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public String getFormaPagamento() { return formaPagamento; }
    public String getObservacoes() { return observacoes; }
    public LocalDateTime getDataProximaRevisao() { return dataProximaRevisao; }
    public boolean isMensagemFinalizacaoEnviada() { return mensagemFinalizacaoEnviada; }
    public boolean isLembreteRevisaoAgendado() { return lembreteRevisaoAgendado; }
    public boolean isLembreteRevisaoEnviadoImediatamente() { return lembreteRevisaoEnviadoImediatamente; }
    public LocalDateTime getDataAgendamentoRevisao() { return dataAgendamentoRevisao; }
    public List<String> getAvisos() { return avisos; }

    public void setMensagemFinalizacaoEnviada(boolean mensagemFinalizacaoEnviada) {
        this.mensagemFinalizacaoEnviada = mensagemFinalizacaoEnviada;
    }

    public void setLembreteRevisaoAgendado(boolean lembreteRevisaoAgendado) {
        this.lembreteRevisaoAgendado = lembreteRevisaoAgendado;
    }

    public void setLembreteRevisaoEnviadoImediatamente(boolean lembreteRevisaoEnviadoImediatamente) {
        this.lembreteRevisaoEnviadoImediatamente = lembreteRevisaoEnviadoImediatamente;
    }

    public void setDataAgendamentoRevisao(LocalDateTime dataAgendamentoRevisao) {
        this.dataAgendamentoRevisao = dataAgendamentoRevisao;
    }

    public void adicionarAviso(String aviso) {
        if (aviso != null && !aviso.isBlank()) {
            this.avisos.add(aviso);
        }
    }
}
