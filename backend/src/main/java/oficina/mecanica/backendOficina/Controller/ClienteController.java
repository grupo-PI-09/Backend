package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping

public class ClienteController {
    private ClienteService service;

    @GetMapping
    public List<ClienteModel> listar(@RequestParam(required = false) String nome) {
        if (nome != null && !nome.isEmpty()) {
            return service.buscarPorNome(nome);
        }
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus
    public ClienteModel cadastrar(@RequestBody ClienteModel request) {
        return service.salvar(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteModel> editar(@PathVariable Long id, @RequestBody ClienteModel cliente) {
        ClienteModel atualizado = service.atualizar(id, cliente);

        if (atualizado != null) {
            return ResponseEntity.ok(atualizado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        boolean excluido = service.excluir(id);

        if (excluido) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
