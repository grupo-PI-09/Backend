package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.NotificacaoDTOResponse;
import oficina.mecanica.backendOficina.Service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoDTOResponse>> getNotificacoes() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<Map<String, Long>> contarNaoLidas() {
        return ResponseEntity.ok(Map.of("naoLidas", service.contarNaoLidas()));
    }

    @PutMapping("/{id}/lida")
    public ResponseEntity<NotificacaoDTOResponse> marcarComoLida(@PathVariable Long id,
                                                                 @RequestParam(defaultValue = "true") boolean lida) {
        return ResponseEntity.ok(service.marcarComoLida(id, lida));
    }

    @PutMapping("/lidas")
    public ResponseEntity<List<NotificacaoDTOResponse>> marcarTodasComoLidas() {
        return ResponseEntity.ok(service.marcarTodasComoLidas());
    }

    @PostMapping("/{id}/reenviar")
    public ResponseEntity<NotificacaoDTOResponse> reenviar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reenviar(id));
    }
}
