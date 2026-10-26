package oficina.mecanica.notificacao.domain;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelefoneE164Test {

    @Test
    void celularSemDdiRecebeCodigoDoBrasil() {
        assertEquals(Optional.of("+5511999998888"), TelefoneE164.converter("11999998888", "55"));
    }

    @Test
    void fixoSemDdiRecebeCodigoDoBrasil() {
        assertEquals(Optional.of("+551133334444"), TelefoneE164.converter("(11) 3333-4444", "55"));
    }

    @Test
    void numeroComDdiEFormatacaoEhMantido() {
        assertEquals(Optional.of("+5511999998888"), TelefoneE164.converter("+55 (11) 99999-8888", "55"));
    }

    @Test
    void zeroDeDiscagemEhRemovido() {
        assertEquals(Optional.of("+5511999998888"), TelefoneE164.converter("011 99999-8888", "55"));
    }

    @Test
    void numeroCurtoOuVazioEhInvalido() {
        assertTrue(TelefoneE164.converter("99998888", "55").isEmpty());
        assertTrue(TelefoneE164.converter("  ", "55").isEmpty());
        assertTrue(TelefoneE164.converter(null, "55").isEmpty());
    }
}
