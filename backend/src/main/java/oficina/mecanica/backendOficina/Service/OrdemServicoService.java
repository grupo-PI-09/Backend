package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.OrdemServicoDTORequest;
import oficina.mecanica.backendOficina.DTO.OrdemServicoDTOResponse;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrdemServicoService {

    private final List<OrdemServicoModel> ordensServico = new ArrayList<>();
    private Integer proximoId = 1;

    public OrdemServicoDTOResponse criarOrdemServico(OrdemServicoDTORequest request) {
        OrdemServicoModel novaOrdem = new OrdemServicoModel(
                proximoId++,
                request.getCliente(),
                request.getCodigoVeiculo(),
                request.getObservacoesAvarias(),
                request.getDescricaoReparo(),
                request.getListaPecas()
        );

        ordensServico.add(novaOrdem);
        return converterParaResponse(novaOrdem);
    }

    public List<OrdemServicoDTOResponse> listarOrdensServico() {
        List<OrdemServicoDTOResponse> resposta = new ArrayList<>();

        for (OrdemServicoModel ordemServico : ordensServico) {
            resposta.add(converterParaResponse(ordemServico));
        }

        return resposta;
    }

    public OrdemServicoDTOResponse buscarOrdemServicoPorId(Integer id) {
        OrdemServicoModel ordemServico = buscarModelPorId(id);
        return converterParaResponse(ordemServico);
    }

    public OrdemServicoDTOResponse atualizarOrdemServico(Integer id, OrdemServicoDTORequest request) {
        OrdemServicoModel ordemServico = buscarModelPorId(id);

        ordemServico.setCliente(request.getCliente());
        ordemServico.setCodigoVeiculo(request.getCodigoVeiculo());
        ordemServico.setObservacoesAvarias(request.getObservacoesAvarias());
        ordemServico.setDescricaoReparo(request.getDescricaoReparo());
        ordemServico.setListaPecas(request.getListaPecas());

        return converterParaResponse(ordemServico);
    }

    public void deletarOrdemServico(Integer id) {
        OrdemServicoModel ordemServico = buscarModelPorId(id);
        ordensServico.remove(ordemServico);
    }

    private OrdemServicoModel buscarModelPorId(Integer id) {
        for (OrdemServicoModel ordemServico : ordensServico) {
            if (ordemServico.getId().equals(id)) {
                return ordemServico;
            }
        }

        throw new IllegalArgumentException("Ordem de serviço não encontrada para o id: " + id);
    }

    private OrdemServicoDTOResponse converterParaResponse(OrdemServicoModel ordemServico) {
        return new OrdemServicoDTOResponse(
                ordemServico.getId(),
                ordemServico.getCliente(),
                ordemServico.getCodigoVeiculo(),
                ordemServico.getObservacoesAvarias(),
                ordemServico.getDescricaoReparo(),
                ordemServico.getListaPecas()
        );
    }
}
