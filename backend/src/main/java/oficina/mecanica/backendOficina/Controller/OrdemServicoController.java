package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.OrdemServicoDTORequest;
import oficina.mecanica.backendOficina.DTO.OrdemServicoDTOResponse;
import oficina.mecanica.backendOficina.Service.OrdemServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordens-servico")
@CrossOrigin("*")
public class OrdemServicoController {

    private final OrdemServicoService service;

    public OrdemServicoController(OrdemServicoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrdemServicoDTOResponse> criar(@RequestBody OrdemServicoDTORequest request) {
        OrdemServicoDTOResponse resposta = service.criarOrdemServico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public ResponseEntity<List<OrdemServicoDTOResponse>> listar() {
        return ResponseEntity.ok(service.listarOrdensServico());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoDTOResponse> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.buscarOrdemServicoPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdemServicoDTOResponse> atualizar(@PathVariable Integer id,
                                                             @RequestBody OrdemServicoDTORequest request) {
        try {
            return ResponseEntity.ok(service.atualizarOrdemServico(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        try {
            service.deletarOrdemServico(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
