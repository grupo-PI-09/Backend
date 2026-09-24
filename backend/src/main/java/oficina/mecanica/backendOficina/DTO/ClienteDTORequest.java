package oficina.mecanica.backendOficina.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ClienteDTORequest {

    // Os limites de tamanho acompanham as colunas de ClienteModel: sem eles um
    // texto maior estourava a coluna no banco e virava erro 500.
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$", message = "CPF deve conter 11 números")
    private String cpf;

    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate dtNascimento;

    // Aceita formatado, ex.: (11) 98765-4321. O service guarda apenas os digitos.
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^[\\d\\s()+-]{10,20}$", message = "Telefone inválido")
    private String telefone;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
    private String email;

    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    private String endereco;

    @Pattern(regexp = "^(\\d{8}|\\d{5}-\\d{3})?$", message = "CEP deve conter 8 números")
    private String cep;

    @Size(max = 120, message = "Logradouro deve ter no máximo 120 caracteres")
    private String logradouro;

    @Size(max = 10, message = "Número deve ter no máximo 10 caracteres")
    private String numero;

    @Size(max = 60, message = "Complemento deve ter no máximo 60 caracteres")
    private String complemento;

    @Size(max = 80, message = "Bairro deve ter no máximo 80 caracteres")
    private String bairro;

    @Size(max = 80, message = "Cidade deve ter no máximo 80 caracteres")
    private String cidade;

    @Pattern(regexp = "^([A-Za-z]{2})?$", message = "Estado deve conter a UF com 2 letras")
    private String estado;

    private Boolean ativo;

    public ClienteDTORequest() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
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

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
