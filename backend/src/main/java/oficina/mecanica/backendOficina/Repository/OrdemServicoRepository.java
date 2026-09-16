package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.TipoServico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServicoModel, Long> {

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<OrdemServicoModel> findAllByOrderByDataAberturaDescIdDesc();

    List<OrdemServicoModel> findByClienteId(Long clienteId);

    List<OrdemServicoModel> findByVeiculoId(Long veiculoId);

    List<OrdemServicoModel> findByStatus(StatusOrdemServico status);

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<OrdemServicoModel> findByStatusAndDataProximaRevisaoBetween(StatusOrdemServico status,
                                                                     LocalDateTime inicio,
                                                                     LocalDateTime fim);

    // ---------------------------------------------------------------------
    // Dashboard financeira
    // ---------------------------------------------------------------------

    /** Faturamento (valor_total) das ordens com o status informado fechadas no intervalo. */
    @Query("""
            select coalesce(sum(o.valorTotal), 0)
            from OrdemServicoModel o
            where o.status = :status
              and o.dataFechamento between :inicio and :fim
            """)
    BigDecimal somarFaturamentoPorStatusEDataFechamento(@Param("status") StatusOrdemServico status,
                                                        @Param("inicio") LocalDateTime inicio,
                                                        @Param("fim") LocalDateTime fim);

    /**
     * Faturamento agrupado por ano/mês de fechamento.
     * Cada linha é {@code [ano (Integer), mes (Integer), total (BigDecimal)]};
     * meses sem ordens não aparecem no resultado.
     */
    @Query("""
            select year(o.dataFechamento), month(o.dataFechamento), coalesce(sum(o.valorTotal), 0)
            from OrdemServicoModel o
            where o.status = :status
              and o.dataFechamento between :inicio and :fim
            group by year(o.dataFechamento), month(o.dataFechamento)
            order by year(o.dataFechamento), month(o.dataFechamento)
            """)
    List<Object[]> somarFaturamentoPorMes(@Param("status") StatusOrdemServico status,
                                          @Param("inicio") LocalDateTime inicio,
                                          @Param("fim") LocalDateTime fim);

    /** Ordens do tipo abertas no intervalo, ignorando o status informado (ex.: canceladas). */
    long countByTipoServicoAndStatusNotAndDataAberturaBetween(TipoServico tipoServico,
                                                              StatusOrdemServico statusIgnorado,
                                                              LocalDateTime inicio,
                                                              LocalDateTime fim);

    /** Ordens do tipo com o status informado fechadas no intervalo. */
    long countByTipoServicoAndStatusAndDataFechamentoBetween(TipoServico tipoServico,
                                                             StatusOrdemServico status,
                                                             LocalDateTime inicio,
                                                             LocalDateTime fim);
}
