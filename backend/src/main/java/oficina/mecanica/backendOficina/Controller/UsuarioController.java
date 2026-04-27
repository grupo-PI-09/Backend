package oficina.mecanica.backendOficina.Controller;

import jakarta.validation.Valid;
import oficina.mecanica.backendOficina.DTO.AuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioAuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioUpdateRequest;
import oficina.mecanica.backendOficina.Model.UsuarioModel;
import oficina.mecanica.backendOficina.Service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioAuthResponse> buscarUsuarioAtual(@AuthenticationPrincipal UsuarioModel usuarioAutenticado) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioAtual(usuarioAutenticado));
    }

    @PutMapping("/me")
    public ResponseEntity<AuthResponse> atualizarUsuarioAtual(@AuthenticationPrincipal UsuarioModel usuarioAutenticado,
                                                              @RequestBody @Valid UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarUsuarioAtual(usuarioAutenticado, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> excluirUsuarioAtual(@AuthenticationPrincipal UsuarioModel usuarioAutenticado) {
        usuarioService.excluirUsuarioAtual(usuarioAutenticado);
        return ResponseEntity.noContent().build();
    }
}
