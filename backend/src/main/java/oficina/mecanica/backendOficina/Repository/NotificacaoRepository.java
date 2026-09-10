package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.NotificacaoModel;
import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<NotificacaoModel, Long> {

    @EntityGraph(attributePaths = {"cliente", "veiculo", "ordemServico"})
    @Query("""
            select n
            from NotificacaoModel n
            order by coalesce(n.dataEnvio, n.dataAgendamento, n.dataCriacao) desc, n.id desc
            """)
    List<NotificacaoModel> listarMaisRecentesPrimeiro();

    @EntityGraph(attributePaths = {"cliente", "veiculo", "ordemServico"})
    Optional<NotificacaoModel> findWithRelacionamentosById(Long id);

    Optional<NotificacaoModel> findFirstByOrdemServicoIdAndTipoAndStatus(Long ordemServicoId,
                                                                        TipoNotificacao tipo,
                                                                        StatusNotificacao status);

    long countByStatus(StatusNotificacao status);

    long countByLidaFalse();
}
