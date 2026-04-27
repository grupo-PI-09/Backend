package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.ConsultaPlacaResponse;
import oficina.mecanica.backendOficina.Service.VeiculoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/placas")
public class PlacaController {

    private final VeiculoService veiculoService;

    public PlacaController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping("/{placa}")
    public ResponseEntity<ConsultaPlacaResponse> consultar(@PathVariable String placa) {
        return ResponseEntity.ok(veiculoService.consultarPorPlaca(placa));
    }
}
