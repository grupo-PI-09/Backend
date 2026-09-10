package oficina.mecanica.backendOficina.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    /**
     * Senha e opcional na atualizacao: vazio/nulo mantem a senha atual.
     * Quando informada, segue a mesma politica do cadastro (OWASP A07).
     */
    @Pattern(
            regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,72}$",
            message = "Senha deve ter no mínimo 8 caracteres, com pelo menos uma letra e um número"
    )
    private String senha;

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
}
