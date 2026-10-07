package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.NotificacaoDTOResponse;
import oficina.mecanica.backendOficina.DTO.PaginaCursorResponse;
import oficina.mecanica.backendOficina.Model.ClienteModel;
import oficina.mecanica.backendOficina.Model.NotificacaoModel;
import oficina.mecanica.backendOficina.Paginacao.CursorCodec;
import oficina.mecanica.backendOficina.Paginacao.CursorCodec.Cursor;
import oficina.mecanica.backendOficina.Repository.NotificacaoRepository;
import oficina.mecanica.backendOficina.notificacao.usecase.port.EnviadorDeMensagem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacaoServicePaginacaoTest {

    private NotificacaoRepository repository;
    private NotificacaoService service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificacaoRepository.class);
        service = new NotificacaoService(repository, mock(EnviadorDeMensagem.class));
    }

    @Test
    void pedeUmaLinhaAMaisParaSaberSeHaProximaPagina() {
        when(repository.listarPorCursor(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of());

        service.listarPorCursor(null, null, null, null, null, null, 8);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).listarPorCursor(any(), any(), any(), any(), any(), any(), any(), pageable.capture());
        assertEquals(9, pageable.getValue().getPageSize());
        assertEquals(0, pageable.getValue().getOffset(), "cursor nunca usa OFFSET");
    }

    @Test
    void comMaisLinhasQueOTamanhoDevolveCursorDaUltimaLinhaEntregue() {
        // 3 linhas para tamanho 2: a 3ª só sinaliza que existe próxima página
        when(repository.listarPorCursor(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(
                        notificacao(30L, LocalDateTime.of(2026, 5, 3, 10, 0)),
                        notificacao(20L, LocalDateTime.of(2026, 5, 2, 10, 0)),
                        notificacao(10L, LocalDateTime.of(2026, 5, 1, 10, 0))));

        PaginaCursorResponse<NotificacaoDTOResponse> pagina =
                service.listarPorCursor(null, null, null, null, null, null, 2);

        assertEquals(List.of(30L, 20L), pagina.getContent().stream().map(NotificacaoDTOResponse::getId).toList());
        assertTrue(pagina.isHasNext());

        Cursor cursor = CursorCodec.decodificar(pagina.getNextPage());
        assertEquals(20L, cursor.id());
        assertEquals(LocalDateTime.of(2026, 5, 2, 10, 0), cursor.data());
    }

    @Test
    void naUltimaPaginaNaoHaCursor() {
        when(repository.listarPorCursor(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(notificacao(1L, LocalDateTime.of(2026, 5, 1, 10, 0))));

        PaginaCursorResponse<NotificacaoDTOResponse> pagina =
                service.listarPorCursor(null, null, null, null, null, null, 8);

        assertEquals(1, pagina.getContent().size());
        assertNull(pagina.getNextPage());
        assertFalse(pagina.isHasNext());
    }

    @Test
    void repassaOCursorRecebidoParaARepositoryEAjustaOPeriodo() {
        when(repository.listarPorCursor(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new ArrayList<>());
        LocalDateTime dataCursor = LocalDateTime.of(2026, 5, 2, 10, 0);

        service.listarPorCursor(true, null, null,
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                CursorCodec.codificar(dataCursor, 20L), 8);

        verify(repository).listarPorCursor(
                eq(true), eq(null), eq(null),
                eq(LocalDateTime.of(2026, 5, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 6, 1, 0, 0)),   // data final inclusiva
                eq(dataCursor), eq(20L), any());
    }

    @Test
    void rejeitaCursorInvalidoSemConsultarOBanco() {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.listarPorCursor(null, null, null, null, null, "lixo!", 8));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verify(repository, never()).listarPorCursor(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void rejeitaTamanhoInvalidoEPeriodoInvertido() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.listarPorCursor(null, null, null, null, null, null, 0)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.listarPorCursor(null, null, null, null, null, null, NotificacaoService.TAMANHO_MAXIMO + 1)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.listarPorCursor(null, null, null,
                        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 5, 1), null, 8)).getStatusCode());
    }

    @Test
    void usaDataDeAgendamentoOuCriacaoComoReferenciaQuandoNaoHaEnvio() {
        NotificacaoModel agendada = notificacao(2L, null);
        agendada.setDataAgendamento(LocalDateTime.of(2026, 7, 1, 8, 0));
        NotificacaoModel criada = notificacao(1L, null);
        ReflectionTestUtils.setField(criada, "dataCriacao", LocalDateTime.of(2026, 6, 1, 9, 0));
        when(repository.listarPorCursor(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(agendada, criada));

        // tamanho 1 → a 2ª linha é a "extra"; o cursor sai da 1ª (agendada)
        PaginaCursorResponse<NotificacaoDTOResponse> pagina =
                service.listarPorCursor(null, null, null, null, null, null, 1);

        assertEquals(LocalDateTime.of(2026, 7, 1, 8, 0), CursorCodec.decodificar(pagina.getNextPage()).data());
    }

    private static NotificacaoModel notificacao(Long id, LocalDateTime dataEnvio) {
        NotificacaoModel notificacao = new NotificacaoModel();
        ReflectionTestUtils.setField(notificacao, "id", id);
        ClienteModel cliente = new ClienteModel();
        cliente.setId(1L);
        cliente.setNome("Cliente " + id);
        notificacao.setCliente(cliente);
        notificacao.setDataEnvio(dataEnvio);
        return notificacao;
    }
}
