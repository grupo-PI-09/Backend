package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.VeiculoModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VeiculoRepository extends JpaRepository<VeiculoModel, Long> {

    List<VeiculoModel> findByPlacaContainingIgnoreCase(String placa);

    List<VeiculoModel> findByModeloContainingIgnoreCase(String modelo);

    List<VeiculoModel> findByMarcaContainingIgnoreCase(String marca);

    List<VeiculoModel> findByAtivo(Boolean ativo);

    List<VeiculoModel> findByClienteId(Long clienteId);

    @EntityGraph(attributePaths = "cliente")
    List<VeiculoModel> findByClienteIdIn(Collection<Long> clienteIds);
}
