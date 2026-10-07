package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.ClienteComVeiculosDTOResponse;
import oficina.mecanica.backendOficina.DTO.ClienteDTORequest;
import oficina.mecanica.backendOficina.DTO.ClienteDTOResponse;
import oficina.mecanica.backendOficina.DTO.PaginaOffsetResponse;
import oficina.mecanica.backendOficina.Paginacao.PaginacaoOffset;
import oficina.mecanica.backendOficina.Service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTOResponse>> getClientes() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/paginado")
    public ResponseEntity<PaginaOffsetResponse<ClienteComVeiculosDTOResponse>> getClientesPaginado(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "" + PaginacaoOffset.TAMANHO_PADRAO) int size,
            @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(service.listarPaginado(page, size, busca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTOResponse> getClienteById(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClienteDTOResponse> criarCliente(@RequestBody @Valid ClienteDTORequest dto) {
        return ResponseEntity.status(201).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTOResponse> atualizarCliente(@PathVariable Long id,
                                                               @RequestBody @Valid ClienteDTORequest dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}