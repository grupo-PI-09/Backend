package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.Model.CanalNotificacao;
import oficina.mecanica.backendOficina.Model.NotificacaoModel;
import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.NotificacaoRepository;
import oficina.mecanica.backendOficina.Repository.OrdemServicoRepository;
import oficina.mecanica.backendOficina.Repository.VeiculoRepository;
import oficina.mecanica.backendOficina.notificacao.domain.NovaNotificacao;
import oficina.mecanica.backendOficina.notificacao.usecase.OrdemParaNotificar;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Adapter JPA do histórico de notificações. Falhas de persistência são
 * registradas no log e não interrompem o fluxo de negócio que as originou.
 *
 * Cada operação roda em transação própria (REQUIRES_NEW): se rodasse dentro da
 * transação de quem chamou, uma falha aqui marcaria aquela transação como
 * rollback-only e desfaria, por exemplo, a atualização da ordem de serviço.
 */
@Component
public class NotificacaoJpaGateway implements NotificacaoGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoJpaGateway.class);

    private final NotificacaoRepository notificacaoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final TransactionTemplate transacaoPropria;

    public NotificacaoJpaGateway(NotificacaoRepository notificacaoRepository,
                                 ClienteRepository clienteRepository,
                                 VeiculoRepository veiculoRepository,
                                 OrdemServicoRepository ordemServicoRepository,
                                 PlatformTransactionManager transactionManager) {
        this.notificacaoRepository = notificacaoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.transacaoPropria = new TransactionTemplate(transactionManager);
        this.transacaoPropria.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Optional<Long> registrar(NovaNotificacao nova) {
        return emTransacaoPropria("registrar notificação no histórico", () -> {
            NotificacaoModel notificacao = new NotificacaoModel();
            notificacao.setCliente(clienteRepository.getReferenceById(nova.clienteId()));
            notificacao.setVeiculo(nova.veiculoId() != null ? veiculoRepository.getReferenceById(nova.veiculoId()) : null);
            notificacao.setOrdemServico(nova.ordemServicoId() != null ? ordemServicoRepository.getReferenceById(nova.ordemServicoId()) : null);
            notificacao.setTipo(nova.tipo());
            notificacao.setAssunto(nova.assunto());
            notificacao.setMensagem(nova.mensagem());
            notificacao.setCanal(CanalNotificacao.whatsapp);
            notificacao.setStatus(nova.status());
            notificacao.setTelefoneDestino(nova.telefoneDestino());
            notificacao.setLida(false);
            notificacao.setDataAgendamento(nova.dataAgendamento());
            notificacao.setDataEnvio(nova.dataEnvio());

            return Optional.ofNullable(notificacaoRepository.save(notificacao).getId());
        }).flatMap(id -> id);
    }

    @Override
    public Optional<Long> atualizarLembreteRevisaoPendente(Long ordemServicoId,
                                                           String mensagem,
                                                           LocalDateTime dataAgendamento) {
        return emTransacaoPropria("atualizar lembrete pendente da OS " + ordemServicoId, () ->
                notificacaoRepository
                        .findFirstByOrdemServicoIdAndTipoAndStatus(
                                ordemServicoId, TipoNotificacao.revisao_preventiva, StatusNotificacao.pendente)
                        .map(pendente -> {
                            pendente.setMensagem(mensagem);
                            pendente.setDataAgendamento(dataAgendamento);
                            return notificacaoRepository.save(pendente).getId();
                        })
        ).flatMap(id -> id);
    }

    @Override
    public void registrarResultadoEnvio(Long notificacaoId, boolean enviada, LocalDateTime dataEnvio) {
        emTransacaoPropria("atualizar notificação agendada " + notificacaoId, () -> {
            notificacaoRepository.findById(notificacaoId).ifPresent(notificacao -> {
                notificacao.setStatus(enviada ? StatusNotificacao.enviada : StatusNotificacao.erro);
                if (enviada) {
                    notificacao.setDataEnvio(dataEnvio);
                }
                notificacaoRepository.save(notificacao);
            });
            return Boolean.TRUE;
        });
    }

    @Override
    public void descartarLembretesPendentes(Long ordemServicoId) {
        emTransacaoPropria("descartar lembretes pendentes da OS " + ordemServicoId, () -> {
            notificacaoRepository.deleteAll(notificacaoRepository.findByOrdemServicoIdAndTipoAndStatus(
                    ordemServicoId, TipoNotificacao.revisao_preventiva, StatusNotificacao.pendente));
            return Boolean.TRUE;
        });
    }

    @Override
    public Optional<OrdemParaNotificar> buscarOrdemFinalizada(Long ordemServicoId) {
        return emTransacaoPropria("consultar OS " + ordemServicoId, () ->
                ordemServicoRepository.findById(ordemServicoId)
                        .filter(ordem -> ordem.getStatus() == StatusOrdemServico.finalizada)
                        .map(OrdemParaNotificarMapper::de)
        ).flatMap(ordem -> ordem);
    }

    /** Executa em transação própria; qualquer falha é logada e vira {@code Optional.empty()}. */
    private <T> Optional<T> emTransacaoPropria(String operacao, Supplier<T> acao) {
        try {
            return Optional.ofNullable(transacaoPropria.execute(status -> acao.get()));
        } catch (RuntimeException e) {
            log.error("Erro ao {}: {}", operacao, e.getMessage());
            return Optional.empty();
        }
    }
}
