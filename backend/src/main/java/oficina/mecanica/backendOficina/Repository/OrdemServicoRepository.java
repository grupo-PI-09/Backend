package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServicoModel, Long> {

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<OrdemServicoModel> findAllByOrderByDataAberturaDesc();

    List<OrdemServicoModel> findByClienteId(Long clienteId);

    List<OrdemServicoModel> findByVeiculoId(Long veiculoId);

    List<OrdemServicoModel> findByStatus(StatusOrdemServico status);

    long countByStatus(StatusOrdemServico status);

    long countByStatusIn(Collection<StatusOrdemServico> status);

    long countByStatusAndDataFechamentoBetween(StatusOrdemServico status, LocalDateTime inicio, LocalDateTime fim);

    long countByDataProximaRevisaoBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("select coalesce(sum(o.valorTotal), 0) from OrdemServicoModel o")
    BigDecimal somarFaturamentoTotal();

    @Query("""
            select coalesce(sum(o.valorTotal), 0)
            from OrdemServicoModel o
            where o.status = :status
              and o.dataFechamento between :inicio and :fim
            """)
    BigDecimal somarFaturamentoPorStatusEDataFechamento(@Param("status") StatusOrdemServico status,
                                                        @Param("inicio") LocalDateTime inicio,
                                                        @Param("fim") LocalDateTime fim);

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<OrdemServicoModel> findTop5ByOrderByDataAberturaDesc();

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<OrdemServicoModel> findTop5ByDataProximaRevisaoBetweenOrderByDataProximaRevisaoAsc(LocalDateTime inicio,
                                                                                            LocalDateTime fim);

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<OrdemServicoModel> findByStatusAndDataProximaRevisaoBetween(StatusOrdemServico status,
                                                                     LocalDateTime inicio,
                                                                     LocalDateTime fim);
}
