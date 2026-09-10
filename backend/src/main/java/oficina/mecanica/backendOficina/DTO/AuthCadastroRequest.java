package oficina.mecanica.backendOficina.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthCadastroRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    // OWASP A07: politica minima de senha (8+ caracteres, com letra e numero).
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
            message = "Senha deve ter no mínimo 8 caracteres, com pelo menos uma letra e um número"
    )
    private String senha;

    /**
     * OWASP A01: o perfil e opcional e, quando ausente, o usuario recebe o
     * menor privilegio ("mecanico"). Somente um ADMIN autenticado pode chamar
     * este cadastro e, portanto, conceder o perfil "admin".
     */
    @Pattern(regexp = "^(admin|mecanico)?$", message = "Perfil deve ser admin ou mecanico")
    private String perfil;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
}
