package oficina.mecanica.backendOficina.Service;

import com.fasterxml.jackson.databind.JsonNode;
import oficina.mecanica.backendOficina.DTO.ConsultaPlacaRequest;
import oficina.mecanica.backendOficina.DTO.ConsultaPlacaResponse;
import oficina.mecanica.backendOficina.DTO.VeiculoDTORequest;
import oficina.mecanica.backendOficina.DTO.VeiculoDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.TipoCombustivel;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.VeiculoRepository;
import oficina.mecanica.backendOficina.exceptions.ApiBrasilException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;
    private final RestTemplate restTemplate;
    private final String apiBrasilBaseUrl;
    private final String apiBrasilToken;
    private final String apiBrasilPlacaEndpoint;
    private final String apiBrasilPlacaTipo;

    public VeiculoService(VeiculoRepository veiculoRepository,
                          ClienteRepository clienteRepository,
                          RestTemplate restTemplate,
                          @Value("${apibrasil.base-url}") String apiBrasilBaseUrl,
                          @Value("${apibrasil.token:}") String apiBrasilToken,
                          @Value("${apibrasil.placa-endpoint:/vehicles/base/001/consulta}") String apiBrasilPlacaEndpoint,
                          @Value("${apibrasil.placa-tipo:fipe}") String apiBrasilPlacaTipo) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
        this.restTemplate = restTemplate;
        this.apiBrasilBaseUrl = apiBrasilBaseUrl;
        this.apiBrasilToken = apiBrasilToken;
        this.apiBrasilPlacaEndpoint = apiBrasilPlacaEndpoint;
        this.apiBrasilPlacaTipo = apiBrasilPlacaTipo;
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

    public ConsultaPlacaResponse consultarPorPlaca(ConsultaPlacaRequest dto) {
        return consultarPorPlaca(dto.getPlaca());
    }

    public ConsultaPlacaResponse consultarPorPlaca(String placa) {
        if (apiBrasilToken == null || apiBrasilToken.isBlank()) {
            throw new ApiBrasilException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível consultar a placa",
                    "Token da APIBrasil não configurado"
            );
        }

        String placaNormalizada = normalizarPlaca(placa);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiBrasilToken);

        Map<String, Object> body = Map.of(
                "tipo", apiBrasilPlacaTipo,
                "placa", placaNormalizada
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String endpointNormalizado = apiBrasilPlacaEndpoint.startsWith("/")
                ? apiBrasilPlacaEndpoint
                : "/" + apiBrasilPlacaEndpoint;
        String consultaUrl = apiBrasilBaseUrl.endsWith("/")
                ? apiBrasilBaseUrl.substring(0, apiBrasilBaseUrl.length() - 1) + endpointNormalizado
                : apiBrasilBaseUrl + endpointNormalizado;

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    consultaUrl,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            if (root == null || root.isEmpty()) {
                throw new ApiBrasilException(
                        HttpStatus.BAD_GATEWAY,
                        "Não foi possível consultar a placa",
                        "Resposta vazia da APIBrasil"
                );
            }

            if (root.path("error").asBoolean(false)) {
                throw new ApiBrasilException(
                        HttpStatus.BAD_GATEWAY,
                        "Não foi possível consultar a placa",
                        root.path("message").asText("A APIBrasil retornou uma resposta de erro")
                );
            }

            return new ConsultaPlacaResponse(
                    placaNormalizada,
                    extrairMarca(root),
                    extrairModelo(root),
                    extrairTexto(root, "data.data[0].anoModelo", "data.data[0].anoFabricacao", "data.veiculo.ano", "ano"),
                    root
            );
        } catch (HttpStatusCodeException ex) {
            HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
            throw new ApiBrasilException(
                    status != null ? status : HttpStatus.BAD_GATEWAY,
                    "Não foi possível consultar a placa",
                    ex.getResponseBodyAsString()
            );
        } catch (ResourceAccessException ex) {
            throw new ApiBrasilException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Não foi possível consultar a placa",
                    "Tempo limite excedido ao consultar a APIBrasil"
            );
        }
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

    private String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String extrairMarca(JsonNode root) {
        String marca = extrairTexto(root, "data.data[0].marca", "data.veiculo.marca", "marca");
        if (marca != null && !marca.isBlank()) {
            return normalizarMarca(marca);
        }

        String marcaModelo = extrairTexto(root, "data.veiculo.marca_modelo", "data.fipes[0].marca_modelo");
        if (marcaModelo == null || marcaModelo.isBlank()) {
            return null;
        }

        String[] partes = marcaModelo.split("/", 2);
        return normalizarMarca(partes[0].trim());
    }

    private String extrairModelo(JsonNode root) {
        String modelo = extrairTexto(root, "data.data[0].modelo", "data.veiculo.modelo", "modelo");
        if (modelo != null && !modelo.isBlank()) {
            return modelo;
        }

        String marcaModelo = extrairTexto(root, "data.veiculo.marca_modelo", "data.fipes[0].marca_modelo");
        if (marcaModelo == null || marcaModelo.isBlank()) {
            return null;
        }

        String[] partes = marcaModelo.split("/", 2);
        return partes.length > 1 ? partes[1].trim() : marcaModelo.trim();
    }

    private String extrairTexto(JsonNode root, String... caminhos) {
        for (String caminho : caminhos) {
            JsonNode atual = root;
            boolean invalido = false;

            for (String segmento : caminho.split("\\.")) {
                if (segmento.contains("[")) {
                    String campo = segmento.substring(0, segmento.indexOf('['));
                    int indice = Integer.parseInt(segmento.substring(segmento.indexOf('[') + 1, segmento.indexOf(']')));
                    atual = atual.path(campo);
                    if (!atual.isArray() || atual.size() <= indice) {
                        invalido = true;
                        break;
                    }
                    atual = atual.get(indice);
                    continue;
                }

                atual = atual.path(segmento);
                if (atual.isMissingNode() || atual.isNull()) {
                    invalido = true;
                    break;
                }
            }

            if (!invalido) {
                String valor = atual.asText();
                if (valor != null && !valor.isBlank()) {
                    return valor;
                }
            }
        }

        return null;
    }

    private String normalizarMarca(String marca) {
        if (marca == null) {
            return null;
        }

        String[] partes = marca.split("\\s+-\\s+", 2);
        if (partes.length > 1 && !partes[1].isBlank()) {
            return partes[1].trim();
        }

        return marca.trim();
    }

}
