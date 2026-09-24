package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.ClienteDTORequest;
import oficina.mecanica.backendOficina.DTO.ClienteDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        String cpf = validarCpf(dto.getCpf());
        if (clienteRepository.existsByCpf(cpf)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }

        ClienteModel cliente = new ClienteModel();

        cliente.setNome(dto.getNome().trim());
        cliente.setCpf(cpf);
        cliente.setDtNascimento(dto.getDtNascimento());
        cliente.setTelefone(validarTelefone(dto.getTelefone()));
        cliente.setEmail(dto.getEmail());
        preencherEndereco(cliente, dto);
        cliente.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

        ClienteModel clienteSalvo = clienteRepository.save(cliente);
        return converterParaResponse(clienteSalvo);
    }

    public ClienteDTOResponse atualizar(Long id, ClienteDTORequest dto) {
        ClienteModel cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        String cpf = validarCpf(dto.getCpf());
        if (clienteRepository.existsByCpfAndIdNot(cpf, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }

        cliente.setNome(dto.getNome().trim());
        cliente.setCpf(cpf);
        cliente.setDtNascimento(dto.getDtNascimento());
        cliente.setTelefone(validarTelefone(dto.getTelefone()));
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

        try {
            clienteRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cliente possui veículos, ordens de serviço ou notificações e não pode ser excluído. "
                            + "Desative o cliente em vez de excluí-lo.");
        }
    }

    private String validarCpf(String cpfInformado) {
        String cpf = normalizarApenasDigitos(cpfInformado);

        if (cpf == null || !cpfValido(cpf)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido");
        }

        return cpf;
    }

    /** Confere os dois digitos verificadores do CPF. */
    private boolean cpfValido(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }

        for (int posicao = 9; posicao <= 10; posicao++) {
            int soma = 0;
            for (int i = 0; i < posicao; i++) {
                soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
            }
            int digito = (soma * 10) % 11 % 10;
            if (digito != cpf.charAt(posicao) - '0') {
                return false;
            }
        }

        return true;
    }

    /** Guarda so os digitos: a coluna tem 11 posicoes (DDD + numero). */
    private String validarTelefone(String telefoneInformado) {
        String telefone = normalizarApenasDigitos(telefoneInformado);

        if (telefone != null && telefone.length() > 11 && telefone.startsWith("55")) {
            telefone = telefone.substring(2);
        }

        if (telefone == null || telefone.length() < 10 || telefone.length() > 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Telefone deve conter DDD e número (10 ou 11 dígitos)");
        }

        return telefone;
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
