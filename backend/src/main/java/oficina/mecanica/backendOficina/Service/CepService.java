package oficina.mecanica.backendOficina.Service;

import com.fasterxml.jackson.databind.JsonNode;
import oficina.mecanica.backendOficina.DTO.ConsultaCepResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CepService {

    private final RestTemplate restTemplate;
    private final String viaCepBaseUrl;

    public CepService(RestTemplate restTemplate,
                      @Value("${viacep.base-url:https://viacep.com.br/ws}") String viaCepBaseUrl) {
        this.restTemplate = restTemplate;
        this.viaCepBaseUrl = viaCepBaseUrl;
    }

    public ConsultaCepResponse consultar(String cep) {
        String cepNormalizado = normalizarCep(cep);

        if (cepNormalizado.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP deve conter 8 números");
        }

        String baseUrlNormalizada = viaCepBaseUrl.endsWith("/")
                ? viaCepBaseUrl.substring(0, viaCepBaseUrl.length() - 1)
                : viaCepBaseUrl;
        String consultaUrl = baseUrlNormalizada + "/" + cepNormalizado + "/json/";

        try {
            JsonNode root = restTemplate.getForObject(consultaUrl, JsonNode.class);

            if (root == null || root.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Resposta vazia do ViaCEP");
            }

            if (root.path("erro").asBoolean(false)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP não encontrado");
            }

            return new ConsultaCepResponse(
                    cepNormalizado,
                    root.path("logradouro").asText(""),
                    root.path("complemento").asText(""),
                    root.path("bairro").asText(""),
                    root.path("localidade").asText(""),
                    root.path("uf").asText(""),
                    root
            );
        } catch (HttpStatusCodeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Não foi possível consultar o ViaCEP",
                    ex
            );
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "Tempo limite excedido ao consultar o ViaCEP",
                    ex
            );
        }
    }

    private String normalizarCep(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
    }
}
