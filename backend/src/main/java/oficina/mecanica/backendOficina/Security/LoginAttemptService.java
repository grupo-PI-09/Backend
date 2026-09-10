package oficina.mecanica.backendOficina.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controle de forca bruta no login (OWASP A07:2021).
 *
 * Mantem em memoria a contagem de tentativas falhas por chave (email + IP) e
 * bloqueia temporariamente a chave depois de N falhas dentro da janela.
 */
@Service
public class LoginAttemptService {

    private final int maxTentativas;
    private final Duration bloqueio;
    private final Map<String, Tentativa> tentativas = new ConcurrentHashMap<>();

    public LoginAttemptService(@Value("${app.login.max-tentativas:5}") int maxTentativas,
                               @Value("${app.login.bloqueio-minutos:15}") long bloqueioMinutos) {
        this.maxTentativas = maxTentativas;
        this.bloqueio = Duration.ofMinutes(bloqueioMinutos);
    }

    public boolean estaBloqueado(String chave) {
        Tentativa tentativa = tentativas.get(chave);

        if (tentativa == null) {
            return false;
        }

        if (Instant.now().isAfter(tentativa.expiraEm)) {
            tentativas.remove(chave);
            return false;
        }

        return tentativa.falhas >= maxTentativas;
    }

    public long minutosRestantes(String chave) {
        Tentativa tentativa = tentativas.get(chave);

        if (tentativa == null) {
            return 0;
        }

        long minutos = Duration.between(Instant.now(), tentativa.expiraEm).toMinutes();
        return Math.max(minutos, 1);
    }

    public void registrarFalha(String chave) {
        Instant agora = Instant.now();

        tentativas.compute(chave, (k, atual) -> {
            if (atual == null || agora.isAfter(atual.expiraEm)) {
                return new Tentativa(1, agora.plus(bloqueio));
            }

            return new Tentativa(atual.falhas + 1, atual.expiraEm);
        });
    }

    public void registrarSucesso(String chave) {
        tentativas.remove(chave);
    }

    private record Tentativa(int falhas, Instant expiraEm) {
    }
}
