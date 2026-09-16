package oficina.mecanica.backendOficina.Config;
import oficina.mecanica.backendOficina.Model.PerfilUsuario;
import oficina.mecanica.backendOficina.Model.UsuarioModel;
import oficina.mecanica.backendOficina.Repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
/**
 * Cria o primeiro administrador a partir de variaveis de ambiente, e somente
 * quando ainda nao existe nenhum usuario cadastrado.
 *
 * Isso substitui o antigo cadastro publico que concedia perfil "admin" a
 * qualquer pessoa da internet (OWASP A01:2021). As credenciais nao ficam no
 * codigo nem no repositorio (OWASP A02:2021).
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String nome;
    private final String senha;

    public AdminBootstrap(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap.admin.email:}") String email,
                          @Value("${app.bootstrap.admin.nome:Administrador}") String nome,
                          @Value("${app.bootstrap.admin.senha:}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.nome = nome;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        if (email.isBlank() || senha.isBlank()) {
            log.warn("Nenhum usuario cadastrado e o admin inicial nao foi configurado. "
                    + "Defina APP_BOOTSTRAP_ADMIN_EMAIL e APP_BOOTSTRAP_ADMIN_SENHA para criar o primeiro acesso.");
            return;
        }

        String emailNormalizado = email.trim().toLowerCase();

        UsuarioModel admin = new UsuarioModel();
        admin.setNome(nome.trim());
        admin.setEmail(emailNormalizado);
        admin.setSenha(passwordEncoder.encode(senha));
        admin.setLogin("admin");
        admin.setPerfil(PerfilUsuario.admin);
        admin.setAtivo(true);

        usuarioRepository.save(admin);
        log.info("Administrador inicial '{}' criado. Troque a senha no primeiro acesso.", emailNormalizado);
    }
}
