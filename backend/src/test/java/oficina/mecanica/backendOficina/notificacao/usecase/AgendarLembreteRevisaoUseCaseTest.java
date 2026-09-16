package oficina.mecanica.backendOficina.notificacao.usecase;

import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.notificacao.domain.NovaNotificacao;
import oficina.mecanica.backendOficina.notificacao.usecase.port.AgendadorDeLembrete;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        assertTrue(agendador.processados.contains(1L));
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
    void semDataDeRevisaoNaoFazNada() {
        ResultadoNotificacao resultado = useCase.executar(ordem(null));

        assertFalse(resultado.enviada());
        assertFalse(resultado.agendada());
        assertTrue(gateway.registradas.isEmpty());
        assertTrue(enviador.enviadas.isEmpty());
    }

    @Test
    void tarefaAgendadaEnviaEAtualizaNotificacaoPendente() {
        useCase.executar(ordem(AGORA.plusDays(30)));

        agendador.tarefa.run();

        assertEquals(1, enviador.enviadas.size());
        assertEquals(Long.valueOf(100L), gateway.resultadoId);
        assertTrue(gateway.resultadoEnviada);
        assertTrue(agendador.processados.contains(1L));
    }

    @Test
    void naoReagendaQuandoJaExisteAgendamento() {
        agendador.agendados.add(1L);

        ResultadoNotificacao resultado = useCase.executar(ordem(AGORA.plusDays(30)));

        assertTrue(resultado.agendada());
        assertEquals("Lembrete de revisão já estava agendado.", resultado.mensagem());
        assertTrue(gateway.registradas.isEmpty());
        assertNull(agendador.tarefa);
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
    }

    private static class AgendadorFake implements AgendadorDeLembrete {
        final Set<Long> agendados = new HashSet<>();
        final Set<Long> processados = new HashSet<>();
        LocalDateTime quando;
        Runnable tarefa;

        @Override
        public boolean estaAgendado(Long ordemServicoId) {
            return agendados.contains(ordemServicoId);
        }

        @Override
        public boolean foiProcessado(Long ordemServicoId) {
            return processados.contains(ordemServicoId);
        }

        @Override
        public void marcarProcessado(Long ordemServicoId) {
            processados.add(ordemServicoId);
        }

        @Override
        public void agendar(Long ordemServicoId, LocalDateTime quando, Runnable tarefa) {
            this.quando = quando;
            this.tarefa = tarefa;
            agendados.add(ordemServicoId);
        }
    }
}
