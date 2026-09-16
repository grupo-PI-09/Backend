package oficina.mecanica.backendOficina.notificacao.infra;

import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.notificacao.usecase.OrdemParaNotificar;

/**
 * Converte a entidade JPA no dado de entrada dos casos de uso.
 */
public final class OrdemParaNotificarMapper {

    private OrdemParaNotificarMapper() {
    }

    public static OrdemParaNotificar de(OrdemServicoModel ordemServico) {
        ClienteModel cliente = ordemServico.getCliente();
        VeiculoModel veiculo = ordemServico.getVeiculo();

        return new OrdemParaNotificar(
                ordemServico.getId(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : null,
                cliente != null ? cliente.getTelefone() : null,
                veiculo != null ? veiculo.getId() : null,
                veiculo != null ? veiculo.getModelo() : null,
                veiculo != null ? veiculo.getPlaca() : null,
                ordemServico.getDataProximaRevisao()
        );
    }
}
