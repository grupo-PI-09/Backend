package oficina.mecanica.backendOficina.DTO;

import java.time.LocalDateTime;

public class NotificacaoDTOResponse {

    private Long id;
    private Long clienteId;
    private String cliente;
    private String telefone;
    private Long veiculoId;
    private String veiculo;
    private String placa;
    private Long ordemServicoId;
    private String statusOs;
    private String tipo;
    private String assunto;
    private String mensagem;
    private String canal;
    private String status;
    private boolean lida;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAgendamento;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataProximaRevisao;

    public NotificacaoDTOResponse() {
    }

    public NotificacaoDTOResponse(Long id, Long clienteId, String cliente, String telefone,
                                  Long veiculoId, String veiculo, String placa,
                                  Long ordemServicoId, String statusOs, String tipo,
                                  String assunto, String mensagem, String canal, String status,
                                  boolean lida, LocalDateTime dataCriacao, LocalDateTime dataAgendamento,
                                  LocalDateTime dataEnvio, LocalDateTime dataProximaRevisao) {
        this.id = id;
        this.clienteId = clienteId;
        this.cliente = cliente;
        this.telefone = telefone;
        this.veiculoId = veiculoId;
        this.veiculo = veiculo;
        this.placa = placa;
        this.ordemServicoId = ordemServicoId;
        this.statusOs = statusOs;
        this.tipo = tipo;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.canal = canal;
        this.status = status;
        this.lida = lida;
        this.dataCriacao = dataCriacao;
        this.dataAgendamento = dataAgendamento;
        this.dataEnvio = dataEnvio;
        this.dataProximaRevisao = dataProximaRevisao;
    }

    public Long getId() { return id; }

    public Long getClienteId() { return clienteId; }

    public String getCliente() { return cliente; }

    public String getTelefone() { return telefone; }

    public Long getVeiculoId() { return veiculoId; }

    public String getVeiculo() { return veiculo; }

    public String getPlaca() { return placa; }

    public Long getOrdemServicoId() { return ordemServicoId; }

    public String getStatusOs() { return statusOs; }

    public String getTipo() { return tipo; }

    public String getAssunto() { return assunto; }

    public String getMensagem() { return mensagem; }

    public String getCanal() { return canal; }

    public String getStatus() { return status; }

    public boolean isLida() { return lida; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }

    public LocalDateTime getDataAgendamento() { return dataAgendamento; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }

    public LocalDateTime getDataProximaRevisao() { return dataProximaRevisao; }
}
