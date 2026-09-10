package oficina.mecanica.backendOficina.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import oficina.mecanica.backendOficina.Model.UsuarioModel;
import oficina.mecanica.backendOficina.Repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractUsername(token);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsuarioModel usuario = usuarioRepository.findByEmail(email).orElse(null);

                if (usuario == null) {
                    // OWASP A09: token assinado para um usuario que nao existe mais.
                    log.warn("Token valido apresentado para usuario inexistente '{}' (IP {})",
                            email, request.getRemoteAddr());
                } else if (!Boolean.TRUE.equals(usuario.getAtivo())) {
                    // OWASP A01/A07: conta desativada nao autentica, mesmo com token valido.
                    log.warn("Acesso negado: usuario '{}' esta desativado (IP {})",
                            email, request.getRemoteAddr());
                } else if (jwtService.isTokenValid(token, usuario.getEmail())) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(usuario, null, authorities(usuario));
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // OWASP A09: token invalido/expirado/forjado deixa de ser descartado em silencio.
            SecurityContextHolder.clearContext();
            log.warn("Token JWT rejeitado em {} {} (IP {}): {}",
                    request.getMethod(), request.getRequestURI(),
                    request.getRemoteAddr(), ex.getClass().getSimpleName());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * OWASP A01: as permissoes passam a vir do perfil gravado no usuario.
     * Antes o contexto era criado com uma lista vazia de authorities, o que
     * tornava qualquer regra baseada em papel inoperante.
     */
    private List<SimpleGrantedAuthority> authorities(UsuarioModel usuario) {
        if (usuario.getPerfil() == null) {
            return List.of();
        }

        return List.of(new SimpleGrantedAuthority(
                "ROLE_" + usuario.getPerfil().name().toUpperCase()));
    }
}
