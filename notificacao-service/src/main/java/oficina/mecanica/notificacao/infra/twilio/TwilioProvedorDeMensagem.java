package oficina.mecanica.notificacao.infra.twilio;

import com.twilio.exception.ApiConnectionException;
import com.twilio.exception.ApiException;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import oficina.mecanica.notificacao.domain.TelefoneE164;
import oficina.mecanica.notificacao.domain.TextoSms;
import oficina.mecanica.notificacao.usecase.ProvedorIndisponivelException;
import oficina.mecanica.notificacao.usecase.ResultadoEnvio;
import oficina.mecanica.notificacao.usecase.port.ProvedorDeMensagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Envia a mensagem pela API REST da Twilio (WhatsApp ou SMS).
 *
 * A Twilio aceita a mensagem na hora (status "queued") e só depois informa se
 * ela falhou — é assim que aparecem os erros 63015 (fora do sandbox) e 63016
 * (fora da janela de 24h). Por isso, depois de criar a mensagem, o status é
 * consultado por até {@code twilio.confirmacao-timeout}.
 */
public class TwilioProvedorDeMensagem implements ProvedorDeMensagem {

    private static final Logger log = LoggerFactory.getLogger(TwilioProvedorDeMensagem.class);

    private static final Duration INTERVALO_CONSULTA = Duration.ofMillis(1500);

    private static final Set<Message.Status> ENTREGUE = Set.of(Message.Status.DELIVERED, Message.Status.READ);
    private static final Set<Message.Status> FALHOU = Set.of(
            Message.Status.FAILED, Message.Status.UNDELIVERED, Message.Status.CANCELED);

    private final TwilioRestClient client;
    private final TwilioProperties properties;

    public TwilioProvedorDeMensagem(TwilioRestClient client, TwilioProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public ResultadoEnvio enviar(String telefoneE164, String mensagem, String mensagemCurta) {
        String texto = texto(mensagem, mensagemCurta);
        String destino = telefoneE164;
        if (properties.temDestinoTeste()) {
            log.info("Destino de teste ativo: mensagem para {} redirecionada para {}.",
                    telefoneE164, properties.destinoTeste());
            destino = TelefoneE164.converter(properties.destinoTeste(), "55").orElse(properties.destinoTeste());
        }

        try {
            Message criada = criador(destino, texto).create(client);
            log.info("Twilio aceitou a mensagem {} para {} (status {}).", criada.getSid(), destino, criada.getStatus());
            return confirmar(criada);
        } catch (ApiConnectionException e) {
            throw new ProvedorIndisponivelException("Sem conexão com a Twilio: " + e.getMessage(), e);
        } catch (ApiException e) {
            if (temporario(e)) {
                throw new ProvedorIndisponivelException(ErrosTwilio.descrever(e.getCode(), e.getMessage()), e);
            }
            return ResultadoEnvio.falha(ErrosTwilio.descrever(e.getCode(), e.getMessage()));
        }
    }

    /**
     * WhatsApp recebe o texto completo. SMS recebe a versão curta sem acentos,
     * para ser cobrado como 1 segmento (ver {@link TextoSms}).
     */
    private String texto(String mensagem, String mensagemCurta) {
        if (properties.canal() != TwilioProperties.Canal.sms) {
            return mensagem;
        }

        String sms = TextoSms.paraGsm7(mensagemCurta != null && !mensagemCurta.isBlank() ? mensagemCurta : mensagem);
        int segmentos = TextoSms.segmentos(sms);
        if (segmentos > 1) {
            log.warn("SMS com {} caracteres será cobrado como {} segmentos.", sms.length(), segmentos);
        }
        return sms;
    }

    private MessageCreator criador(String destino, String mensagem) {
        PhoneNumber para = new PhoneNumber(endereco(destino));

        if (properties.usaMessagingService()) {
            return Message.creator(para, properties.messagingServiceSid(), mensagem);
        }

        return Message.creator(para, new PhoneNumber(endereco(properties.remetente())), mensagem);
    }

    /** No WhatsApp, origem e destino levam o prefixo "whatsapp:". */
    private String endereco(String numero) {
        String limpo = numero.trim();
        if (properties.canal() == TwilioProperties.Canal.whatsapp && !limpo.startsWith("whatsapp:")) {
            return "whatsapp:" + limpo;
        }
        return limpo;
    }

    private ResultadoEnvio confirmar(Message criada) {
        Duration timeout = properties.confirmacaoTimeout();
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            return ResultadoEnvio.sucesso(criada.getSid());
        }

        Instant limite = Instant.now().plus(timeout);
        Message atual = criada;

        while (true) {
            if (ENTREGUE.contains(atual.getStatus())) {
                return ResultadoEnvio.sucesso(atual.getSid());
            }

            if (FALHOU.contains(atual.getStatus())) {
                return ResultadoEnvio.falha(atual.getSid(),
                        ErrosTwilio.descrever(atual.getErrorCode(), atual.getErrorMessage()));
            }

            if (Instant.now().plus(INTERVALO_CONSULTA).isAfter(limite)) {
                // Aceita pela Twilio e sem falha até aqui: conta como enviada.
                log.info("Mensagem {} ainda com status {} após {}s; considerada enviada.",
                        atual.getSid(), atual.getStatus(), timeout.toSeconds());
                return ResultadoEnvio.sucesso(atual.getSid());
            }

            if (!aguardar()) {
                return ResultadoEnvio.sucesso(atual.getSid());
            }

            try {
                atual = Message.fetcher(criada.getSid()).fetch(client);
            } catch (Exception e) {
                // A mensagem já foi aceita; uma falha só na consulta não deve reenviar.
                log.warn("Não foi possível consultar o status da mensagem {}: {}", criada.getSid(), e.getMessage());
                return ResultadoEnvio.sucesso(criada.getSid());
            }
        }
    }

    private boolean aguardar() {
        try {
            Thread.sleep(INTERVALO_CONSULTA.toMillis());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** 429 (limite de taxa) e 5xx valem nova tentativa; 4xx é erro de dados/configuração. */
    private static boolean temporario(ApiException e) {
        Integer status = e.getStatusCode();
        return status != null && (status == 429 || status >= 500);
    }
}
