package oficina.mecanica.backendOficina.Scheduler;

import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Repository.OrdemServicoRepository;
import oficina.mecanica.backendOficina.notificacao.infra.OrdemParaNotificarMapper;
import oficina.mecanica.backendOficina.notificacao.usecase.AgendarLembreteRevisaoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RevisaoScheduler {

    private static final Logger log = LoggerFactory.getLogger(RevisaoScheduler.class);

    private final OrdemServicoRepository ordemServicoRepository;
    private final AgendarLembreteRevisaoUseCase agendarLembreteRevisao;

    public RevisaoScheduler(OrdemServicoRepository ordemServicoRepository,
                            AgendarLembreteRevisaoUseCase agendarLembreteRevisao) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.agendarLembreteRevisao = agendarLembreteRevisao;
    }

    @Scheduled(cron = "${notificacoes.revisao-scheduler-cron:0 0 8 * * *}")
    public void verificarRevisoesProximas() {
        log.info("Scheduler iniciado: verificando revisões preventivas para agendamento.");

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime janelaInicio = agora.plusDays(7);
        LocalDateTime janelaFim = agora.plusDays(8);

        List<OrdemServicoModel> ordens =
                ordemServicoRepository.findByStatusAndDataProximaRevisaoBetween(
                        StatusOrdemServico.finalizada,
                        janelaInicio,
                        janelaFim
                );

        log.info("Revisões encontradas para lembrete nas próximas 24h: {}", ordens.size());

        for (OrdemServicoModel os : ordens) {
            agendarLembreteRevisao.executar(OrdemParaNotificarMapper.de(os));
        }

        log.info("Scheduler finalizado.");
    }
}
