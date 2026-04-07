package oficina.mecanica.backendOficina.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClienteDTOResponse {

    private Long id;
    private String nome;
    private LocalDate dtNascimento;
    private String telefone;
    private String email;
    private String endereco;
    private Boolean ativo;
    private LocalDateTime dataCadastro;

    public ClienteDTOResponse() {
    }

    public ClienteDTOResponse(Long id, String nome, LocalDate dtNascimento,
                              String telefone, String email, String endereco,
                              Boolean ativo, LocalDateTime dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.dtNascimento = dtNascimento;
        this.telefone = telefone;
        this.email = email;
        this.endereco = endereco;
        this.ativo = ativo;
        this.dataCadastro = dataCadastro;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDtNascimento() {
        return dtNascimento;
    }

    public void setDtNascimento(LocalDate dtNascimento) {
        this.dtNascimento = dtNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}