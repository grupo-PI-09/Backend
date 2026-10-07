package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.ClienteComVeiculosDTOResponse;
import oficina.mecanica.backendOficina.DTO.ClienteDTORequest;
import oficina.mecanica.backendOficina.DTO.ClienteDTOResponse;
import oficina.mecanica.backendOficina.DTO.PaginaOffsetResponse;
import oficina.mecanica.backendOficina.DTO.VeiculoDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Paginacao.PaginacaoOffset;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VeiculoService veiculoService;

    public ClienteService(ClienteRepository clienteRepository, VeiculoService veiculoService) {
        this.clienteRepository = clienteRepository;
        this.veiculoService = veiculoService;
    }

    public List<ClienteDTOResponse> listar() {
        List<ClienteModel> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public PaginaOffsetResponse<ClienteComVeiculosDTOResponse> listarPaginado(int pagina, int tamanho, String busca) {
        Pageable pageable = PaginacaoOffset.criar(pagina, tamanho, Sort.by(Sort.Direction.ASC, "id"));
        String termo = busca == null ? "" : busca.trim();

        Page<ClienteModel> resultado = termo.isEmpty()
                ? clienteRepository.findAll(pageable)
                : clienteRepository.findByNomeContainingIgnoreCase(termo, pageable);

        List<Long> ids = resultado.getContent().stream().map(ClienteModel::getId).toList();
        Map<Long, List<VeiculoDTOResponse>> veiculosPorCliente = veiculoService.buscarPorClienteIds(ids);

        return PaginaOffsetResponse.de(resultado, resultado.getContent().stream()
                .map(cliente -> new ClienteComVeiculosDTOResponse(
                        converterParaResponse(cliente),
                        veiculosPorCliente.getOrDefault(cliente.getId(), List.of())))
                .toList());
    }

    public ClienteDTOResponse buscarPorId(Long id) {
        Optional<ClienteModel> clienteOptional = clienteRepository.findById(id);

        if (clienteOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado");
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
        cliente.setCpf(normalizarApenasDigitos(dto.getCpf()));
        cliente.setDtNascimento(dto.getDtNascimento());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEmail(dto.getEmail());
        preencherEndereco(cliente, dto);
        cliente.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

        ClienteModel clienteSalvo = clienteRepository.save(cliente);
        return converterParaResponse(clienteSalvo);
    }

    public ClienteDTOResponse atualizar(Long id, ClienteDTORequest dto) {
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        cliente.setNome(dto.getNome());
        cliente.setCpf(normalizarApenasDigitos(dto.getCpf()));
        cliente.setDtNascimento(dto.getDtNascimento());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEmail(dto.getEmail());
        preencherEndereco(cliente, dto);
        if (dto.getAtivo() != null) {
            cliente.setAtivo(dto.getAtivo());
        }

        ClienteModel clienteAtualizado = clienteRepository.save(cliente);
        return converterParaResponse(clienteAtualizado);
    }

    public void deletar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado");
        }

        clienteRepository.deleteById(id);
    }

    private ClienteDTOResponse converterParaResponse(ClienteModel cliente) {
        return new ClienteDTOResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getDtNascimento(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.getCep(),
                cliente.getLogradouro(),
                cliente.getNumero(),
                cliente.getComplemento(),
                cliente.getBairro(),
                cliente.getCidade(),
                cliente.getEstado(),
                cliente.getAtivo(),
                cliente.getDataCadastro()
        );
    }

    private void preencherEndereco(ClienteModel cliente, ClienteDTORequest dto) {
        cliente.setCep(normalizarApenasDigitos(dto.getCep()));
        cliente.setLogradouro(dto.getLogradouro());
        cliente.setNumero(dto.getNumero());
        cliente.setComplemento(dto.getComplemento());
        cliente.setBairro(dto.getBairro());
        cliente.setCidade(dto.getCidade());
        cliente.setEstado(dto.getEstado() == null ? null : dto.getEstado().toUpperCase());
        cliente.setEndereco(montarEndereco(dto));
    }

    private String montarEndereco(ClienteDTORequest dto) {
        if (dto.getEndereco() != null && !dto.getEndereco().isBlank()) {
            return dto.getEndereco();
        }

        StringBuilder endereco = new StringBuilder();
        adicionarParteEndereco(endereco, dto.getLogradouro());
        adicionarParteEndereco(endereco, dto.getNumero());
        adicionarParteEndereco(endereco, dto.getComplemento());
        adicionarParteEndereco(endereco, dto.getBairro());
        adicionarParteEndereco(endereco, dto.getCidade());
        adicionarParteEndereco(endereco, dto.getEstado());

        return endereco.isEmpty() ? null : endereco.toString();
    }

    private void adicionarParteEndereco(StringBuilder endereco, String parte) {
        if (parte == null || parte.isBlank()) {
            return;
        }

        if (!endereco.isEmpty()) {
            endereco.append(", ");
        }

        endereco.append(parte.trim());
    }

    private String normalizarApenasDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}
