package oficina.mecanica.notificacao.usecase.port;

import oficina.mecanica.notificacao.usecase.ProvedorIndisponivelException;
import oficina.mecanica.notificacao.usecase.ResultadoEnvio;

/**
 * Port de saída para o provedor que de fato entrega a mensagem
 * (Twilio ou log). Trocar o provedor é trocar a implementação.
 */
public interface ProvedorDeMensagem {

    /**
     * @param telefoneE164  destinatário no formato E.164 (ex.: +5511999998888)
     * @param mensagem      texto completo
     * @param mensagemCurta versão para canais com limite de tamanho (SMS); pode ser {@code null}.
     *                      O provedor escolhe qual das duas usar conforme o canal.
     * @return o resultado definitivo do envio
     * @throws ProvedorIndisponivelException quando a falha é temporária e vale nova tentativa
     */
    ResultadoEnvio enviar(String telefoneE164, String mensagem, String mensagemCurta);
}
