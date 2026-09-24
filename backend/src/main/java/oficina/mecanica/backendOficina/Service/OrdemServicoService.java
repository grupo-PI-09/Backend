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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class OrdemServicoService {

    private static final Logger log = LoggerFactory.getLogger(OrdemServicoService.class);

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final NotificarFinalizacaoOrdemUseCase notificarFinalizacao;
    private final AgendarLembreteRevisaoUseCase agendarLembreteRevisao;
    private final TransactionTemplate transactionTemplate;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
                               ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository,
                               NotificarFinalizacaoOrdemUseCase notificarFinalizacao,
                               AgendarLembreteRevisaoUseCase agendarLembreteRevisao,
                               PlatformTransactionManager transactionManager) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.notificarFinalizacao = notificarFinalizacao;
        this.agendarLembreteRevisao = agendarLembreteRevisao;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
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

    public OrdemServicoDTOResponse criar(OrdemServicoDTORequest dto) {
        OrdemSalva salva = transactionTemplate.execute(status -> criarNaTransacao(dto));
        notificarAposSalvar(salva);
        return salva.response();
    }

    public OrdemServicoDTOResponse atualizar(Long id, OrdemServicoDTORequest dto) {
        OrdemSalva salva = transactionTemplate.execute(status -> atualizarNaTransacao(id, dto));
        notificarAposSalvar(salva);
        return salva.response();
    }

    private OrdemSalva criarNaTransacao(OrdemServicoDTORequest dto) {
        ClienteModel cliente = buscarCliente(dto.getClienteId());
        VeiculoModel veiculo = buscarVeiculoDoCliente(dto.getVeiculoId(), cliente);

        OrdemServicoModel ordemServico = new OrdemServicoModel();
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        aplicarDadosDto(ordemServico, dto);
        sincronizarQuilometragemVeiculo(veiculo, dto.getQuilometragem());

        OrdemServicoModel salva = ordemServicoRepository.save(ordemServico);

        // Uma OS ja criada como finalizada tambem avisa o cliente.
        AcaoNotificacao acao = salva.getStatus() == StatusOrdemServico.finalizada
                ? AcaoNotificacao.FINALIZACAO
                : AcaoNotificacao.NENHUMA;

        return new OrdemSalva(converterParaResponse(salva), OrdemParaNotificarMapper.de(salva), acao);
    }

    private OrdemSalva atualizarNaTransacao(Long id, OrdemServicoDTORequest dto) {
        OrdemServicoModel ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço não encontrada"));

        ClienteModel cliente = buscarCliente(dto.getClienteId());
        VeiculoModel veiculo = buscarVeiculoDoCliente(dto.getVeiculoId(), cliente);

        boolean estavaFinalizada = ordemServico.getStatus() == StatusOrdemServico.finalizada;
        LocalDateTime revisaoAnterior = ordemServico.getDataProximaRevisao();

        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        aplicarDadosDto(ordemServico, dto);
        sincronizarQuilometragemVeiculo(veiculo, dto.getQuilometragem());

        OrdemServicoModel atualizada = ordemServicoRepository.save(ordemServico);
        boolean ficouFinalizada = atualizada.getStatus() == StatusOrdemServico.finalizada;

        AcaoNotificacao acao = AcaoNotificacao.NENHUMA;
        if (!estavaFinalizada && ficouFinalizada) {
            acao = AcaoNotificacao.FINALIZACAO;
        } else if (estavaFinalizada && !ficouFinalizada) {
            acao = AcaoNotificacao.CANCELAR_REVISAO;
        } else if (ficouFinalizada && !Objects.equals(revisaoAnterior, atualizada.getDataProximaRevisao())) {
            acao = AcaoNotificacao.REAGENDAR_REVISAO;
        }

        return new OrdemSalva(converterParaResponse(atualizada), OrdemParaNotificarMapper.de(atualizada), acao);
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
            if (dto.getDataFechamento() != null) {
                ordemServico.setDataFechamento(dto.getDataFechamento());
            } else if (ordemServico.getDataFechamento() == null) {
                ordemServico.setDataFechamento(LocalDateTime.now());
            }
        } else {
            ordemServico.setDataFechamento(dto.getDataFechamento());
        }
    }

    private StatusOrdemServico parseStatus(String status) {
        try {
            return StatusOrdemServico.valueOf(status.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Status inválido. Use aberta, em_andamento, aguardando_aprovacao, "
                            + "aguardando_peca, finalizada ou cancelada.");
        }
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

    private void sincronizarQuilometragemVeiculo(VeiculoModel veiculo, Integer quilometragem) {
        if (veiculo == null || quilometragem == null) {
            return;
        }

        if (veiculo.getQuilometragem() != null && quilometragem <= veiculo.getQuilometragem()) {
            return;
        }

        veiculo.setQuilometragem(quilometragem);
        veiculoRepository.save(veiculo);
    }

    private void notificarAposSalvar(OrdemSalva salva) {
        try {
            switch (salva.acao()) {
                case FINALIZACAO -> adicionarResultadoNotificacoesFinalizacao(salva.ordem(), salva.response());
                case REAGENDAR_REVISAO -> adicionarResultadoLembreteRevisao(salva.ordem(), salva.response());
                case CANCELAR_REVISAO -> agendarLembreteRevisao.cancelar(salva.ordem().id());
                case NENHUMA -> {
                }
            }
        } catch (RuntimeException e) {
            log.error("Falha ao processar notificações da OS {}", salva.ordem().id(), e);
            salva.response().adicionarAviso("Ordem salva, mas não foi possível registrar as notificações.");
        }
    }

    private void adicionarResultadoNotificacoesFinalizacao(OrdemParaNotificar ordem,
                                                           OrdemServicoDTOResponse response) {
        ResultadoNotificacao finalizacao = notificarFinalizacao.executar(ordem);
        response.setMensagemFinalizacaoEnviada(finalizacao.enviada());
        response.adicionarAviso(finalizacao.aviso());

        if (ordem.dataProximaRevisao() != null) {
            adicionarResultadoLembreteRevisao(ordem, response);
        }
    }

    private void adicionarResultadoLembreteRevisao(OrdemParaNotificar ordem, OrdemServicoDTOResponse response) {
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

        try {
            ordemServicoRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ordem de serviço possui notificações registradas e não pode ser excluída. "
                            + "Altere o status para cancelada.");
        }
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

    private enum AcaoNotificacao {
        NENHUMA,
        FINALIZACAO,
        REAGENDAR_REVISAO,
        CANCELAR_REVISAO
    }

    private record OrdemSalva(OrdemServicoDTOResponse response, OrdemParaNotificar ordem, AcaoNotificacao acao) {
    }
}
