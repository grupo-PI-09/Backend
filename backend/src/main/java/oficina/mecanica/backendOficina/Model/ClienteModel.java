package oficina.mecanica.backendOficina.Model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
public class ClienteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clientes")
    private Long id;

    @Column(name = "nome_usuario", length = 50)
    private String nome;

    @Column(name = "dt_nascimento")
    private LocalDate dtNascimento;

    @Column(name = "telefone", length = 11)
    private String telefone;

    @Column(name = "email_usuario", length = 100)
    private String email;

    @Column(name = "endereco", length = 100)
    private String endereco;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    public ClienteModel() {
    }

    public ClienteModel(Long id, String nome, LocalDate dtNascimento, String telefone,
                        String email, String endereco, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.dtNascimento = dtNascimento;
        this.telefone = telefone;
        this.email = email;
        this.endereco = endereco;
        this.ativo = ativo;
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

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}