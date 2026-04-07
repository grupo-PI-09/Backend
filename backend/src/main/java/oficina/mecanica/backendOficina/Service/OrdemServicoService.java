package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.OrdemServicoDTORequest;
import oficina.mecanica.backendOficina.DTO.OrdemServicoDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.OrdemServicoRepository;
import oficina.mecanica.backendOficina.Repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
                               ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
    }

    public List<OrdemServicoDTOResponse> listar() {
        return ordemServicoRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public OrdemServicoDTOResponse buscarPorId(Long id) {
        OrdemServicoModel ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        return converterParaResponse(ordemServico);
    }

    public List<OrdemServicoDTOResponse> buscarPorClienteId(Long clienteId) {
        return ordemServicoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }


    public List<OrdemServicoDTOResponse> buscarPorVeiculoId(Long veiculoId) {
        return ordemServicoRepository.findByVeiculoId(veiculoId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public OrdemServicoDTOResponse criar(OrdemServicoDTORequest dto) {
        ClienteModel cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        VeiculoModel veiculo = veiculoRepository.findById(dto.getVeiculoId())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        OrdemServicoModel ordemServico = new OrdemServicoModel();
        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setUsuarioId(dto.getUsuarioId());
        ordemServico.setStatus(StatusOrdemServico.valueOf(dto.getStatus().toLowerCase()));
        ordemServico.setProblemaRelatado(dto.getProblemaRelatado());
        ordemServico.setDiagnostico(dto.getDiagnostico());
        ordemServico.setQuilometragem(dto.getQuilometragem());
        ordemServico.setValorEstimado(dto.getValorEstimado() != null ? dto.getValorEstimado() : BigDecimal.ZERO);
        ordemServico.setValorTotal(dto.getValorTotal() != null ? dto.getValorTotal() : BigDecimal.ZERO);
        ordemServico.setFormaPagamento(dto.getFormaPagamento());
        ordemServico.setObservacoes(dto.getObservacoes());
        ordemServico.setDataFechamento(dto.getDataFechamento());

        OrdemServicoModel salva = ordemServicoRepository.save(ordemServico);
        return converterParaResponse(salva);
    }

    public OrdemServicoDTOResponse atualizar(Long id, OrdemServicoDTORequest dto) {
        OrdemServicoModel ordemServico = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de serviço não encontrada"));

        ClienteModel cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        VeiculoModel veiculo = veiculoRepository.findById(dto.getVeiculoId())
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        ordemServico.setCliente(cliente);
        ordemServico.setVeiculo(veiculo);
        ordemServico.setUsuarioId(dto.getUsuarioId());
        ordemServico.setStatus(StatusOrdemServico.valueOf(dto.getStatus().toLowerCase()));
        ordemServico.setProblemaRelatado(dto.getProblemaRelatado());
        ordemServico.setDiagnostico(dto.getDiagnostico());
        ordemServico.setQuilometragem(dto.getQuilometragem());
        ordemServico.setValorEstimado(dto.getValorEstimado() != null ? dto.getValorEstimado() : BigDecimal.ZERO);
        ordemServico.setValorTotal(dto.getValorTotal() != null ? dto.getValorTotal() : BigDecimal.ZERO);
        ordemServico.setFormaPagamento(dto.getFormaPagamento());
        ordemServico.setObservacoes(dto.getObservacoes());
        ordemServico.setDataFechamento(dto.getDataFechamento());

        OrdemServicoModel atualizada = ordemServicoRepository.save(ordemServico);
        return converterParaResponse(atualizada);
    }

    public void deletar(Long id) {
        if (!ordemServicoRepository.existsById(id)) {
            throw new RuntimeException("Ordem de serviço não encontrada");
        }

        ordemServicoRepository.deleteById(id);
    }

    private OrdemServicoDTOResponse converterParaResponse(OrdemServicoModel ordemServico) {
        return new OrdemServicoDTOResponse(
                ordemServico.getId(),
                ordemServico.getCliente().getId(),
                ordemServico.getCliente().getNome(),
                ordemServico.getVeiculo().getId(),
                ordemServico.getVeiculo().getPlaca(),
                ordemServico.getUsuarioId(),
                ordemServico.getDataAbertura(),
                ordemServico.getDataFechamento(),
                ordemServico.getStatus().name(),
                ordemServico.getProblemaRelatado(),
                ordemServico.getDiagnostico(),
                ordemServico.getQuilometragem(),
                ordemServico.getValorEstimado(),
                ordemServico.getValorTotal(),
                ordemServico.getFormaPagamento(),
                ordemServico.getObservacoes()
        );
    }
}