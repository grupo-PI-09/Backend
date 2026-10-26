package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.NotificacaoModel;
import oficina.mecanica.backendOficina.Model.StatusNotificacao;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.TipoNotificacao;
import oficina.mecanica.backendOficina.Model.TipoServico;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
    @Query("""
            select n
            from NotificacaoModel n
            left join n.ordemServico o
            where (:lida is null or n.lida = :lida)
              and (:tipoServico is null or o.tipoServico = :tipoServico)
              and (:statusOs is null or o.status = :statusOs)
              and (:inicio is null or coalesce(n.dataEnvio, n.dataAgendamento, n.dataCriacao) >= :inicio)
              and (:fim is null or coalesce(n.dataEnvio, n.dataAgendamento, n.dataCriacao) < :fim)
              and (:cursorData is null
                   or coalesce(n.dataEnvio, n.dataAgendamento, n.dataCriacao) < :cursorData
                   or (coalesce(n.dataEnvio, n.dataAgendamento, n.dataCriacao) = :cursorData
                       and n.id < :cursorId))
            order by coalesce(n.dataEnvio, n.dataAgendamento, n.dataCriacao) desc, n.id desc
            """)
    List<NotificacaoModel> listarPorCursor(@Param("lida") Boolean lida,
                                           @Param("tipoServico") TipoServico tipoServico,
                                           @Param("statusOs") StatusOrdemServico statusOs,
                                           @Param("inicio") LocalDateTime inicio,
                                           @Param("fim") LocalDateTime fim,
                                           @Param("cursorData") LocalDateTime cursorData,
                                           @Param("cursorId") Long cursorId,
                                           Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "veiculo", "ordemServico"})
    Optional<NotificacaoModel> findWithRelacionamentosById(Long id);

    Optional<NotificacaoModel> findFirstByOrdemServicoIdAndTipoAndStatus(Long ordemServicoId,
                                                                        TipoNotificacao tipo,
                                                                        StatusNotificacao status);

    List<NotificacaoModel> findByOrdemServicoIdAndTipoAndStatus(Long ordemServicoId,
                                                                TipoNotificacao tipo,
                                                                StatusNotificacao status);

    long countByStatus(StatusNotificacao status);

    long countByLidaFalse();
}
