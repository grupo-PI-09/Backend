package oficina.mecanica.backendOficina.DTO;
import java.util.List;

public class ClienteDTOResponse {
    Long id;
    String nome;
    String email;
    String telefone;
    List<VeiculoDTO> veiculos;

    public ClienteDTOResponse() {

    }

    public ClienteDTOResponse(Long id, String nome, String email, String telefone, List<VeiculoDTO> veiculos) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.veiculos = veiculos;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public List<VeiculoDTO> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(List<VeiculoDTO> veiculos) {
        this.veiculos = veiculos;
    }
}
