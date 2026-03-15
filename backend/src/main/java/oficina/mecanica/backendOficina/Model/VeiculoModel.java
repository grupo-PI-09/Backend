package oficina.mecanica.backendOficina.Model;

public class VeiculoModel {
    private String modelo;
    private String marca;
    private String placa;
    private String ano;
    private String kmAtual;
    private String combustivel;

    public VeiculoModel() {

    }

    public VeiculoModel(String modelo, String marca, String placa, String ano, String kmAtual, String combustivel) {
        this.modelo = modelo;
        this.marca = marca;
        this.placa = placa;
        this.ano = ano;
        this.kmAtual = kmAtual;
        this.combustivel = combustivel;
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

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(String kmAtual) {
        this.kmAtual = kmAtual;
    }

    public String getCombustivel() {
        return combustivel;
    }

    public void setCombustivel(String combustivel) {
        this.combustivel = combustivel;
    }
}
