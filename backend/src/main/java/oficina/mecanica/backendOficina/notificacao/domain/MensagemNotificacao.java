package oficina.mecanica.backendOficina.notificacao.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Textos enviados ao cliente. Concentra a redação das mensagens da oficina
 * para que nenhuma camada externa precise conhecer o formato.
 *
 * Cada mensagem tem uma versão completa (histórico, tela e WhatsApp) e uma
 * versão curta para SMS, que cabe em um único segmento.
 */
public final class MensagemNotificacao {

    public static final String ASSUNTO_FINALIZACAO = "Serviço finalizado";
    public static final String ASSUNTO_REVISAO = "Lembrete de revisão preventiva";

    /** Um segmento de SMS sem acentos (GSM-7). Acima disso, cada SMS é cobrado como 2 ou mais. */
    public static final int LIMITE_SMS = 160;

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

    /**
     * Versão para SMS da {@link #finalizacao}: cabe em {@value #LIMITE_SMS} caracteres
     * (1 segmento). Se o nome ou o modelo forem longos, eles são omitidos antes de cortar o texto.
     */
    public static String finalizacaoCurta(String nomeCliente, String modeloVeiculo, String placaVeiculo) {
        return primeiraQueCabe(saudacoesCurtas(nomeCliente), veiculosCurtos(modeloVeiculo, placaVeiculo),
                (saudacao, veiculo) -> "RRmax Oficina: " + saudacao + " O serviço do " + veiculo
                        + " foi finalizado e a garantia já está ativa. Obrigado pela preferência!");
    }

    /** Versão para SMS do {@link #lembreteRevisao}, com as mesmas regras de {@link #finalizacaoCurta}. */
    public static String lembreteRevisaoCurto(String nomeCliente,
                                              String modeloVeiculo,
                                              String placaVeiculo,
                                              LocalDateTime dataRevisao) {
        String data = dataRevisao.format(DATA_PT_BR);
        return primeiraQueCabe(saudacoesCurtas(nomeCliente), veiculosCurtos(modeloVeiculo, placaVeiculo),
                (saudacao, veiculo) -> "RRmax Oficina: " + saudacao + " A revisão preventiva do " + veiculo
                        + " está prevista para " + data + ". Entre em contato para agendar.");
    }

    private static String primeiraQueCabe(List<String> saudacoes,
                                           List<String> veiculos,
                                           BinaryOperator<String> montar) {
        String ultima = null;
        for (String saudacao : saudacoes) {
            for (String veiculo : veiculos) {
                ultima = montar.apply(saudacao, veiculo);
                if (ultima.length() <= LIMITE_SMS) {
                    return ultima;
                }
            }
        }
        return ultima.substring(0, LIMITE_SMS);
    }

    /** Do mais completo para o mais curto; "Olá!" é sempre a última opção. */
    private static List<String> saudacoesCurtas(String nomeCliente) {
        String nome = valorOuPadrao(nomeCliente, "");
        if (nome.isBlank()) {
            return List.of("Olá!");
        }
        return List.of("Olá, " + nome.split("\\s+")[0] + "!", "Olá!");
    }

    /** Do mais completo para o mais curto; "seu veículo" é sempre a última opção. */
    private static List<String> veiculosCurtos(String modeloVeiculo, String placaVeiculo) {
        String modelo = valorOuPadrao(modeloVeiculo, "");
        String placa = valorOuPadrao(placaVeiculo, "");
        List<String> opcoes = new ArrayList<>();

        if (!modelo.isBlank() && !placa.isBlank()) {
            opcoes.add(modelo + " (" + placa + ")");
        }
        if (!placa.isBlank()) {
            opcoes.add("veículo " + placa);
        }
        if (!modelo.isBlank() && placa.isBlank()) {
            opcoes.add(modelo);
        }
        opcoes.add("seu veículo");

        return opcoes;
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
