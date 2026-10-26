package oficina.mecanica.backendOficina.Repository;

import oficina.mecanica.backendOficina.Model.ClienteModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ClienteRepository extends JpaRepository<ClienteModel, Long> {
    List<ClienteModel> findByNomeContainingIgnoreCase(String nome);

    Page<ClienteModel> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    long countByDataCadastroBetween(LocalDateTime inicio, LocalDateTime fim);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
