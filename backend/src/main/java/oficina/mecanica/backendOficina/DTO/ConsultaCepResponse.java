package oficina.mecanica.backendOficina.DTO;

/**
 * OWASP A04:2021 - minimizacao de dados.
 * O campo "raw", que repassava ao frontend a resposta bruta e completa do
 * ViaCEP, foi removido: apenas os campos usados pelo cadastro sao expostos.
 */
public class ConsultaCepResponse {

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    public ConsultaCepResponse() {
    }

    public ConsultaCepResponse(String cep, String logradouro, String complemento,
                               String bairro, String cidade, String estado) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
