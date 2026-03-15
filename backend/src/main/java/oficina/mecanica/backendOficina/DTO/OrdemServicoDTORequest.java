package oficina.mecanica.backendOficina.DTO;

import java.util.List;

public class OrdemServicoDTORequest {

    private String cliente;
    private String codigoVeiculo;
    private String observacoesAvarias;
    private String descricaoReparo;
    private List<String> listaPecas;

    public OrdemServicoDTORequest() {
    }

    public OrdemServicoDTORequest(String cliente, String codigoVeiculo, String observacoesAvarias,
                                  String descricaoReparo, List<String> listaPecas) {
        this.cliente = cliente;
        this.codigoVeiculo = codigoVeiculo;
        this.observacoesAvarias = observacoesAvarias;
        this.descricaoReparo = descricaoReparo;
        this.listaPecas = listaPecas;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getCodigoVeiculo() {
        return codigoVeiculo;
    }

    public void setCodigoVeiculo(String codigoVeiculo) {
        this.codigoVeiculo = codigoVeiculo;
    }

    public String getObservacoesAvarias() {
        return observacoesAvarias;
    }

    public void setObservacoesAvarias(String observacoesAvarias) {
        this.observacoesAvarias = observacoesAvarias;
    }

    public String getDescricaoReparo() {
        return descricaoReparo;
    }

    public void setDescricaoReparo(String descricaoReparo) {
        this.descricaoReparo = descricaoReparo;
    }

    public List<String> getListaPecas() {
        return listaPecas;
    }

    public void setListaPecas(List<String> listaPecas) {
        this.listaPecas = listaPecas;
    }
}
