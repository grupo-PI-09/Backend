package oficina.mecanica.backendOficina.DTO;

public class ApiErrorResponse {

    private boolean erro;
    private String mensagem;
    private String detalhes;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(boolean erro, String mensagem, String detalhes) {
        this.erro = erro;
        this.mensagem = mensagem;
        this.detalhes = detalhes;
    }

    public boolean isErro() {
        return erro;
    }

    public void setErro(boolean erro) {
        this.erro = erro;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }
}
