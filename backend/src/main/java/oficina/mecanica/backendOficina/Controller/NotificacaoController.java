package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.NotificacaoDTOResponse;
import oficina.mecanica.backendOficina.DTO.PaginaCursorResponse;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.TipoServico;
import oficina.mecanica.backendOficina.Service.NotificacaoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/paginado")
    public ResponseEntity<PaginaCursorResponse<NotificacaoDTOResponse>> getNotificacoesPaginado(
            @RequestParam(required = false) Boolean lida,
            @RequestParam(required = false) TipoServico tipoServico,
            @RequestParam(required = false) StatusOrdemServico statusOs,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "" + NotificacaoService.TAMANHO_PADRAO) int size) {
        return ResponseEntity.ok(service.listarPorCursor(lida, tipoServico, statusOs, dataInicio, dataFim, cursor, size));
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
