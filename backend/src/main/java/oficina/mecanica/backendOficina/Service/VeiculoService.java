package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.VeiculoDTORequest;
import oficina.mecanica.backendOficina.DTO.VeiculoDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.TipoCombustivel;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, ClienteRepository clienteRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<VeiculoDTOResponse> listar() {
        return veiculoRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public VeiculoDTOResponse buscarPorId(Long id) {
        VeiculoModel veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        return converterParaResponse(veiculo);
    }

    public List<VeiculoDTOResponse> buscarPorClienteId(Long clienteId) {
        return veiculoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    public VeiculoDTOResponse criar(VeiculoDTORequest dto) {
        ClienteModel cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        VeiculoModel veiculo = new VeiculoModel();
        veiculo.setPlaca(dto.getPlaca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setMarca(dto.getMarca());
        veiculo.setAno(dto.getAno());
        veiculo.setQuilometragem(dto.getQuilometragem());
        veiculo.setTipoCombustivel(TipoCombustivel.valueOf(dto.getTipoCombustivel().toLowerCase()));
        veiculo.setAtivo(true);
        veiculo.setCliente(cliente);

        VeiculoModel veiculoSalvo = veiculoRepository.save(veiculo);
        return converterParaResponse(veiculoSalvo);
    }

    public VeiculoDTOResponse atualizar(Long id, VeiculoDTORequest dto) {
        VeiculoModel veiculo = veiculoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veículo não encontrado"));

        ClienteModel cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        veiculo.setPlaca(dto.getPlaca());
        veiculo.setModelo(dto.getModelo());
        veiculo.setMarca(dto.getMarca());
        veiculo.setAno(dto.getAno());
        veiculo.setQuilometragem(dto.getQuilometragem());
        veiculo.setTipoCombustivel(TipoCombustivel.valueOf(dto.getTipoCombustivel().toLowerCase()));
        veiculo.setCliente(cliente);

        VeiculoModel veiculoAtualizado = veiculoRepository.save(veiculo);
        return converterParaResponse(veiculoAtualizado);
    }

    public void deletar(Long id) {
        if (!veiculoRepository.existsById(id)) {
            throw new RuntimeException("Veículo não encontrado");
        }

        veiculoRepository.deleteById(id);
    }

    private VeiculoDTOResponse converterParaResponse(VeiculoModel veiculo) {
        return new VeiculoDTOResponse(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getModelo(),
                veiculo.getMarca(),
                veiculo.getAno(),
                veiculo.getQuilometragem(),
                veiculo.getTipoCombustivel().name(),
                veiculo.getAtivo(),
                veiculo.getDataCriacao(),
                veiculo.getCliente().getId(),
                veiculo.getCliente().getNome()
        );
    }
}