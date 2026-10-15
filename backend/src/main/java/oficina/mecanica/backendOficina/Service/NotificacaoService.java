package oficina.mecanica.backendOficina.Service;
import oficina.mecanica.backendOficina.DTO.NotificacaoDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.NotificacaoModel;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Repository.NotificacaoRepository;
import oficina.mecanica.backendOficina.notificacao.domain.Telefone;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import oficina.mecanica.backendOficina.notificacao.usecase.port.NotificacaoGateway;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.List;

@Service
public class NotificacaoService {
    private final NotificacaoRepository notificacaoRepository;

    private final EnviadorDeMensagem enviador;

    private final NotificacaoGateway gateway;

    public NotificacaoService(NotificacaoRepository notificacaoRepository,
                              EnviadorDeMensagem enviador,
                              NotificacaoGateway gateway) {
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