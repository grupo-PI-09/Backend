package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.ClienteDTORequest;
import oficina.mecanica.backendOficina.DTO.ClienteDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<ClienteDTOResponse> listar() {
        List<ClienteModel> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public ClienteDTOResponse buscarPorId(Long id) {
        Optional<ClienteModel> clienteOptional = clienteRepository.findById(id);

        if (clienteOptional.isEmpty()) {
            throw new RuntimeException("Cliente não encontrado");
        }

        return converterParaResponse(clienteOptional.get());
    }

    public List<ClienteDTOResponse> buscarPorNome(String nome) {
        List<ClienteModel> clientes = clienteRepository.findByNomeContainingIgnoreCase(nome);
        return clientes.stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public ClienteDTOResponse criar(ClienteDTORequest dto) {
        ClienteModel cliente = new ClienteModel();

        cliente.setNome(dto.getNome());
        cliente.setDtNascimento(dto.getDtNascimento());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEmail(dto.getEmail());
        cliente.setEndereco(dto.getEndereco());
        cliente.setAtivo(true);

        ClienteModel clienteSalvo = clienteRepository.save(cliente);
        return converterParaResponse(clienteSalvo);
    }

    public ClienteDTOResponse atualizar(Long id, ClienteDTORequest dto) {
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        cliente.setNome(dto.getNome());
        cliente.setDtNascimento(dto.getDtNascimento());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEmail(dto.getEmail());
        cliente.setEndereco(dto.getEndereco());

        ClienteModel clienteAtualizado = clienteRepository.save(cliente);
        return converterParaResponse(clienteAtualizado);
    }

    public void deletar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente não encontrado");
        }

        clienteRepository.deleteById(id);
    }

    private ClienteDTOResponse converterParaResponse(ClienteModel cliente) {
        return new ClienteDTOResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getDtNascimento(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getAtivo(),
                cliente.getDataCadastro()
        );
    }
}