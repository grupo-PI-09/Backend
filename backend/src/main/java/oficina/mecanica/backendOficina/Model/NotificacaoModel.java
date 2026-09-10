package oficina.mecanica.backendOficina.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
public class NotificacaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacoes")
    private Long id;

    @ManyToOne //Relacionamento** 1 cliente pode receber várias notificações
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteModel cliente;

    @ManyToOne //Relacionamento** 1 veículo pode gerar várias notificações
    @JoinColumn(name = "veiculo_id")
    private VeiculoModel veiculo;

    @ManyToOne //Relacionamento** 1 ordem de serviço pode gerar várias notificações
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServicoModel ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoNotificacao tipo;

    @Column(name = "assunto", length = 150, nullable = false)
    private String assunto;

    @Column(name = "mensagem", columnDefinition = "TEXT", nullable = false)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false)
    private CanalNotificacao canal = CanalNotificacao.whatsapp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusNotificacao status = StatusNotificacao.pendente;

    @Column(name = "telefone_destino", length = 30)
    private String telefoneDestino;

    @Column(name = "lida", nullable = false)
    private Boolean lida = false;

    @CreationTimestamp
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_agendamento")
    private LocalDateTime dataAgendamento;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    public NotificacaoModel() {
    }

    public Long getId() {
        return id;
    }

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    public VeiculoModel getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(VeiculoModel veiculo) {
        this.veiculo = veiculo;
    }

    public OrdemServicoModel getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServicoModel ordemServico) {
        this.ordemServico = ordemServico;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public CanalNotificacao getCanal() {
        return canal;
    }

    public void setCanal(CanalNotificacao canal) {
        this.canal = canal;
    }

    public StatusNotificacao getStatus() {
        return status;
    }

    public void setStatus(StatusNotificacao status) {
        this.status = status;
    }

    public String getTelefoneDestino() {
        return telefoneDestino;
    }

    public void setTelefoneDestino(String telefoneDestino) {
        this.telefoneDestino = telefoneDestino;
    }

    public Boolean getLida() {
        return lida != null && lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida != null && lida;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(LocalDateTime dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}
