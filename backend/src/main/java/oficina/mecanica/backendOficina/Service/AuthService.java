package oficina.mecanica.backendOficina.Service;

import jakarta.servlet.http.HttpServletRequest;
import oficina.mecanica.backendOficina.DTO.AuthCadastroRequest;
import oficina.mecanica.backendOficina.DTO.AuthLoginRequest;
import oficina.mecanica.backendOficina.DTO.AuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioAuthResponse;
import oficina.mecanica.backendOficina.Model.PerfilUsuario;
import oficina.mecanica.backendOficina.Model.UsuarioModel;
import oficina.mecanica.backendOficina.Repository.UsuarioRepository;
import oficina.mecanica.backendOficina.Security.JwtService;
import oficina.mecanica.backendOficina.Security.LoginAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       LoginAttemptService loginAttemptService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Cadastro de usuario. O endpoint que chama este metodo exige perfil ADMIN
     * (ver SecurityConfig) e o perfil concedido nunca vem "admin" por padrao:
     * quem nao informar o perfil recebe o de menor privilegio (OWASP A01).
     */
    public UsuarioAuthResponse cadastrar(AuthCadastroRequest request) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setNome(request.getNome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setLogin(gerarLogin(emailNormalizado));
        usuario.setPerfil(resolverPerfil(request.getPerfil()));
        usuario.setAtivo(true);

        UsuarioModel salvo = usuarioRepository.save(usuario);
        log.info("Usuario '{}' criado com perfil {}", salvo.getEmail(), salvo.getPerfil());

        return converterParaResponse(salvo);
    }

    public AuthResponse login(AuthLoginRequest request, HttpServletRequest httpRequest) {
        String emailNormalizado = request.getEmail().trim().toLowerCase();
        String ip = httpRequest == null ? "desconhecido" : httpRequest.getRemoteAddr();
        String chave = emailNormalizado + "|" + ip;

        // OWASP A07: bloqueio temporario apos sucessivas falhas, por email+IP e
        // tambem so por email (com limite maior), para quem troca de IP a cada tentativa.
        String chaveBloqueada = loginAttemptService.estaBloqueado(chave) ? chave
                : loginAttemptService.estaBloqueadoPorEmail(emailNormalizado) ? emailNormalizado
                : null;
        if (chaveBloqueada != null) {
            log.warn("Login bloqueado por excesso de tentativas: '{}' (IP {})", emailNormalizado, ip);
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Tente novamente em "
                            + loginAttemptService.minutosRestantes(chaveBloqueada) + " minuto(s)"
            );
        }

        UsuarioModel usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);

        // A mesma resposta para email inexistente e senha errada evita
        // enumeracao de contas (OWASP A07).
        if (usuario == null || !passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            loginAttemptService.registrarFalha(chave);
            loginAttemptService.registrarFalha(emailNormalizado);
            log.warn("Falha de autenticacao para '{}' (IP {})", emailNormalizado, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos");
        }

        // OWASP A01: conta desativada nao entra.
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            loginAttemptService.registrarFalha(chave);
            log.warn("Login recusado: usuario '{}' esta desativado (IP {})", emailNormalizado, ip);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário desativado");
        }

        loginAttemptService.registrarSucesso(chave);
        loginAttemptService.registrarSucesso(emailNormalizado);
        log.info("Login realizado por '{}' (IP {})", usuario.getEmail(), ip);

        return gerarResposta(usuario);
    }

    private PerfilUsuario resolverPerfil(String perfilSolicitado) {
        if (perfilSolicitado == null || perfilSolicitado.isBlank()) {
            return PerfilUsuario.mecanico;
        }

        try {
            return PerfilUsuario.valueOf(perfilSolicitado.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil inválido: use admin ou mecanico");
        }
    }

    private AuthResponse gerarResposta(UsuarioModel usuario) {
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token, converterParaResponse(usuario));
    }

    private UsuarioAuthResponse converterParaResponse(UsuarioModel usuario) {
        return new UsuarioAuthResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
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

        // Sufixo aleatorio conferido no banco: o antigo, derivado do relogio,
        // se repetia a cada ~16 minutos e podia violar o unique da coluna.
        for (int tentativa = 0; tentativa < 10; tentativa++) {
            String login = base + "_" + String.format("%06d", RANDOM.nextInt(1_000_000));
            if (!usuarioRepository.existsByLogin(login)) {
                return login;
            }
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível gerar um login único");
    }
}
