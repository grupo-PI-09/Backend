package oficina.mecanica.backendOficina.DTO;

import java.util.List;

/** Cliente já acompanhado dos seus veículos, usado na listagem paginada da tela de clientes. */
public class ClienteComVeiculosDTOResponse extends ClienteDTOResponse {

    private List<VeiculoDTOResponse> veiculos;

    public ClienteComVeiculosDTOResponse() {
    }

    public ClienteComVeiculosDTOResponse(ClienteDTOResponse cliente, List<VeiculoDTOResponse> veiculos) {
        super(cliente.getId(), cliente.getNome(), cliente.getCpf(), cliente.getDtNascimento(),
                cliente.getTelefone(), cliente.getEmail(), cliente.getEndereco(), cliente.getCep(),
                cliente.getLogradouro(), cliente.getNumero(), cliente.getComplemento(), cliente.getBairro(),
                cliente.getCidade(), cliente.getEstado(), cliente.getAtivo(), cliente.getDataCadastro());
        this.veiculos = veiculos;
    }

    public List<VeiculoDTOResponse> getVeiculos() {
        return veiculos;
    }

    public void setVeiculos(List<VeiculoDTOResponse> veiculos) {
        this.veiculos = veiculos;
    }
}
