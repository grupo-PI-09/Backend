package oficina.mecanica.backendOficina.DTO;

import java.time.LocalDateTime;

public class VeiculoDTOResponse {

    private Long id;
    private String placa;
    private String modelo;
    private String marca;
    private Integer ano;
    private Integer quilometragem;
    private String tipoCombustivel;
    private Boolean ativo;
    private LocalDateTime dataCriacao;

    private Long clienteId;
    private String nomeCliente;

    public VeiculoDTOResponse() {
    }

    public VeiculoDTOResponse(Long id, String placa, String modelo, String marca,
                              Integer ano, Integer quilometragem, String tipoCombustivel,
                              Boolean ativo, LocalDateTime dataCriacao,
                              Long clienteId, String nomeCliente) {
        this.id = id;
        this.placa = placa;
        this.modelo = modelo;
        this.marca = marca;
        this.ano = ano;
        this.quilometragem = quilometragem;
        this.tipoCombustivel = tipoCombustivel;
        this.ativo = ativo;
        this.dataCriacao = dataCriacao;
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(Integer quilometragem) {
        this.quilometragem = quilometragem;
    }

    public String getTipoCombustivel() {
        return tipoCombustivel;
    }

    public void setTipoCombustivel(String tipoCombustivel) {
        this.tipoCombustivel = tipoCombustivel;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }
}