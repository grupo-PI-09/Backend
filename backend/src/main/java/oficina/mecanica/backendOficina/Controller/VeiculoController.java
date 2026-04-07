package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.VeiculoDTORequest;
import oficina.mecanica.backendOficina.DTO.VeiculoDTOResponse;
import oficina.mecanica.backendOficina.Service.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VeiculoDTOResponse>> getVeiculos() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoDTOResponse> getVeiculoById(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<VeiculoDTOResponse>> getVeiculosByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.buscarPorClienteId(clienteId));
    }

    @PostMapping
    public ResponseEntity<VeiculoDTOResponse> criarVeiculo(@RequestBody @Valid VeiculoDTORequest dto) {
        return ResponseEntity.status(201).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeiculoDTOResponse> atualizarVeiculo(@PathVariable Long id,
                                                               @RequestBody @Valid VeiculoDTORequest dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarVeiculo(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}