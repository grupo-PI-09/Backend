package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.OrdemServicoDTORequest;
import oficina.mecanica.backendOficina.DTO.OrdemServicoDTOResponse;
import oficina.mecanica.backendOficina.Service.OrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens")
public class OrdemServicoController {

    private final OrdemServicoService service;

    public OrdemServicoController(OrdemServicoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<OrdemServicoDTOResponse>> getOrdens() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoDTOResponse> getOrdemById(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<OrdemServicoDTOResponse>> getOrdensByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.buscarPorClienteId(clienteId));
    }

    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<List<OrdemServicoDTOResponse>> getOrdensByVeiculo(@PathVariable Long veiculoId) {
        return ResponseEntity.ok(service.buscarPorVeiculoId(veiculoId));
    }

    @PostMapping
    public ResponseEntity<OrdemServicoDTOResponse> criarOrdem(@RequestBody @Valid OrdemServicoDTORequest dto) {
        return ResponseEntity.status(201).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdemServicoDTOResponse> atualizarOrdem(@PathVariable Long id,
                                                                  @RequestBody @Valid OrdemServicoDTORequest dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarOrdem(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}