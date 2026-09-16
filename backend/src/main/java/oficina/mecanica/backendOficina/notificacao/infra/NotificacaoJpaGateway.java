package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.Model.CanalNotificacao;
import oficina.mecanica.backendOficina.Model.NotificacaoModel;
import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.NotificacaoRepository;
import oficina.mecanica.backendOficina.Repository.OrdemServicoRepository;
import oficina.mecanica.backendOficina.Repository.VeiculoRepository;
import oficina.mecanica.backendOficina.notificacao.domain.NovaNotificacao;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Adapter JPA do histórico de notificações. Falhas de persistência são
 * registradas no log e não interrompem o fluxo de negócio que as originou.
 */
@Component
public class NotificacaoJpaGateway implements NotificacaoGateway {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoJpaGateway.class);

    private final NotificacaoRepository notificacaoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public NotificacaoJpaGateway(NotificacaoRepository notificacaoRepository,
                                 ClienteRepository clienteRepository,
                                 VeiculoRepository veiculoRepository,
                                 OrdemServicoRepository ordemServicoRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Override
    @Transactional
    public Optional<Long> registrar(NovaNotificacao nova) {
        try {
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
        } catch (Exception e) {
            log.error("Erro ao registrar notificação no histórico: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Optional<Long> atualizarLembreteRevisaoPendente(Long ordemServicoId,
                                                           String mensagem,
                                                           LocalDateTime dataAgendamento) {
        return notificacaoRepository
                .findFirstByOrdemServicoIdAndTipoAndStatus(
                        ordemServicoId, TipoNotificacao.revisao_preventiva, StatusNotificacao.pendente)
                .map(pendente -> {
                    pendente.setMensagem(mensagem);
                    pendente.setDataAgendamento(dataAgendamento);
                    return notificacaoRepository.save(pendente).getId();
                });
    }

    @Override
    @Transactional
    public void registrarResultadoEnvio(Long notificacaoId, boolean enviada, LocalDateTime dataEnvio) {
        try {
            notificacaoRepository.findById(notificacaoId).ifPresent(notificacao -> {
                notificacao.setStatus(enviada ? StatusNotificacao.enviada : StatusNotificacao.erro);
                if (enviada) {
                    notificacao.setDataEnvio(dataEnvio);
                }
                notificacaoRepository.save(notificacao);
            });
        } catch (Exception e) {
            log.error("Erro ao atualizar notificação agendada {}: {}", notificacaoId, e.getMessage());
        }
    }
}
