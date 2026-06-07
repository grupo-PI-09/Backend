package oficina.mecanica.backendOficina.Service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import oficina.mecanica.backendOficina.DTO.NotificacaoResultadoDTO;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);
    private static final DateTimeFormatter DATA_PT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${twilio.whatsapp-from}")
    private String from;

    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> lembretesAgendados = new ConcurrentHashMap<>();
    private final Set<Long> lembretesProcessados = ConcurrentHashMap.newKeySet();

    public NotificacaoService(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void enviarWhatsApp(String telefoneCliente, String nomeCliente, String placaVeiculo) {
        String mensagem = String.format(
                "%s Passando para lembrar que a revisão preventiva do veículo de placa %s está se aproximando. " +
                        "Entre em contato com a RRMaxx Oficina Inteligente para agendar. Obrigado!",
                montarSaudacao(nomeCliente),
                valorOuPadrao(placaVeiculo, "não informada")
        );

        enviarMensagemWhatsApp(telefoneCliente, mensagem, "revisão preventiva");
    }

    public NotificacaoResultadoDTO enviarMensagemFinalizacao(OrdemServicoModel ordemServico) {
        ClienteModel cliente = ordemServico.getCliente();
        VeiculoModel veiculo = ordemServico.getVeiculo();
        String mensagem = String.format(
                "%s O serviço do veículo %s foi finalizado pela oficina. " +
                        "A garantia do serviço já está ativa conforme as condições informadas. Obrigado pela preferência!",
                montarSaudacao(cliente != null ? cliente.getNome() : null),
                montarDescricaoVeiculoParaMensagem(veiculo)
        );

        boolean enviada = enviarMensagemWhatsApp(
                cliente != null ? cliente.getTelefone() : null,
                mensagem,
                "finalização da OS " + ordemServico.getId()
        );

        if (enviada) {
            return NotificacaoResultadoDTO.enviada("Mensagem de finalização enviada.");
        }

        return NotificacaoResultadoDTO.naoEnviada("Mensagem de finalização não enviada. Verifique telefone e configuração do Twilio.");
    }

    public NotificacaoResultadoDTO agendarLembreteRevisao(OrdemServicoModel ordemServico) {
        if (ordemServico.getDataProximaRevisao() == null) {
            return NotificacaoResultadoDTO.naoEnviada("Data de revisão preventiva não informada.");
        }

        Long ordemId = ordemServico.getId();
        if (ordemId != null && lembretesProcessados.contains(ordemId)) {
            return NotificacaoResultadoDTO.naoEnviada("Lembrete de revisão já processado nesta execução.");
        }

        LocalDateTime dataEnvio = ordemServico.getDataProximaRevisao().minusDays(7);
        LocalDateTime agora = LocalDateTime.now();

        if (!dataEnvio.isAfter(agora)) {
            boolean enviada = enviarMensagemRevisaoPreventiva(ordemServico);
            if (enviada) {
                if (ordemId != null) {
                    lembretesProcessados.add(ordemId);
                }

                return new NotificacaoResultadoDTO(
                        true,
                        false,
                        true,
                        agora,
                        "Data da revisão com menos de 7 dias; lembrete enviado imediatamente.",
                        null
                );
            }

            return NotificacaoResultadoDTO.naoEnviada("Lembrete de revisão não enviado. Verifique telefone e configuração do Twilio.");
        }

        if (ordemId != null && lembretesAgendados.containsKey(ordemId)) {
            return NotificacaoResultadoDTO.agendada(dataEnvio, "Lembrete de revisão já estava agendado.");
        }

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> {
                    try {
                        boolean enviada = enviarMensagemRevisaoPreventiva(ordemServico);
                        if (enviada && ordemId != null) {
                            lembretesProcessados.add(ordemId);
                        }
                    } finally {
                        if (ordemId != null) {
                            lembretesAgendados.remove(ordemId);
                        }
                    }
                },
                Date.from(dataEnvio.atZone(ZoneId.systemDefault()).toInstant())
        );

        if (ordemId != null) {
            lembretesAgendados.put(ordemId, future);
        }

        log.info("Lembrete de revisão preventiva agendado para OS {} em {}", ordemId, dataEnvio);
        return NotificacaoResultadoDTO.agendada(dataEnvio, "Lembrete de revisão preventiva agendado.");
    }

    private boolean enviarMensagemRevisaoPreventiva(OrdemServicoModel ordemServico) {
        ClienteModel cliente = ordemServico.getCliente();
        VeiculoModel veiculo = ordemServico.getVeiculo();
        String mensagem = String.format(
                "%s Passando para lembrar que a revisão preventiva do veículo %s está prevista para %s. " +
                        "Recomendamos entrar em contato com a oficina para confirmar o agendamento.",
                montarSaudacao(cliente != null ? cliente.getNome() : null),
                montarDescricaoVeiculoParaMensagem(veiculo),
                ordemServico.getDataProximaRevisao().format(DATA_PT_BR)
        );

        return enviarMensagemWhatsApp(
                cliente != null ? cliente.getTelefone() : null,
                mensagem,
                "lembrete de revisão da OS " + ordemServico.getId()
        );
    }

    private boolean enviarMensagemWhatsApp(String telefoneCliente, String mensagem, String contexto) {
        String destinatario = montarDestinatarioWhatsApp(telefoneCliente);
        if (destinatario == null) {
            log.warn("Mensagem de {} não enviada: telefone do cliente ausente ou inválido.", contexto);
            return false;
        }

        if (from == null || from.isBlank()) {
            log.warn("Mensagem de {} não enviada: remetente Twilio não configurado.", contexto);
            return false;
        }

        try {
            Message.creator(
                    new PhoneNumber(destinatario),
                    new PhoneNumber(from),
                    mensagem
            ).create();

            log.info("Mensagem de {} enviada para {}", contexto, destinatario);
            return true;
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem de {} via Twilio: {}", contexto, e.getMessage());
            return false;
        }
    }

    private String montarDestinatarioWhatsApp(String telefoneCliente) {
        String digitos = telefoneCliente != null ? telefoneCliente.replaceAll("[^0-9]", "") : "";
        if (digitos.isBlank()) {
            return null;
        }

        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = "55" + digitos;
        }

        if (!digitos.startsWith("55") || digitos.length() < 12 || digitos.length() > 13) {
            return null;
        }

        return "whatsapp:+" + digitos;
    }

    private String montarSaudacao(String nomeCliente) {
        String nome = valorOuPadrao(nomeCliente, "");
        return nome.isBlank() ? "Olá!" : "Olá, " + nome + "!";
    }

    private String montarDescricaoVeiculoParaMensagem(VeiculoModel veiculo) {
        if (veiculo == null) {
            return "não informado";
        }

        String modelo = valorOuPadrao(veiculo.getModelo(), "");
        String placa = valorOuPadrao(veiculo.getPlaca(), "");

        if (!modelo.isBlank() && !placa.isBlank()) {
            return modelo + " / placa " + placa;
        }

        if (!placa.isBlank()) {
            return "placa " + placa;
        }

        if (!modelo.isBlank()) {
            return modelo;
        }

        return "não informado";
    }

    private String valorOuPadrao(String valor, String padrao) {
        return valor != null && !valor.isBlank() ? valor.trim() : padrao;
    }
}
