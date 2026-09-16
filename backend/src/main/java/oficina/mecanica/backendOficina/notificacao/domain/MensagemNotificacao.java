package oficina.mecanica.backendOficina.notificacao.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Textos enviados ao cliente. Concentra a redação das mensagens da oficina
 * para que nenhuma camada externa precise conhecer o formato.
 */
public final class MensagemNotificacao {

    public static final String ASSUNTO_FINALIZACAO = "Serviço finalizado";
    public static final String ASSUNTO_REVISAO = "Lembrete de revisão preventiva";

    private static final DateTimeFormatter DATA_PT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private MensagemNotificacao() {
    }

    public static String finalizacao(String nomeCliente, String modeloVeiculo, String placaVeiculo) {
        return String.format(
                "%s O serviço do veículo %s foi finalizado pela oficina. " +
                        "A garantia do serviço já está ativa conforme as condições informadas. Obrigado pela preferência!",
                saudacao(nomeCliente),
                descricaoVeiculo(modeloVeiculo, placaVeiculo)
        );
    }

    public static String lembreteRevisao(String nomeCliente,
                                         String modeloVeiculo,
                                         String placaVeiculo,
                                         LocalDateTime dataRevisao) {
        return String.format(
                "%s Passando para lembrar que a revisão preventiva do veículo %s está prevista para %s. " +
                        "Recomendamos entrar em contato com a oficina para confirmar o agendamento.",
                saudacao(nomeCliente),
                descricaoVeiculo(modeloVeiculo, placaVeiculo),
                dataRevisao.format(DATA_PT_BR)
        );
    }

    private static String saudacao(String nomeCliente) {
        String nome = valorOuPadrao(nomeCliente, "");
        return nome.isBlank()
                ? "Olá! Aqui é da RRmax oficina mecânica."
                : "Olá, " + nome + "! Aqui é da RRmax oficina mecânica.";
    }

    private static String descricaoVeiculo(String modeloVeiculo, String placaVeiculo) {
        String modelo = valorOuPadrao(modeloVeiculo, "");
        String placa = valorOuPadrao(placaVeiculo, "");

        if (!modelo.isBlank() && !placa.isBlank()) {
            return modelo + " / placa " + placa;
        }

        if (!placa.isBlank()) {
            return "placa " + placa;
        }

        if (!modelo.isBlank()) {
            return modelo;
        }

        return "não informado";
    }

    private static String valorOuPadrao(String valor, String padrao) {
        return valor != null && !valor.isBlank() ? valor.trim() : padrao;
    }
}
