package oficina.mecanica.notificacao.infra;

import oficina.mecanica.notificacao.usecase.ResultadoEnvio;
import oficina.mecanica.notificacao.usecase.port.ProvedorDeMensagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Modo simulado ({@code twilio.enabled=false}): não chama nenhum serviço
 * externo, só registra a mensagem no log e responde como enviada. Permite
 * demonstrar o fluxo completo API → RabbitMQ → microsserviço → API sem conta
 * na Twilio.
 */
public class LogProvedorDeMensagem implements ProvedorDeMensagem {

    private static final Logger log = LoggerFactory.getLogger(LogProvedorDeMensagem.class);

    @Override
    public ResultadoEnvio enviar(String telefoneE164, String mensagem, String mensagemCurta) {
        String id = "simulado-" + UUID.randomUUID();
        log.info("[SIMULADO] Mensagem {} para {}: {} | Versão SMS: {}", id, telefoneE164, mensagem, mensagemCurta);
        return ResultadoEnvio.sucesso(id);
    }
}
