package oficina.mecanica.backendOficina.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import oficina.mecanica.backendOficina.DTO.AuthCadastroRequest;
import oficina.mecanica.backendOficina.DTO.AuthLoginRequest;
import oficina.mecanica.backendOficina.DTO.AuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioAuthResponse;
import oficina.mecanica.backendOficina.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * OWASP A01: cadastro deixou de ser publico. Apenas um administrador
     * autenticado cria novos usuarios, e a resposta nao devolve token do
     * usuario criado.
     */
    @PostMapping("/cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioAuthResponse> cadastrar(@RequestBody @Valid AuthCadastroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest request,
                                              HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }
}
