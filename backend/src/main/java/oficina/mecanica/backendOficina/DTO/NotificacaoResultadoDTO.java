package oficina.mecanica.backendOficina.DTO;

import java.time.LocalDateTime;

public class NotificacaoResultadoDTO {

    private boolean enviada;
    private boolean agendada;
    private boolean enviadaImediatamente;
    private LocalDateTime dataAgendamento;
    private String mensagem;
    private String aviso;

    public NotificacaoResultadoDTO() {
    }

    public NotificacaoResultadoDTO(boolean enviada, boolean agendada, boolean enviadaImediatamente,
                                   LocalDateTime dataAgendamento, String mensagem, String aviso) {
        this.enviada = enviada;
        this.agendada = agendada;
        this.enviadaImediatamente = enviadaImediatamente;
        this.dataAgendamento = dataAgendamento;
        this.mensagem = mensagem;
        this.aviso = aviso;
    }

    public static NotificacaoResultadoDTO enviada(String mensagem) {
        return new NotificacaoResultadoDTO(true, false, true, null, mensagem, null);
    }

    public static NotificacaoResultadoDTO agendada(LocalDateTime dataAgendamento, String mensagem) {
        return new NotificacaoResultadoDTO(false, true, false, dataAgendamento, mensagem, null);
    }

    public static NotificacaoResultadoDTO naoEnviada(String aviso) {
        return new NotificacaoResultadoDTO(false, false, false, null, null, aviso);
    }

    public boolean isEnviada() {
        return enviada;
    }

    public boolean isAgendada() {
        return agendada;
    }

    public boolean isEnviadaImediatamente() {
        return enviadaImediatamente;
    }

    public LocalDateTime getDataAgendamento() {
        return dataAgendamento;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getAviso() {
        return aviso;
    }
}
