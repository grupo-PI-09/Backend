package oficina.mecanica.backendOficina.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class VeiculoDTORequest {

    @NotBlank(message = "Placa é obrigatória")
    private String placa;

    @NotBlank(message = "Modelo é obrigatório")
    private String modelo;

    @NotBlank(message = "Marca é obrigatória")
    private String marca;

    @NotNull(message = "Ano é obrigatório")
    @Positive(message = "Ano deve ser positivo")
    private Integer ano;

    @NotNull(message = "Quilometragem é obrigatória")
    @Positive(message = "Quilometragem deve ser positiva")
    private Integer quilometragem;

    @NotBlank(message = "Tipo de combustível é obrigatório")
    private String tipoCombustivel;

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    public VeiculoDTORequest() {
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

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}