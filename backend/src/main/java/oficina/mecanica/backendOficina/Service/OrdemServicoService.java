package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.OrdemServicoDTORequest;
import oficina.mecanica.backendOficina.DTO.OrdemServicoDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.TipoServico;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.OrdemServicoRepository;
import oficina.mecanica.backendOficina.Repository.VeiculoRepository;
import oficina.mecanica.backendOficina.notificacao.infra.OrdemParaNotificarMapper;
import oficina.mecanica.backendOficina.notificacao.usecase.AgendarLembreteRevisaoUseCase;
import oficina.mecanica.backendOficina.notificacao.usecase.NotificarFinalizacaoOrdemUseCase;
import oficina.mecanica.backendOficina.notificacao.usecase.OrdemParaNotificar;
import oficina.mecanica.backendOficina.notificacao.usecase.ResultadoNotificacao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final NotificarFinalizacaoOrdemUseCase notificarFinalizacao;
    private final AgendarLembreteRevisaoUseCase agendarLembreteRevisao;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
                               ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository,
                               NotificarFinalizacaoOrdemUseCase notificarFinalizacao,
                               AgendarLembreteRevisaoUseCase agendarLembreteRevisao) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.notificarFinalizacao = notificarFinalizacao;
        this.agendarLembreteRevisao = agendarLembreteRevisao;
    }

    public List<OrdemServicoDTOResponse> listar() {
        return ordemServicoRepository.findAllByOrderByDataAberturaDescIdDesc()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public OrdemServicoDTOResponse buscarPorId(Long id) {
        OrdemServicoModel ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada"));

        return converterParaResponse(ordemServico);
    }

    public List<OrdemServicoDTOResponse> buscarPorClienteId(Long clienteId) {
        return ordemServicoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }


    public List<OrdemServicoDTOResponse> buscarPorVeiculoId(Long veiculoId) {
        return ordemServicoRepository.findByVeiculoId(veiculoId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    @Transactional
    public OrdemServicoDTOResponse criar(OrdemServicoDTORequest dto) {
        ClienteModel cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        VeiculoModel veiculo = veiculoRepository.findById(dto.getVeiculoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));

        OrdemServicoModel ordemServico = new OrdemServicoModel();
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        aplicarDadosDto(ordemServico, dto);
        sincronizarQuilometragemVeiculo(veiculo, dto.getQuilometragem());

        OrdemServicoModel salva = ordemServicoRepository.save(ordemServico);
        return converterParaResponse(salva);
    }

    @Transactional
    public OrdemServicoDTOResponse atualizar(Long id, OrdemServicoDTORequest dto) {
        OrdemServicoModel ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada"));

        ClienteModel cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        VeiculoModel veiculo = veiculoRepository.findById(dto.getVeiculoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));

        boolean deveNotificarFinalizacao = ordemServico.getStatus() != StatusOrdemServico.finalizada
                && parseStatus(dto.getStatus()) == StatusOrdemServico.finalizada;

        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        aplicarDadosDto(ordemServico, dto);
        sincronizarQuilometragemVeiculo(veiculo, dto.getQuilometragem());

        OrdemServicoModel atualizada = ordemServicoRepository.save(ordemServico);
        OrdemServicoDTOResponse response = converterParaResponse(atualizada);

        if (deveNotificarFinalizacao) {
            adicionarResultadoNotificacoesFinalizacao(atualizada, response);
        }

        return response;
    }

    private void aplicarDadosDto(OrdemServicoModel ordemServico, OrdemServicoDTORequest dto) {
        StatusOrdemServico status = parseStatus(dto.getStatus());

        ordemServico.setUsuarioId(dto.getUsuarioId());
        ordemServico.setStatus(status);
        ordemServico.setTipoServico(parseTipoServico(dto.getTipoServico()));
        ordemServico.setProblemaRelatado(dto.getProblemaRelatado());
        ordemServico.setDiagnostico(dto.getDiagnostico());
        ordemServico.setQuilometragem(dto.getQuilometragem());
        ordemServico.setValorEstimado(dto.getValorEstimado() != null ? dto.getValorEstimado() : BigDecimal.ZERO);
        ordemServico.setValorTotal(dto.getValorTotal() != null ? dto.getValorTotal() : BigDecimal.ZERO);
        ordemServico.setFormaPagamento(dto.getFormaPagamento());
        ordemServico.setObservacoes(dto.getObservacoes());
        ordemServico.setDataProximaRevisao(dto.getDataProximaRevisao());

        if (status == StatusOrdemServico.finalizada) {
            ordemServico.setDataFechamento(dto.getDataFechamento() != null ? dto.getDataFechamento() : LocalDateTime.now());
        } else {
            ordemServico.setDataFechamento(dto.getDataFechamento());
        }
    }

    private StatusOrdemServico parseStatus(String status) {
        return StatusOrdemServico.valueOf(status.toLowerCase());
    }

    private TipoServico parseTipoServico(String tipoServico) {
        if (tipoServico == null || tipoServico.isBlank()) {
            return TipoServico.corretiva;
        }

        try {
            return TipoServico.valueOf(tipoServico.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de serviço inválido. Use 'preventiva' ou 'corretiva'.");
        }
    }

    private void sincronizarQuilometragemVeiculo(VeiculoModel veiculo, Integer quilometragem) {
        if (veiculo == null || quilometragem == null) {
            return;
        }

        veiculo.setQuilometragem(quilometragem);
        veiculoRepository.save(veiculo);
    }

    private void adicionarResultadoNotificacoesFinalizacao(OrdemServicoModel ordemServico,
                                                           OrdemServicoDTOResponse response) {
        OrdemParaNotificar ordem = OrdemParaNotificarMapper.de(ordemServico);

        ResultadoNotificacao finalizacao = notificarFinalizacao.executar(ordem);
        response.setMensagemFinalizacaoEnviada(finalizacao.enviada());
        response.adicionarAviso(finalizacao.aviso());

        if (ordemServico.getDataProximaRevisao() == null) {
            return;
        }

        ResultadoNotificacao revisao = agendarLembreteRevisao.executar(ordem);
        response.setLembreteRevisaoAgendado(revisao.agendada());
        response.setLembreteRevisaoEnviadoImediatamente(revisao.enviadaImediatamente());
        response.setDataAgendamentoRevisao(revisao.dataAgendamento());
        response.adicionarAviso(revisao.aviso());
    }

    public void deletar(Long id) {
        if (!ordemServicoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada");
        }

        ordemServicoRepository.deleteById(id);
    }

    private OrdemServicoDTOResponse converterParaResponse(OrdemServicoModel ordemServico) {
        return new OrdemServicoDTOResponse(
                ordemServico.getId(),
                ordemServico.getCliente().getId(),
                ordemServico.getCliente().getNome(),
                ordemServico.getVeiculo().getId(),
                ordemServico.getVeiculo().getPlaca(),
                ordemServico.getUsuarioId(),
                ordemServico.getDataAbertura(),
                ordemServico.getDataFechamento(),
                ordemServico.getStatus().name(),
                ordemServico.getTipoServico() != null ? ordemServico.getTipoServico().name() : TipoServico.corretiva.name(),
                ordemServico.getProblemaRelatado(),
                ordemServico.getDiagnostico(),
                ordemServico.getQuilometragem(),
                ordemServico.getValorEstimado(),
                ordemServico.getValorTotal(),
                ordemServico.getFormaPagamento(),
                ordemServico.getObservacoes(),
                ordemServico.getDataProximaRevisao()
        );
    }
}
