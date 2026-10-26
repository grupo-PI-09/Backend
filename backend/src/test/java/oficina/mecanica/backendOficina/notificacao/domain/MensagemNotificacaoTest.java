package oficina.mecanica.backendOficina.notificacao.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MensagemNotificacaoTest {

    private static final LocalDateTime REVISAO = LocalDateTime.of(2026, 11, 20, 9, 0);

    @Test
    void finalizacaoCurtaUsaPrimeiroNomeModeloEPlaca() {
        assertEquals("RRmax Oficina: Olá, Ana! O serviço do Gol (ABC1D23) foi finalizado e a garantia já está ativa. "
                        + "Obrigado pela preferência!",
                MensagemNotificacao.finalizacaoCurta("Ana Maria Souza", "Gol", "ABC1D23"));
    }

    @Test
    void modeloLongoCaiParaPlacaAntesDeOmitirONome() {
        String curta = MensagemNotificacao.finalizacaoCurta(
                "Ana", "Volkswagen Gol 1.6 MSI Total Flex Trendline Manual 4 portas", "ABC1D23");

        assertEquals("RRmax Oficina: Olá, Ana! O serviço do veículo ABC1D23 foi finalizado e a garantia já está ativa. "
                + "Obrigado pela preferência!", curta);
    }

    @Test
    void semDadosAindaGeraTextoValido() {
        assertEquals("RRmax Oficina: Olá! O serviço do seu veículo foi finalizado e a garantia já está ativa. "
                        + "Obrigado pela preferência!",
                MensagemNotificacao.finalizacaoCurta(null, null, null));
    }

    @Test
    void textosCurtosNuncaPassamDeUmSegmento() {
        String nomeEnorme = "Maximiliano".repeat(20);
        String modeloEnorme = "Modelo".repeat(30);

        assertTrue(MensagemNotificacao.finalizacaoCurta(nomeEnorme, modeloEnorme, "ABC1D23").length()
                <= MensagemNotificacao.LIMITE_SMS);
        assertTrue(MensagemNotificacao.lembreteRevisaoCurto(nomeEnorme, modeloEnorme, "ABC1D23", REVISAO).length()
                <= MensagemNotificacao.LIMITE_SMS);
    }
}
