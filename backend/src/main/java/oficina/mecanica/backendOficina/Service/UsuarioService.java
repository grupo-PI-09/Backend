package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.AuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioAuthResponse;
import oficina.mecanica.backendOficina.DTO.UsuarioUpdateRequest;
import oficina.mecanica.backendOficina.Model.PerfilUsuario;
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

        boolean trocaSenha = request.getSenha() != null && !request.getSenha().isBlank();
        boolean trocaEmail = !emailNormalizado.equals(usuario.getEmail());

        if (trocaSenha || trocaEmail) {
            String senhaAtual = request.getSenhaAtual();
            if (senhaAtual == null || senhaAtual.isBlank()
                    || !passwordEncoder.matches(senhaAtual, usuario.getSenha())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Informe a senha atual correta para alterar email ou senha");
            }
        }

        usuario.setNome(request.getNome().trim());
        usuario.setEmail(emailNormalizado);

        if (trocaSenha) {
            usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        UsuarioModel atualizado = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(atualizado);
        return new AuthResponse(token, converterParaResponse(atualizado));
    }

    public void excluirUsuarioAtual(UsuarioModel usuarioAutenticado) {
        UsuarioModel usuario = buscarUsuarioValido(usuarioAutenticado);

        // Sem nenhum admin ativo ninguem mais cadastra usuarios, e o AdminBootstrap
        // so roda com a tabela vazia.
        if (usuario.getPerfil() == PerfilUsuario.admin && Boolean.TRUE.equals(usuario.getAtivo())
                && usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.admin) <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Você é o único administrador ativo e não pode excluir sua conta");
        }

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
