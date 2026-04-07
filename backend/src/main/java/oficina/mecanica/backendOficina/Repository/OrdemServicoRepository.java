package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;

import java.util.List;

public interface OrdemServicoRepository extends JpaRepository<OrdemServicoModel, Long> {

    List<OrdemServicoModel> findByClienteId(Long clienteId);

    List<OrdemServicoModel> findByVeiculoId(Long veiculoId);

    List<OrdemServicoModel> findByStatus(StatusOrdemServico status);
}