package oficina.mecanica.notificacao.usecase;

import oficina.mecanica.notificacao.domain.TelefoneE164;
import oficina.mecanica.notificacao.usecase.port.ProvedorDeMensagem;
import oficina.mecanica.notificacao.usecase.port.PublicadorDeResultado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Envia a notificação pedida pela API e devolve o resultado pela fila de
 * resultado. Falhas definitivas (telefone inválido, número fora do sandbox,
 * credencial errada) viram resultado com erro; falhas temporárias sobem como
 * {@link ProvedorIndisponivelException} para a fila tentar de novo.
 */
public class EnviarNotificacaoUseCase {

    private static final Logger log = LoggerFactory.getLogger(EnviarNotificacaoUseCase.class);

    private final ProvedorDeMensagem provedor;
    private final PublicadorDeResultado publicador;
    private final String codigoPaisPadrao;

    public EnviarNotificacaoUseCase(ProvedorDeMensagem provedor,
                                    PublicadorDeResultado publicador,
                                    String codigoPaisPadrao) {
        this.provedor = provedor;
        this.publicador = publicador;
        this.codigoPaisPadrao = codigoPaisPadrao;
    }

    public ResultadoEnvio executar(PedidoDeEnvio pedido) {
        ResultadoEnvio resultado = enviar(pedido);

        if (resultado.enviada()) {
            log.info("Notificação {} ({}) enviada. Id no provedor: {}",
                    pedido.notificacaoId(), pedido.contexto(), resultado.idProvedor());
        } else {
            log.warn("Notificação {} ({}) não enviada: {}",
                    pedido.notificacaoId(), pedido.contexto(), resultado.erro());
        }

        publicarResultado(pedido.notificacaoId(), resultado.enviada(), resultado.erro());
        return resultado;
    }

    /** Usado quando todas as tentativas se esgotaram: avisa a API que o envio falhou. */
    public void registrarFalhaDefinitiva(Long notificacaoId, String erro) {
        publicarResultado(notificacaoId, false, erro);
    }

    private ResultadoEnvio enviar(PedidoDeEnvio pedido) {
        if (pedido.mensagem() == null || pedido.mensagem().isBlank()) {
            return ResultadoEnvio.falha("Mensagem vazia.");
        }

        Optional<String> destino = TelefoneE164.converter(pedido.telefone(), codigoPaisPadrao);
        if (destino.isEmpty()) {
            return ResultadoEnvio.falha("Telefone inválido para envio: " + pedido.telefone());
        }

        return provedor.enviar(destino.get(), pedido.mensagem(), pedido.mensagemCurta());
    }

    private void publicarResultado(Long notificacaoId, boolean enviada, String erro) {
        // Sem id não há histórico para atualizar na API.
        if (notificacaoId != null) {
            publicador.publicar(notificacaoId, enviada, erro);
        }
    }
}
