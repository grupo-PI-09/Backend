package oficina.mecanica.backendOficina.Controller;

import oficina.mecanica.backendOficina.DTO.ConsultaCepResponse;
import oficina.mecanica.backendOficina.Service.CepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ceps")
public class CepController {

    private final CepService cepService;

    public CepController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping("/{cep}")
    public ResponseEntity<ConsultaCepResponse> consultarCep(@PathVariable String cep) {
        return ResponseEntity.ok(cepService.consultar(cep));
    }
}
