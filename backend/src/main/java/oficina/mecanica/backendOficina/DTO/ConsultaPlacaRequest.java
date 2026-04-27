package oficina.mecanica.backendOficina.DTO;

import jakarta.validation.constraints.NotBlank;

public class ConsultaPlacaRequest {

    @NotBlank(message = "Placa é obrigatória")
    private String placa;

    public ConsultaPlacaRequest() {
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}
