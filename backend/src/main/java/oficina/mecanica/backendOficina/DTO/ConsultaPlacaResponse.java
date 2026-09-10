package oficina.mecanica.backendOficina.DTO;

/**
 * OWASP A04:2021 - minimizacao de dados.
 * O campo "raw", que repassava ao frontend a resposta bruta e completa da
 * APIBrasil (incluindo dados que a aplicacao nao utiliza), foi removido.
 * Apenas os campos efetivamente usados pelo sistema sao expostos.
 */
public class ConsultaPlacaResponse {

    private String placa;
    private String marca;
    private String modelo;
    private String ano;

    public ConsultaPlacaResponse() {
    }

    public ConsultaPlacaResponse(String placa, String marca, String modelo, String ano) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
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
}
