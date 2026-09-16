package oficina.mecanica.backendOficina.notificacao.domain;

/**
 * Regra de domínio para destinatários: um telefone só é válido para envio
 * quando, após remover a formatação, restam dígitos.
 */
public final class Telefone {

    private Telefone() {
    }

    /** Retorna apenas os dígitos do telefone, ou {@code null} quando não há destinatário válido. */
    public static String normalizar(String telefone) {
        if (telefone == null) {
            return null;
        }

        String digitos = telefone.replaceAll("[^0-9]", "");
        return digitos.isBlank() ? null : digitos;
    }
}
