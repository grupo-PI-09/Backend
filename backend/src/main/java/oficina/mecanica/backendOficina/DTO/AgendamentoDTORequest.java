package oficina.mecanica.backendOficina.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AgendamentoDTORequest {

    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    @NotNull(message = "Veículo é obrigatório")
    private Long veiculoId;

    private Long usuarioId;

    @NotNull(message = "Data e horário são obrigatórios")
    private LocalDateTime dataHora;

    @NotNull(message = "Duração é obrigatória")
    @Min(value = 1, message = "Duração mínima é 1 hora")
    @Max(value = 8, message = "Duração máxima é 8 horas")
    private Integer duracaoHoras;

    // preventiva | corretiva (opcional: padrão corretiva)
    private String tipoServico;

    @NotBlank(message = "Serviço é obrigatório")
    private String servico;

    private String observacoes;

    // agendado | cancelado | concluido (opcional: padrão agendado)
    private String status;

    public AgendamentoDTORequest() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(Long veiculoId) {
        this.veiculoId = veiculoId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Integer getDuracaoHoras() {
        return duracaoHoras;
    }

    public void setDuracaoHoras(Integer duracaoHoras) {
        this.duracaoHoras = duracaoHoras;
    }

    public String getTipoServico() {
        return tipoServico;
    }

    public void setTipoServico(String tipoServico) {
        this.tipoServico = tipoServico;
    }

    public String getServico() {
        return servico;
    }

    public void setServico(String servico) {
        this.servico = servico;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
