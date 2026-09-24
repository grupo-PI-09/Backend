package oficina.mecanica.backendOficina.notificacao.usecase;

import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.NovaNotificacao;
import oficina.mecanica.backendOficina.notificacao.usecase.port.AgendadorDeLembrete;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgendarLembreteRevisaoUseCaseTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 15, 10, 0);

    private EnviadorFake enviador;
    private GatewayFake gateway;
    private AgendadorFake agendador;
    private AgendarLembreteRevisaoUseCase useCase;

    @BeforeEach
    void setUp() {
        enviador = new EnviadorFake();
        gateway = new GatewayFake();
        agendador = new AgendadorFake();
        Clock clock = Clock.fixed(AGORA.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        useCase = new AgendarLembreteRevisaoUseCase(enviador, gateway, agendador, clock);
    }

    @Test
    void revisaoDistanteAgendaLembreteSeteDiasAntes() {
        OrdemParaNotificar ordem = ordem(AGORA.plusDays(30));

        ResultadoNotificacao resultado = useCase.executar(ordem);

        assertTrue(resultado.agendada());
        assertFalse(resultado.enviada());
        assertEquals(AGORA.plusDays(23), resultado.dataAgendamento());
        assertEquals(AGORA.plusDays(23), agendador.quando);
        assertEquals(1, gateway.registradas.size());
        assertEquals(StatusNotificacao.pendente, gateway.registradas.get(0).status());
        assertTrue(enviador.enviadas.isEmpty());
    }

    @Test
    void revisaoDentroDoPrazoEnviaImediatamente() {
        OrdemParaNotificar ordem = ordem(AGORA.plusDays(3));

        ResultadoNotificacao resultado = useCase.executar(ordem);

        assertTrue(resultado.enviadaImediatamente());
        assertFalse(resultado.agendada());
        assertEquals(1, enviador.enviadas.size());
        assertEquals("11999998888", enviador.enviadas.get(0));
        assertEquals(StatusNotificacao.enviada, gateway.registradas.get(0).status());
        assertEquals(AGORA, gateway.registradas.get(0).dataEnvio());
        assertEquals(AGORA.plusDays(3), agendador.processados.get(1L));
    }

    @Test
    void semTelefoneRegistraErroENaoEnvia() {
        OrdemParaNotificar ordem = new OrdemParaNotificar(
                1L, 10L, "Ana", "  ", 20L, "Gol", "ABC1D23", AGORA.plusDays(2));

        ResultadoNotificacao resultado = useCase.executar(ordem);

        assertFalse(resultado.enviada());
        assertEquals("Lembrete de revisão não registrado: cliente sem telefone cadastrado.", resultado.aviso());
        assertTrue(enviador.enviadas.isEmpty());
        assertEquals(StatusNotificacao.erro, gateway.registradas.get(0).status());
        assertNull(gateway.registradas.get(0).dataEnvio());
    }

    @Test
    void semDataDeRevisaoCancelaLembreteExistente() {
        agendador.agendados.put(1L, AGORA.plusDays(23));

        ResultadoNotificacao resultado = useCase.executar(ordem(null));

        assertFalse(resultado.enviada());
        assertFalse(resultado.agendada());
        assertTrue(gateway.registradas.isEmpty());
        assertTrue(enviador.enviadas.isEmpty());
        assertTrue(agendador.agendados.isEmpty());
        assertEquals(List.of(1L), gateway.pendentesDescartados);
    }

    @Test
    void tarefaAgendadaEnviaEAtualizaNotificacaoPendente() {
        OrdemParaNotificar ordem = ordem(AGORA.plusDays(30));
        useCase.executar(ordem);
        gateway.ordemAtual = ordem;

        agendador.tarefa.run();

        assertEquals(1, enviador.enviadas.size());
        assertEquals(Long.valueOf(100L), gateway.resultadoId);
        assertTrue(gateway.resultadoEnviada);
        assertEquals(AGORA.plusDays(30), agendador.processados.get(1L));
    }

    @Test
    void tarefaAgendadaNaoEnviaQuandoOrdemNaoEstaMaisFinalizada() {
        useCase.executar(ordem(AGORA.plusDays(30)));
        gateway.ordemAtual = null;

        agendador.tarefa.run();

        assertTrue(enviador.enviadas.isEmpty());
        assertNull(gateway.resultadoId);
        assertEquals(List.of(1L), gateway.pendentesDescartados);
    }

    @Test
    void tarefaAgendadaNaoEnviaQuandoDataDaRevisaoMudou() {
        useCase.executar(ordem(AGORA.plusDays(30)));
        gateway.ordemAtual = ordem(AGORA.plusDays(40));

        agendador.tarefa.run();

        assertTrue(enviador.enviadas.isEmpty());
        assertNull(gateway.resultadoId);
        assertTrue(gateway.pendentesDescartados.isEmpty());
    }

    @Test
    void naoReagendaQuandoJaExisteAgendamentoNaMesmaData() {
        agendador.agendados.put(1L, AGORA.plusDays(23));

        ResultadoNotificacao resultado = useCase.executar(ordem(AGORA.plusDays(30)));

        assertTrue(resultado.agendada());
        assertEquals("Lembrete de revisão já estava agendado.", resultado.mensagem());
        assertTrue(gateway.registradas.isEmpty());
        assertNull(agendador.tarefa);
    }

    @Test
    void reagendaQuandoDataDaRevisaoMuda() {
        agendador.agendados.put(1L, AGORA.plusDays(23));

        ResultadoNotificacao resultado = useCase.executar(ordem(AGORA.plusDays(40)));

        assertTrue(resultado.agendada());
        assertEquals(AGORA.plusDays(33), agendador.quando);
        assertEquals(AGORA.plusDays(33), agendador.agendados.get(1L));
        assertEquals(List.of(1L), agendador.cancelados);
    }

    @Test
    void enviaDeNovoQuandoRevisaoJaProcessadaMudaDeData() {
        agendador.processados.put(1L, AGORA.plusDays(2));

        ResultadoNotificacao resultado = useCase.executar(ordem(AGORA.plusDays(3)));

        assertTrue(resultado.enviadaImediatamente());
        assertEquals(1, enviador.enviadas.size());
    }

    private static OrdemParaNotificar ordem(LocalDateTime dataRevisao) {
        return new OrdemParaNotificar(
                1L, 10L, "Ana", "(11) 99999-8888", 20L, "Gol", "ABC1D23", dataRevisao);
    }

    private static class EnviadorFake implements EnviadorDeMensagem {
        final List<String> enviadas = new ArrayList<>();

        @Override
        public boolean enviar(String telefone, String mensagem, String contexto) {
            enviadas.add(telefone);
            return true;
        }
    }

    private static class GatewayFake implements NotificacaoGateway {
        final List<NovaNotificacao> registradas = new ArrayList<>();
        final List<Long> pendentesDescartados = new ArrayList<>();
        OrdemParaNotificar ordemAtual;
        Long resultadoId;
        boolean resultadoEnviada;

        @Override
        public Optional<Long> registrar(NovaNotificacao notificacao) {
            registradas.add(notificacao);
            return Optional.of(100L);
        }

        @Override
        public Optional<Long> atualizarLembreteRevisaoPendente(Long ordemServicoId, String mensagem,
                                                               LocalDateTime dataAgendamento) {
            return Optional.empty();
        }

        @Override
        public void registrarResultadoEnvio(Long notificacaoId, boolean enviada, LocalDateTime dataEnvio) {
            resultadoId = notificacaoId;
            resultadoEnviada = enviada;
        }

        @Override
        public void descartarLembretesPendentes(Long ordemServicoId) {
            pendentesDescartados.add(ordemServicoId);
        }

        @Override
        public Optional<OrdemParaNotificar> buscarOrdemFinalizada(Long ordemServicoId) {
            return Optional.ofNullable(ordemAtual);
        }
    }

    private static class AgendadorFake implements AgendadorDeLembrete {
        final Map<Long, LocalDateTime> agendados = new HashMap<>();
        final Map<Long, LocalDateTime> processados = new HashMap<>();
        final List<Long> cancelados = new ArrayList<>();
        LocalDateTime quando;
        Runnable tarefa;

        @Override
        public boolean estaAgendado(Long ordemServicoId, LocalDateTime quando) {
            return quando.equals(agendados.get(ordemServicoId));
        }

        @Override
        public boolean foiProcessado(Long ordemServicoId, LocalDateTime dataRevisao) {
            return dataRevisao.equals(processados.get(ordemServicoId));
        }

        @Override
        public void marcarProcessado(Long ordemServicoId, LocalDateTime dataRevisao) {
            processados.put(ordemServicoId, dataRevisao);
        }

        @Override
        public void agendar(Long ordemServicoId, LocalDateTime quando, Runnable tarefa) {
            this.quando = quando;
            this.tarefa = tarefa;
            agendados.put(ordemServicoId, quando);
        }

        @Override
        public void cancelar(Long ordemServicoId) {
            if (agendados.remove(ordemServicoId) != null) {
                cancelados.add(ordemServicoId);
            }
        }
    }
}
