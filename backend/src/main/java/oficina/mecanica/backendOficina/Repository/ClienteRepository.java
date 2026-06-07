package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.ClienteModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ClienteRepository extends JpaRepository<ClienteModel, Long> {
    List<ClienteModel> findByNomeContainingIgnoreCase(String nome);

    long countByDataCadastroBetween(LocalDateTime inicio, LocalDateTime fim);
}
