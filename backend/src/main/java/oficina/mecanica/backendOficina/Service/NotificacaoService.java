package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.NotificacaoDTOResponse;
import oficina.mecanica.backendOficina.DTO.NotificacaoResultadoDTO;
import oficina.mecanica.backendOficina.Model.CanalNotificacao;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.NotificacaoModel;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Repository.NotificacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Registra o histórico de notificações da oficina.
 *
 * O envio externo (WhatsApp/SMS) está desativado: as mensagens são montadas,
 * gravadas na tabela `notificacoes` e registradas no log, o que mantém a tela de
 * notificações funcional sem depender de um gateway configurado.
 */
@Service
public class NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoService.class);
    private static final DateTimeFormatter DATA_PT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String ASSUNTO_FINALIZACAO = "Serviço finalizado";
    private static final String ASSUNTO_REVISAO = "Lembrete de revisão preventiva";

    private final TaskScheduler taskScheduler;
    private final NotificacaoRepository notificacaoRepository;
    private final Map<Long, ScheduledFuture<?>> lembretesAgendados = new ConcurrentHashMap<>();
    private final Set<Long> lembretesProcessados = ConcurrentHashMap.newKeySet();

    public NotificacaoService(TaskScheduler taskScheduler, NotificacaoRepository notificacaoRepository) {
        this.taskScheduler = taskScheduler;
        this.notificacaoRepository = notificacaoRepository;
    }

    public List<NotificacaoDTOResponse> listar() {
        return notificacaoRepository.listarMaisRecentesPrimeiro()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public long contarNaoLidas() {
        return notificacaoRepository.countByLidaFalse();
    }

    public long contarEnviadas() {
        return notificacaoRepository.countByStatus(StatusNotificacao.enviada);
    }

    @Transactional
    public NotificacaoDTOResponse marcarComoLida(Long id, boolean lida) {
        NotificacaoModel notificacao = buscarNotificacao(id);
        notificacao.setLida(lida);

        return converterParaResponse(notificacaoRepository.save(notificacao));
    }

    @Transactional
    public List<NotificacaoDTOResponse> marcarTodasComoLidas() {
        List<NotificacaoModel> notificacoes = notificacaoRepository.findAll();
        notificacoes.forEach(notificacao -> notificacao.setLida(true));
        notificacaoRepository.saveAll(notificacoes);

        return listar();
    }

    @Transactional
    public NotificacaoDTOResponse reenviar(Long id) {
        NotificacaoModel notificacao = buscarNotificacao(id);
        ClienteModel cliente = notificacao.getCliente();
        String telefone = cliente != null ? cliente.getTelefone() : notificacao.getTelefoneDestino();

        boolean registrada = registrarEnvio(telefone, notificacao.getMensagem(), "reenvio da notificação " + id);

        notificacao.setStatus(registrada ? StatusNotificacao.enviada : StatusNotificacao.erro);
        notificacao.setTelefoneDestino(telefone);
        notificacao.setLida(false);

        if (registrada) {
            notificacao.setDataEnvio(LocalDateTime.now());
        }

        return converterParaResponse(notificacaoRepository.save(notificacao));
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

        boolean registrada = registrarEnvio(
                cliente != null ? cliente.getTelefone() : null,
                mensagem,
                "finalização da OS " + ordemServico.getId()
        );

        registrarNotificacao(
                ordemServico,
                TipoNotificacao.pos_servico,
                ASSUNTO_FINALIZACAO,
                mensagem,
                registrada ? StatusNotificacao.enviada : StatusNotificacao.erro,
                null,
                registrada ? LocalDateTime.now() : null
        );

        if (registrada) {
            return NotificacaoResultadoDTO.enviada("Notificação de finalização registrada para o cliente.");
        }

        return NotificacaoResultadoDTO.naoEnviada("Notificação de finalização não registrada: cliente sem telefone cadastrado.");
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
            boolean registrada = registrarLembreteRevisaoPreventiva(ordemServico, null);
            if (registrada) {
                if (ordemId != null) {
                    lembretesProcessados.add(ordemId);
                }

                return new NotificacaoResultadoDTO(
                        true,
                        false,
                        true,
                        agora,
                        "Data da revisão com menos de 7 dias; lembrete registrado imediatamente.",
                        null
                );
            }

            return NotificacaoResultadoDTO.naoEnviada("Lembrete de revisão não registrado: cliente sem telefone cadastrado.");
        }

        if (ordemId != null && lembretesAgendados.containsKey(ordemId)) {
            return NotificacaoResultadoDTO.agendada(dataEnvio, "Lembrete de revisão já estava agendado.");
        }

        NotificacaoModel agendada = registrarOuAtualizarAgendamento(ordemServico, dataEnvio);
        Long notificacaoId = agendada != null ? agendada.getId() : null;

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> {
                    try {
                        boolean registrada = registrarLembreteRevisaoPreventiva(ordemServico, notificacaoId);
                        if (registrada && ordemId != null) {
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

    private boolean registrarLembreteRevisaoPreventiva(OrdemServicoModel ordemServico, Long notificacaoAgendadaId) {
        ClienteModel cliente = ordemServico.getCliente();
        String mensagem = montarMensagemRevisaoPreventiva(ordemServico);

        boolean registrada = registrarEnvio(
                cliente != null ? cliente.getTelefone() : null,
                mensagem,
                "lembrete de revisão da OS " + ordemServico.getId()
        );

        if (notificacaoAgendadaId != null) {
            atualizarStatusNotificacaoAgendada(notificacaoAgendadaId, registrada);
            return registrada;
        }

        registrarNotificacao(
                ordemServico,
                TipoNotificacao.revisao_preventiva,
                ASSUNTO_REVISAO,
                mensagem,
                registrada ? StatusNotificacao.enviada : StatusNotificacao.erro,
                null,
                registrada ? LocalDateTime.now() : null
        );

        return registrada;
    }

    /**
     * Substitui o envio externo: apenas valida o destinatário e registra a mensagem no log.
     * Quando um gateway de mensagens for contratado, é este o único ponto a ser trocado.
     */
    private boolean registrarEnvio(String telefoneCliente, String mensagem, String contexto) {
        String destinatario = normalizarTelefone(telefoneCliente);
        if (destinatario == null) {
            log.warn("Notificação de {} não registrada como enviada: telefone do cliente ausente.", contexto);
            return false;
        }

        log.info("Notificação de {} registrada (envio externo desativado). Para: {} | Texto: {}",
                contexto,
                destinatario,
                mensagem
        );

        return true;
    }

    private String normalizarTelefone(String telefone) {
        String digitos = valorOuPadrao(telefone, "").replaceAll("[^0-9]", "");
        return digitos.isBlank() ? null : digitos;
    }

    private String montarMensagemRevisaoPreventiva(OrdemServicoModel ordemServico) {
        ClienteModel cliente = ordemServico.getCliente();
        VeiculoModel veiculo = ordemServico.getVeiculo();

        return String.format(
                "%s Passando para lembrar que a revisão preventiva do veículo %s está prevista para %s. " +
                        "Recomendamos entrar em contato com a oficina para confirmar o agendamento.",
                montarSaudacao(cliente != null ? cliente.getNome() : null),
                montarDescricaoVeiculoParaMensagem(veiculo),
                ordemServico.getDataProximaRevisao().format(DATA_PT_BR)
        );
    }

    private NotificacaoModel registrarOuAtualizarAgendamento(OrdemServicoModel ordemServico,
                                                             LocalDateTime dataAgendamento) {
        String mensagem = montarMensagemRevisaoPreventiva(ordemServico);
        Long ordemId = ordemServico.getId();

        if (ordemId != null) {
            NotificacaoModel pendente = notificacaoRepository
                    .findFirstByOrdemServicoIdAndTipoAndStatus(
                            ordemId,
                            TipoNotificacao.revisao_preventiva,
                            StatusNotificacao.pendente
                    )
                    .orElse(null);

            if (pendente != null) {
                pendente.setMensagem(mensagem);
                pendente.setDataAgendamento(dataAgendamento);
                return notificacaoRepository.save(pendente);
            }
        }

        return registrarNotificacao(
                ordemServico,
                TipoNotificacao.revisao_preventiva,
                ASSUNTO_REVISAO,
                mensagem,
                StatusNotificacao.pendente,
                dataAgendamento,
                null
        );
    }

    private NotificacaoModel registrarNotificacao(OrdemServicoModel ordemServico,
                                                  TipoNotificacao tipo,
                                                  String assunto,
                                                  String mensagem,
                                                  StatusNotificacao status,
                                                  LocalDateTime dataAgendamento,
                                                  LocalDateTime dataEnvio) {
        ClienteModel cliente = ordemServico != null ? ordemServico.getCliente() : null;
        if (cliente == null) {
            log.warn("Notificação não registrada: ordem de serviço sem cliente associado.");
            return null;
        }

        try {
            NotificacaoModel notificacao = new NotificacaoModel();
            notificacao.setCliente(cliente);
            notificacao.setVeiculo(ordemServico.getVeiculo());
            notificacao.setOrdemServico(ordemServico);
            notificacao.setTipo(tipo);
            notificacao.setAssunto(assunto);
            notificacao.setMensagem(mensagem);
            notificacao.setCanal(CanalNotificacao.whatsapp);
            notificacao.setStatus(status);
            notificacao.setTelefoneDestino(cliente.getTelefone());
            notificacao.setLida(false);
            notificacao.setDataAgendamento(dataAgendamento);
            notificacao.setDataEnvio(dataEnvio);

            return notificacaoRepository.save(notificacao);
        } catch (Exception e) {
            log.error("Erro ao registrar notificação no histórico: {}", e.getMessage());
            return null;
        }
    }

    private void atualizarStatusNotificacaoAgendada(Long notificacaoId, boolean registrada) {
        try {
            notificacaoRepository.findById(notificacaoId).ifPresent(notificacao -> {
                notificacao.setStatus(registrada ? StatusNotificacao.enviada : StatusNotificacao.erro);
                if (registrada) {
                    notificacao.setDataEnvio(LocalDateTime.now());
                }
                notificacaoRepository.save(notificacao);
            });
        } catch (Exception e) {
            log.error("Erro ao atualizar notificação agendada {}: {}", notificacaoId, e.getMessage());
        }
    }

    private NotificacaoModel buscarNotificacao(Long id) {
        return notificacaoRepository.findWithRelacionamentosById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificação não encontrada"));
    }

    private NotificacaoDTOResponse converterParaResponse(NotificacaoModel notificacao) {
        ClienteModel cliente = notificacao.getCliente();
        VeiculoModel veiculo = notificacao.getVeiculo();
        OrdemServicoModel ordemServico = notificacao.getOrdemServico();

        return new NotificacaoDTOResponse(
                notificacao.getId(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : null,
                cliente != null ? cliente.getTelefone() : notificacao.getTelefoneDestino(),
                veiculo != null ? veiculo.getId() : null,
                montarDescricaoVeiculoParaTela(veiculo),
                veiculo != null ? veiculo.getPlaca() : null,
                ordemServico != null ? ordemServico.getId() : null,
                ordemServico != null && ordemServico.getStatus() != null ? ordemServico.getStatus().name() : null,
                notificacao.getTipo() != null ? notificacao.getTipo().name() : null,
                notificacao.getAssunto(),
                notificacao.getMensagem(),
                notificacao.getCanal() != null ? notificacao.getCanal().name() : null,
                notificacao.getStatus() != null ? notificacao.getStatus().name() : null,
                notificacao.getLida(),
                notificacao.getDataCriacao(),
                notificacao.getDataAgendamento(),
                notificacao.getDataEnvio(),
                ordemServico != null ? ordemServico.getDataProximaRevisao() : null
        );
    }

    private String montarDescricaoVeiculoParaTela(VeiculoModel veiculo) {
        if (veiculo == null) {
            return null;
        }

        String modelo = valorOuPadrao(veiculo.getModelo(), "");
        String placa = valorOuPadrao(veiculo.getPlaca(), "");

        if (!modelo.isBlank() && !placa.isBlank()) {
            return modelo + " (" + placa + ")";
        }

        if (!placa.isBlank()) {
            return placa;
        }

        return modelo.isBlank() ? null : modelo;
    }

    private String montarSaudacao(String nomeCliente) {
        String nome = valorOuPadrao(nomeCliente, "");
        return nome.isBlank()
                ? "Olá! Aqui é da RRmax oficina mecânica."
                : "Olá, " + nome + "! Aqui é da RRmax oficina mecânica.";
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
