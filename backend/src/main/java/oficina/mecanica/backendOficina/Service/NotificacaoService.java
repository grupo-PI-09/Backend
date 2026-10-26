package oficina.mecanica.backendOficina.Service;
import oficina.mecanica.backendOficina.DTO.NotificacaoDTOResponse;
import oficina.mecanica.backendOficina.DTO.PaginaCursorResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.NotificacaoModel;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.TipoServico;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Paginacao.CursorCodec;
import oficina.mecanica.backendOficina.Paginacao.CursorCodec.Cursor;
import oficina.mecanica.backendOficina.Repository.NotificacaoRepository;
import oficina.mecanica.backendOficina.notificacao.domain.Telefone;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoService {
    private final NotificacaoRepository notificacaoRepository;

    private final EnviadorDeMensagem enviador;

    public static final int TAMANHO_PADRAO = 8;
    public static final int TAMANHO_MAXIMO = 50;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, EnviadorDeMensagem enviador) {
        this.notificacaoRepository = notificacaoRepository;
        this.enviador = enviador;
        this.gateway = gateway;
    }

    public List<NotificacaoDTOResponse> listar() {
        return notificacaoRepository.listarMaisRecentesPrimeiro()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public PaginaCursorResponse<NotificacaoDTOResponse> listarPorCursor(Boolean lida,
                                                                        TipoServico tipoServico,
                                                                        StatusOrdemServico statusOs,
                                                                        LocalDate dataInicio,
                                                                        LocalDate dataFim,
                                                                        String cursor,
                                                                        int tamanho) {
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO);
        }

        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data final deve ser posterior à data inicial");
        }

        Cursor posicao = cursor == null || cursor.isBlank() ? null : CursorCodec.decodificar(cursor);

        List<NotificacaoModel> linhas = notificacaoRepository.listarPorCursor(
                lida,
                tipoServico,
                statusOs,
                dataInicio != null ? dataInicio.atStartOfDay() : null,
                // data final inclusiva: até o fim do dia = antes do início do dia seguinte
                dataFim != null ? dataFim.plusDays(1).atStartOfDay() : null,
                posicao != null ? posicao.data() : null,
                posicao != null ? posicao.id() : null,
                PageRequest.of(0, tamanho + 1));

        boolean temProxima = linhas.size() > tamanho;
        List<NotificacaoModel> pagina = temProxima ? linhas.subList(0, tamanho) : linhas;

        String proximoCursor = null;
        if (temProxima) {
            NotificacaoModel ultima = pagina.get(pagina.size() - 1);
            proximoCursor = CursorCodec.codificar(dataReferencia(ultima), ultima.getId());
        }

        return new PaginaCursorResponse<>(
                pagina.stream().map(this::converterParaResponse).toList(),
                proximoCursor,
                tamanho);
    }

    private LocalDateTime dataReferencia(NotificacaoModel notificacao) {
        if (notificacao.getDataEnvio() != null) {
            return notificacao.getDataEnvio();
        }

        return notificacao.getDataAgendamento() != null
                ? notificacao.getDataAgendamento()
                : notificacao.getDataCriacao();
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

        String destinatario = Telefone.normalizar(telefone);

        notificacao.setStatus(destinatario != null ? StatusNotificacao.pendente : StatusNotificacao.erro);
        notificacao.setTelefoneDestino(telefone);
        notificacao.setLida(false);

        NotificacaoModel salva = notificacaoRepository.save(notificacao);

        if (destinatario != null) {
            publicarReenvioAposCommit(id, destinatario, salva.getMensagem());
        }

        return converterParaResponse(salva);
    }

    private void publicarReenvioAposCommit(Long id, String destinatario, String mensagem) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                boolean aceita = enviador.enviar(id, destinatario, mensagem, "reenvio da notificação " + id);

                if (!aceita) {
                    gateway.registrarResultadoEnvio(id, false, null);
                }
            }
        });
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
                ordemServico != null && ordemServico.getTipoServico() != null ? ordemServico.getTipoServico().name() : null,
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

    private String valorOuPadrao(String valor, String padrao) {
        return valor != null && !valor.isBlank() ? valor.trim() : padrao;
    }
}