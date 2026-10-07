package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.ClienteComVeiculosDTOResponse;
import oficina.mecanica.backendOficina.DTO.PaginaOffsetResponse;
import oficina.mecanica.backendOficina.DTO.VeiculoDTOResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteServicePaginacaoTest {

    private ClienteRepository repository;
    private Map<Long, List<VeiculoDTOResponse>> veiculosPorCliente = Map.of();
    private List<Long> idsConsultados;
    private ClienteService service;

    @BeforeEach
    void setUp() {
        repository = mock(ClienteRepository.class);
        // VeiculoService é classe concreta (Mockito inline não a instrumenta no JDK 25): subclasse de teste
        VeiculoService veiculoService = new VeiculoService(null, null, null, "", "", "", "") {
            @Override
            public Map<Long, List<VeiculoDTOResponse>> buscarPorClienteIds(Collection<Long> clienteIds) {
                idsConsultados = List.copyOf(clienteIds);
                return veiculosPorCliente;
            }
        };
        service = new ClienteService(repository, veiculoService);
    }

    @Test
    void paginaTresComTamanhoDezVaiParaOOffsetVinteOrdenadoPorId() {
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service.listarPaginado(3, 10, null);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(pageable.capture());
        assertEquals(20, pageable.getValue().getOffset());
        assertEquals(10, pageable.getValue().getPageSize());
        assertEquals("id", pageable.getValue().getSort().iterator().next().getProperty());
        verify(repository, never()).findByNomeContainingIgnoreCase(any(), any(Pageable.class));
    }

    @Test
    void comBuscaFiltraPorNomeEAnexaVeiculosDeCadaCliente() {
        ClienteModel ana = cliente(1L, "Ana");
        ClienteModel bia = cliente(2L, "Bia");
        Pageable pedido = org.springframework.data.domain.PageRequest.of(0, 2);
        when(repository.findByNomeContainingIgnoreCase(eq("an"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ana, bia), pedido, 5));
        VeiculoDTOResponse civic = new VeiculoDTOResponse();
        civic.setId(10L);
        veiculosPorCliente = Map.of(1L, List.of(civic));

        PaginaOffsetResponse<ClienteComVeiculosDTOResponse> pagina = service.listarPaginado(1, 2, "  an ");

        assertEquals(List.of(1L, 2L), idsConsultados, "veículos buscados numa única consulta");
        assertEquals(1, pagina.getPage());
        assertEquals(2, pagina.getSize());
        assertEquals(5, pagina.getTotalElements());
        assertEquals(3, pagina.getTotalPages());
        assertTrue(pagina.isFirst());
        assertFalse(pagina.isLast());
        assertEquals(1, pagina.getContent().get(0).getVeiculos().size());
        assertEquals(0, pagina.getContent().get(1).getVeiculos().size(), "cliente sem veículo recebe lista vazia");
    }

    @Test
    void rejeitaPaginaETamanhoInvalidos() {
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.listarPaginado(0, 8, null)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.listarPaginado(1, 1000, null)).getStatusCode());
    }

    private static ClienteModel cliente(Long id, String nome) {
        ClienteModel cliente = new ClienteModel();
        cliente.setId(id);
        cliente.setNome(nome);
        return cliente;
    }
}
