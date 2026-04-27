package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.AuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioAuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioUpdateRequest;
import oficina.mecanica.backendOficina.Model.UsuarioModel;
import oficina.mecanica.backendOficina.Repository.UsuarioRepository;
import oficina.mecanica.backendOficina.Security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UsuarioAuthResponse buscarUsuarioAtual(UsuarioModel usuarioAutenticado) {
        UsuarioModel usuario = buscarUsuarioValido(usuarioAutenticado);
        return converterParaResponse(usuario);
    }

    public AuthResponse atualizarUsuarioAtual(UsuarioModel usuarioAutenticado, UsuarioUpdateRequest request) {
        UsuarioModel usuario = buscarUsuarioValido(usuarioAutenticado);
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmailAndIdNot(emailNormalizado, usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        usuario.setNome(request.getNome().trim());
        usuario.setEmail(emailNormalizado);

        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        UsuarioModel atualizado = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(atualizado);
        return new AuthResponse(token, converterParaResponse(atualizado));
    }

    public void excluirUsuarioAtual(UsuarioModel usuarioAutenticado) {
        UsuarioModel usuario = buscarUsuarioValido(usuarioAutenticado);
        usuarioRepository.delete(usuario);
    }

    private UsuarioModel buscarUsuarioValido(UsuarioModel usuarioAutenticado) {
        if (usuarioAutenticado == null || usuarioAutenticado.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return usuarioRepository.findById(usuarioAutenticado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    private UsuarioAuthResponse converterParaResponse(UsuarioModel usuario) {
        return new UsuarioAuthResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}
