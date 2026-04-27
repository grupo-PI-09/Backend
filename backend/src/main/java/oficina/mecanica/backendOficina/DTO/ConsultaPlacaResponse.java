package oficina.mecanica.backendOficina.DTO;

import com.fasterxml.jackson.databind.JsonNode;

public class ConsultaPlacaResponse {

    private String placa;
    private String marca;
    private String modelo;
    private String ano;
    private JsonNode raw;

    public ConsultaPlacaResponse() {
    }

    public ConsultaPlacaResponse(String placa, String marca, String modelo, String ano, JsonNode raw) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.raw = raw;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public JsonNode getRaw() {
        return raw;
    }

    public void setRaw(JsonNode raw) {
        this.raw = raw;
    }
}
