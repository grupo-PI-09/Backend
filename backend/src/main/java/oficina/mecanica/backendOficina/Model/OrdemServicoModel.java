package oficina.mecanica.backendOficina.Model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordens_servico")
public class OrdemServicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ordens_servico")
    private Long id;

    @ManyToOne //Relacionamento** 1 cliente pode ter várias ordens
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteModel cliente;

    @ManyToOne //Relacionamento** 1 veículo pode ter várias ordens
    @JoinColumn(name = "veiculo_id", nullable = false)
    private VeiculoModel veiculo;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "data_abertura")
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusOrdemServico status;

    @Column(name = "problema_relatado", columnDefinition = "TEXT")
    private String problemaRelatado;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "quilometragem")
    private Integer quilometragem;

    @Column(name = "valor_estimado", precision = 10, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    public OrdemServicoModel() {
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(LocalDateTime dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public LocalDateTime getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(LocalDateTime dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public StatusOrdemServico getStatus() {
        return status;
    }

    public void setStatus(StatusOrdemServico status) {
        this.status = status;
    }

    public String getProblemaRelatado() {
        return problemaRelatado;
    }

    public void setProblemaRelatado(String problemaRelatado) {
        this.problemaRelatado = problemaRelatado;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public Integer getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(Integer quilometragem) {
        this.quilometragem = quilometragem;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}