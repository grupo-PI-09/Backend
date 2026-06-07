package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.DashboardOrdemDTO;
import oficina.mecanica.backendOficina.DTO.DashboardPontoFinanceiroDTO;
import oficina.mecanica.backendOficina.DTO.DashboardPontoNumericoDTO;
import oficina.mecanica.backendOficina.DTO.DashboardResumoDTO;
import oficina.mecanica.backendOficina.DTO.DashboardRevisaoMensalDTO;
import oficina.mecanica.backendOficina.Model.OrdemServicoModel;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.VeiculoModel;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.OrdemServicoRepository;
import oficina.mecanica.backendOficina.Repository.VeiculoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DashboardService {

    private static final int MESES_SERIE = 6;
    private static final DateTimeFormatter ROTULO_MES =
            DateTimeFormatter.ofPattern("MMM/yy", Locale.forLanguageTag("pt-BR"));

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public DashboardService(ClienteRepository clienteRepository,
                            VeiculoRepository veiculoRepository,
                            OrdemServicoRepository ordemServicoRepository) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    public DashboardResumoDTO obterResumo() {
        YearMonth mesAtual = YearMonth.now();
        LocalDateTime inicioMes = inicioDoMes(mesAtual);
        LocalDateTime fimMes = fimDoMes(mesAtual);
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime fimRevisoes = agora.plusDays(30);

        long totalClientes = clienteRepository.count();
        long totalVeiculos = veiculoRepository.count();
        long totalOrdens = ordemServicoRepository.count();
        long abertasEmAndamento = ordemServicoRepository.countByStatusIn(List.of(
                StatusOrdemServico.aberta,
                StatusOrdemServico.em_andamento,
                StatusOrdemServico.aguardando_aprovacao,
                StatusOrdemServico.aguardando_peca
        ));
        long finalizadas = ordemServicoRepository.countByStatus(StatusOrdemServico.finalizada);
        long finalizadasMes = ordemServicoRepository.countByStatusAndDataFechamentoBetween(
                StatusOrdemServico.finalizada,
                inicioMes,
                fimMes
        );
        long novosClientesMes = clienteRepository.countByDataCadastroBetween(inicioMes, fimMes);
        long proximasRevisoes = ordemServicoRepository.countByDataProximaRevisaoBetween(agora, fimRevisoes);

        return new DashboardResumoDTO(
                totalClientes,
                totalVeiculos,
                totalOrdens,
                abertasEmAndamento,
                finalizadas,
                finalizadasMes,
                novosClientesMes,
                proximasRevisoes,
                0,
                valorOuZero(ordemServicoRepository.somarFaturamentoTotal()),
                valorOuZero(ordemServicoRepository.somarFaturamentoPorStatusEDataFechamento(
                        StatusOrdemServico.finalizada,
                        inicioMes,
                        fimMes
                )),
                ordemServicoRepository.findTop5ByOrderByDataAberturaDesc()
                        .stream()
                        .map(this::converterOrdem)
                        .toList(),
                ordemServicoRepository.findTop5ByDataProximaRevisaoBetweenOrderByDataProximaRevisaoAsc(agora, fimRevisoes)
                        .stream()
                        .map(this::converterOrdem)
                        .toList(),
                montarSerieFinalizacoes(mesAtual),
                montarSerieFaturamento(mesAtual),
                montarSerieRevisoes(mesAtual)
        );
    }

    private List<DashboardPontoNumericoDTO> montarSerieFinalizacoes(YearMonth mesAtual) {
        List<DashboardPontoNumericoDTO> pontos = new ArrayList<>();
        for (YearMonth mes : ultimosMeses(mesAtual)) {
            pontos.add(new DashboardPontoNumericoDTO(
                    rotulo(mes),
                    ordemServicoRepository.countByStatusAndDataFechamentoBetween(
                            StatusOrdemServico.finalizada,
                            inicioDoMes(mes),
                            fimDoMes(mes)
                    )
            ));
        }
        return pontos;
    }

    private List<DashboardPontoFinanceiroDTO> montarSerieFaturamento(YearMonth mesAtual) {
        List<DashboardPontoFinanceiroDTO> pontos = new ArrayList<>();
        for (YearMonth mes : ultimosMeses(mesAtual)) {
            pontos.add(new DashboardPontoFinanceiroDTO(
                    rotulo(mes),
                    valorOuZero(ordemServicoRepository.somarFaturamentoPorStatusEDataFechamento(
                            StatusOrdemServico.finalizada,
                            inicioDoMes(mes),
                            fimDoMes(mes)
                    ))
            ));
        }
        return pontos;
    }

    private List<DashboardRevisaoMensalDTO> montarSerieRevisoes(YearMonth mesAtual) {
        List<DashboardRevisaoMensalDTO> pontos = new ArrayList<>();
        for (YearMonth mes : ultimosMeses(mesAtual)) {
            long estimadas = ordemServicoRepository.countByDataProximaRevisaoBetween(inicioDoMes(mes), fimDoMes(mes));
            long realizadas = ordemServicoRepository.countByStatusAndDataFechamentoBetween(
                    StatusOrdemServico.finalizada,
                    inicioDoMes(mes),
                    fimDoMes(mes)
            );

            pontos.add(new DashboardRevisaoMensalDTO(rotulo(mes), realizadas, estimadas));
        }
        return pontos;
    }

    private List<YearMonth> ultimosMeses(YearMonth mesAtual) {
        List<YearMonth> meses = new ArrayList<>();
        for (int i = MESES_SERIE - 1; i >= 0; i--) {
            meses.add(mesAtual.minusMonths(i));
        }
        return meses;
    }

    private DashboardOrdemDTO converterOrdem(OrdemServicoModel ordem) {
        VeiculoModel veiculo = ordem.getVeiculo();
        String modelo = veiculo != null ? veiculo.getModelo() : null;
        String placa = veiculo != null ? veiculo.getPlaca() : null;

        return new DashboardOrdemDTO(
                ordem.getId(),
                ordem.getCliente() != null ? ordem.getCliente().getNome() : "",
                montarDescricaoVeiculo(modelo, placa),
                placa,
                modelo,
                ordem.getStatus() != null ? ordem.getStatus().name() : "",
                ordem.getDataAbertura(),
                ordem.getDataProximaRevisao(),
                valorOuZero(ordem.getValorTotal())
        );
    }

    private String montarDescricaoVeiculo(String modelo, String placa) {
        if (modelo != null && !modelo.isBlank() && placa != null && !placa.isBlank()) {
            return modelo + " (" + placa + ")";
        }

        if (placa != null && !placa.isBlank()) {
            return placa;
        }

        if (modelo != null && !modelo.isBlank()) {
            return modelo;
        }

        return "Sem dados";
    }

    private String rotulo(YearMonth mes) {
        return mes.atDay(1).format(ROTULO_MES).replace(".", "");
    }

    private LocalDateTime inicioDoMes(YearMonth mes) {
        return mes.atDay(1).atStartOfDay();
    }

    private LocalDateTime fimDoMes(YearMonth mes) {
        LocalDate primeiroDiaProximoMes = mes.plusMonths(1).atDay(1);
        return primeiroDiaProximoMes.atStartOfDay().minusNanos(1);
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
