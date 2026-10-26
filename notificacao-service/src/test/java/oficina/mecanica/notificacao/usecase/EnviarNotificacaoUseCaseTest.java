package oficina.mecanica.notificacao.usecase;

import oficina.mecanica.notificacao.usecase.port.ProvedorDeMensagem;
import oficina.mecanica.notificacao.usecase.port.PublicadorDeResultado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnviarNotificacaoUseCaseTest {

    private ProvedorFake provedor;
    private PublicadorFake publicador;
    private EnviarNotificacaoUseCase useCase;

    @BeforeEach
    void setUp() {
        provedor = new ProvedorFake();
        publicador = new PublicadorFake();
        useCase = new EnviarNotificacaoUseCase(provedor, publicador, "55");
    }

    @Test
    void enviaEmE164EPublicaSucesso() {
        ResultadoEnvio resultado = useCase.executar(pedido(7L, "11999998888"));

        assertTrue(resultado.enviada());
        assertEquals(List.of("+5511999998888"), provedor.destinos);
        assertEquals(1, publicador.publicados.size());
        assertEquals(7L, publicador.publicados.get(0).notificacaoId());
        assertTrue(publicador.publicados.get(0).enviada());
    }

    @Test
    void telefoneInvalidoNaoChamaProvedorEPublicaErro() {
        ResultadoEnvio resultado = useCase.executar(pedido(7L, "123"));

        assertFalse(resultado.enviada());
        assertTrue(provedor.destinos.isEmpty());
        assertFalse(publicador.publicados.get(0).enviada());
        assertTrue(publicador.publicados.get(0).erro().startsWith("Telefone inválido"));
    }

    @Test
    void falhaDefinitivaDoProvedorEhDevolvidaParaAApi() {
        provedor.resposta = ResultadoEnvio.falha("Twilio 63015: fora do sandbox");

        useCase.executar(pedido(7L, "11999998888"));

        assertFalse(publicador.publicados.get(0).enviada());
        assertEquals("Twilio 63015: fora do sandbox", publicador.publicados.get(0).erro());
    }

    @Test
    void falhaTemporariaSobeParaAFilaTentarDeNovoSemPublicarResultado() {
        provedor.erroTemporario = true;

        assertThrows(ProvedorIndisponivelException.class, () -> useCase.executar(pedido(7L, "11999998888")));
        assertTrue(publicador.publicados.isEmpty());
    }

    @Test
    void semNotificacaoIdEnviaMasNaoPublicaResultado() {
        useCase.executar(pedido(null, "11999998888"));

        assertEquals(1, provedor.destinos.size());
        assertTrue(publicador.publicados.isEmpty());
    }

    private static PedidoDeEnvio pedido(Long id, String telefone) {
        return new PedidoDeEnvio(id, telefone, "Olá! Seu carro está pronto.", "Carro pronto.", "teste");
    }

    private static class ProvedorFake implements ProvedorDeMensagem {
        final List<String> destinos = new ArrayList<>();
        ResultadoEnvio resposta = ResultadoEnvio.sucesso("SM123");
        boolean erroTemporario;

        @Override
        public ResultadoEnvio enviar(String telefoneE164, String mensagem, String mensagemCurta) {
            if (erroTemporario) {
                throw new ProvedorIndisponivelException("timeout", null);
            }
            destinos.add(telefoneE164);
            return resposta;
        }
    }

    private record Publicado(Long notificacaoId, boolean enviada, String erro) {
    }

    private static class PublicadorFake implements PublicadorDeResultado {
        final List<Publicado> publicados = new ArrayList<>();

        @Override
        public void publicar(Long notificacaoId, boolean enviada, String erro) {
            publicados.add(new Publicado(notificacaoId, enviada, erro));
        }
    }
}
