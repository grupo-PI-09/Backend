package oficina.mecanica.backendOficina.notificacao.usecase;

import java.time.LocalDateTime;

/**
 * Dados de entrada dos casos de uso de notificação. Contém apenas o que o
 * fluxo precisa da ordem de serviço, desacoplado da entidade JPA.
 */
public record OrdemParaNotificar(
        Long id,
        Long clienteId,
        String nomeCliente,
        String telefoneCliente,
        Long veiculoId,
        String modeloVeiculo,
        String placaVeiculo,
        LocalDateTime dataProximaRevisao
) {
}
