package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.AuthCadastroRequest;
import oficina.mecanica.backendOficina.DTO.AuthLoginRequest;
import oficina.mecanica.backendOficina.DTO.AuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioAuthResponse;
import oficina.mecanica.backendOficina.Model.PerfilUsuario;
import oficina.mecanica.backendOficina.Model.UsuarioModel;
import oficina.mecanica.backendOficina.Repository.UsuarioRepository;
import oficina.mecanica.backendOficina.Security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse cadastrar(AuthCadastroRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setNome(request.getNome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setLogin(gerarLogin(emailNormalizado));
        usuario.setPerfil(PerfilUsuario.admin);
        usuario.setAtivo(true);

        UsuarioModel salvo = usuarioRepository.save(usuario);
        return gerarResposta(salvo);
    }

    public AuthResponse login(AuthLoginRequest request) {
        UsuarioModel usuario = usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos");
        }

        return gerarResposta(usuario);
    }

    private AuthResponse gerarResposta(UsuarioModel usuario) {
        String token = jwtService.generateToken(usuario);
        UsuarioAuthResponse usuarioResponse = new UsuarioAuthResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );

        return new AuthResponse(token, usuarioResponse);
    }

    private String gerarLogin(String email) {
        String base = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9._-]", "")
                .toLowerCase();

        if (base.isBlank()) {
            base = "usuario";
        }

        if (base.length() > 40) {
            base = base.substring(0, 40);
        }

        String sufixo = String.valueOf(System.currentTimeMillis());
        if (sufixo.length() > 6) {
            sufixo = sufixo.substring(sufixo.length() - 6);
        }

        return (base + "_" + sufixo).substring(0, Math.min(50, base.length() + 1 + sufixo.length()));
    }
}
