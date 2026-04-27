package oficina.mecanica.backendOficina.DTO;

public class AuthResponse {

    private String token;
    private UsuarioAuthResponse usuario;

    public AuthResponse() {
    }

    public AuthResponse(String token, UsuarioAuthResponse usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuarioAuthResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioAuthResponse usuario) {
        this.usuario = usuario;
    }
}
