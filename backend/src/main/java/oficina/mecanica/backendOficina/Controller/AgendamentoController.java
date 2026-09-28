package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.AgendamentoDTORequest;
import oficina.mecanica.backendOficina.DTO.AgendamentoDTOResponse;
import oficina.mecanica.backendOficina.Service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoDTOResponse>> getAgendamentos() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDTOResponse> getAgendamentoById(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AgendamentoDTOResponse>> getAgendamentosByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.buscarPorClienteId(clienteId));
    }

    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<List<AgendamentoDTOResponse>> getAgendamentosByVeiculo(@PathVariable Long veiculoId) {
        return ResponseEntity.ok(service.buscarPorVeiculoId(veiculoId));
    }

    @PostMapping
    public ResponseEntity<AgendamentoDTOResponse> criarAgendamento(@RequestBody @Valid AgendamentoDTORequest dto) {
        return ResponseEntity.status(201).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgendamentoDTOResponse> atualizarAgendamento(@PathVariable Long id,
                                                                        @RequestBody @Valid AgendamentoDTORequest dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAgendamento(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
