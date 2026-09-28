package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.AgendamentoDTORequest;
import oficina.mecanica.backendOficina.DTO.AgendamentoDTOResponse;
import oficina.mecanica.backendOficina.Model.AgendamentoModel;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.StatusAgendamento;
import oficina.mecanica.backendOficina.Model.TipoServico;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Repository.AgendamentoRepository;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              ClienteRepository clienteRepository,
                              VeiculoRepository veiculoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    public List<AgendamentoDTOResponse> listar() {
        return agendamentoRepository.findAllByOrderByDataHoraAsc()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public AgendamentoDTOResponse buscarPorId(Long id) {
        return converterParaResponse(buscarEntidadePorId(id));
    }

    public List<AgendamentoDTOResponse> buscarPorClienteId(Long clienteId) {
        return agendamentoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public List<AgendamentoDTOResponse> buscarPorVeiculoId(Long veiculoId) {
        return agendamentoRepository.findByVeiculoId(veiculoId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public AgendamentoDTOResponse criar(AgendamentoDTORequest dto) {
        ClienteModel cliente = buscarCliente(dto.getClienteId());
        VeiculoModel veiculo = buscarVeiculoDoCliente(dto.getVeiculoId(), cliente);

        AgendamentoModel agendamento = new AgendamentoModel();
        agendamento.setCliente(cliente);
        agendamento.setVeiculo(veiculo);
        aplicarDadosDto(agendamento, dto);

        return converterParaResponse(agendamentoRepository.save(agendamento));
    }

    public AgendamentoDTOResponse atualizar(Long id, AgendamentoDTORequest dto) {
        AgendamentoModel agendamento = buscarEntidadePorId(id);

        ClienteModel cliente = buscarCliente(dto.getClienteId());
        VeiculoModel veiculo = buscarVeiculoDoCliente(dto.getVeiculoId(), cliente);

        agendamento.setCliente(cliente);
        agendamento.setVeiculo(veiculo);
        aplicarDadosDto(agendamento, dto);

        return converterParaResponse(agendamentoRepository.save(agendamento));
    }

    public void deletar(Long id) {
        if (!agendamentoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado");
        }

        agendamentoRepository.deleteById(id);
    }

    private AgendamentoModel buscarEntidadePorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
    }

    private ClienteModel buscarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
    }

    private VeiculoModel buscarVeiculoDoCliente(Long veiculoId, ClienteModel cliente) {
        VeiculoModel veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));

        if (veiculo.getCliente() == null || !Objects.equals(veiculo.getCliente().getId(), cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O veículo informado não pertence ao cliente");
        }

        return veiculo;
    }

    private void aplicarDadosDto(AgendamentoModel agendamento, AgendamentoDTORequest dto) {
        agendamento.setUsuarioId(dto.getUsuarioId());
        agendamento.setDataHora(dto.getDataHora());
        agendamento.setDuracaoHoras(dto.getDuracaoHoras());
        agendamento.setTipoServico(parseTipoServico(dto.getTipoServico()));
        agendamento.setServico(dto.getServico());
        agendamento.setObservacoes(dto.getObservacoes());
        agendamento.setStatus(parseStatus(dto.getStatus()));
    }

    private TipoServico parseTipoServico(String tipoServico) {
        if (tipoServico == null || tipoServico.isBlank()) {
            return TipoServico.corretiva;
        }

        try {
            return TipoServico.valueOf(tipoServico.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de serviço inválido. Use 'preventiva' ou 'corretiva'.");
        }
    }

    private StatusAgendamento parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return StatusAgendamento.agendado;
        }

        try {
            return StatusAgendamento.valueOf(status.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status inválido. Use agendado, cancelado ou concluido.");
        }
    }

    private AgendamentoDTOResponse converterParaResponse(AgendamentoModel agendamento) {
        VeiculoModel veiculo = agendamento.getVeiculo();
        String veiculoLabel = veiculo.getModelo() + (veiculo.getPlaca() != null ? " (" + veiculo.getPlaca() + ")" : "");

        return new AgendamentoDTOResponse(
                agendamento.getId(),
                agendamento.getCliente().getId(),
                agendamento.getCliente().getNome(),
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculoLabel,
                agendamento.getUsuarioId(),
                agendamento.getDataHora(),
                agendamento.getDuracaoHoras(),
                agendamento.getTipoServico().name(),
                agendamento.getServico(),
                agendamento.getObservacoes(),
                agendamento.getStatus().name(),
                agendamento.getDataCriacao()
        );
    }
}
