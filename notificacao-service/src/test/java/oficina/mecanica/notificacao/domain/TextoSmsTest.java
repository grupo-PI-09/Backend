package oficina.mecanica.notificacao.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextoSmsTest {

    @Test
    void removeAcentosMantendoOTamanho() {
        String original = "RRmax Oficina: Olá, João! O serviço do veículo ABC1D23 foi finalizado e a garantia já está ativa.";

        String sms = TextoSms.paraGsm7(original);

        assertEquals("RRmax Oficina: Ola, Joao! O servico do veiculo ABC1D23 foi finalizado e a garantia ja esta ativa.", sms);
        assertEquals(original.length(), sms.length());
    }

    @Test
    void trocaCaracteresQueOGsm7NaoTemOuCobraEmDobro() {
        assertEquals("\"Oi\" - (teste) ok / 'x'...", TextoSms.paraGsm7("“Oi” — [teste] ok | ‘x’…"));
    }

    @Test
    void removeEmoji() {
        assertEquals("Carro pronto ", TextoSms.paraGsm7("Carro pronto 🚗"));
    }

    @Test
    void contaSegmentos() {
        assertEquals(1, TextoSms.segmentos("a".repeat(160)));
        assertEquals(2, TextoSms.segmentos("a".repeat(161)));
        assertEquals(2, TextoSms.segmentos("a".repeat(306)));
        assertEquals(3, TextoSms.segmentos("a".repeat(307)));
    }
}
