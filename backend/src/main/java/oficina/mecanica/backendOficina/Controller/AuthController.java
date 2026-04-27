package oficina.mecanica.backendOficina.Controller;

import jakarta.validation.Valid;
import oficina.mecanica.backendOficina.DTO.AuthCadastroRequest;
import oficina.mecanica.backendOficina.DTO.AuthLoginRequest;
import oficina.mecanica.backendOficina.DTO.AuthResponse;
import oficina.mecanica.backendOficina.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<AuthResponse> cadastrar(@RequestBody @Valid AuthCadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
