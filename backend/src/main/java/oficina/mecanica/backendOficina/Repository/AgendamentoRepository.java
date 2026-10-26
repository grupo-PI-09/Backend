package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.AgendamentoModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<AgendamentoModel, Long> {

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<AgendamentoModel> findAllByOrderByDataHoraAsc();

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<AgendamentoModel> findByClienteId(Long clienteId);

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<AgendamentoModel> findByVeiculoId(Long veiculoId);

    @EntityGraph(attributePaths = {"cliente", "veiculo"})
    List<AgendamentoModel> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
}
