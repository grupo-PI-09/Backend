package oficina.mecanica.backendOficina.Service;

import oficina.mecanica.backendOficina.DTO.DashboardEvolucaoFaturamentoDTO;
import oficina.mecanica.backendOficina.DTO.DashboardFaturamentoDTO;
import oficina.mecanica.backendOficina.DTO.DashboardFaturamentoPeriodoDTO;
import oficina.mecanica.backendOficina.DTO.DashboardPontoFinanceiroDTO;
import oficina.mecanica.backendOficina.DTO.DashboardResumoDTO;
import oficina.mecanica.backendOficina.DTO.DashboardServicoPorTipoDTO;
import oficina.mecanica.backendOficina.DTO.DashboardServicosPorTipoDTO;
import oficina.mecanica.backendOficina.Model.StatusOrdemServico;
import oficina.mecanica.backendOficina.Model.TipoServico;
import oficina.mecanica.backendOficina.Repository.ClienteRepository;
import oficina.mecanica.backendOficina.Repository.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Painel financeiro. Faturamento = soma do valor_total das ordens finalizadas,
 * pela data de fechamento. As comparações "vs período anterior" usam o mesmo
 * período do ano anterior até o mesmo dia, para comparar bases equivalentes.
 */
@Service
public class DashboardService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final StatusOrdemServico STATUS_FATURADO = StatusOrdemServico.finalizada;

    private final ClienteRepository clienteRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public DashboardService(ClienteRepository clienteRepository,
                            OrdemServicoRepository ordemServicoRepository) {
        this.clienteRepository = clienteRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumoDTO obterResumo() {
        LocalDateTime agora = LocalDateTime.now();
        YearMonth mesAtual = YearMonth.from(agora);

        return new DashboardResumoDTO(
                montarFaturamento(agora),
                clienteRepository.count(),
                clienteRepository.countByDataCadastroBetween(inicioDoMes(mesAtual), fimDoMes(mesAtual)),
                montarComparativoMensal(mesAtual),
                montarEvolucaoFaturamento(mesAtual),
                montarServicosPorTipo(mesAtual)
        );
    }

    // ---------------------------------------------------------------------
    // Card "Faturamento" (mensal / semestral / anual)
    // ---------------------------------------------------------------------

    private DashboardFaturamentoDTO montarFaturamento(LocalDateTime agora) {
        YearMonth mesAtual = YearMonth.from(agora);
        int ano = agora.getYear();
        int semestre = agora.getMonthValue() <= 6 ? 1 : 2;
        YearMonth inicioSemestre = YearMonth.of(ano, semestre == 1 ? 1 : 7);

        return new DashboardFaturamentoDTO(
                montarPeriodo("mensal", inicioDoMes(mesAtual), agora,
                        rotuloMesAno(mesAtual), rotuloMesAno(mesAtual.minusYears(1))),
                montarPeriodo("semestral", inicioDoMes(inicioSemestre), agora,
                        rotuloSemestre(semestre, ano), rotuloSemestre(semestre, ano - 1)),
                montarPeriodo("anual", Year.of(ano).atDay(1).atStartOfDay(), agora,
                        String.valueOf(ano), String.valueOf(ano - 1))
        );
    }

    private DashboardFaturamentoPeriodoDTO montarPeriodo(String periodo, LocalDateTime inicio, LocalDateTime fim,
                                                         String rotulo, String rotuloAnterior) {
        BigDecimal atual = somarFaturamento(inicio, fim);
        BigDecimal anterior = somarFaturamento(inicio.minusYears(1), fim.minusYears(1));

        return new DashboardFaturamentoPeriodoDTO(periodo, rotulo, atual, rotuloAnterior, anterior,
                variacaoPercentual(atual, anterior));
    }

    private Double variacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.signum() == 0) {
            return null;
        }

        return atual.subtract(anterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(anterior, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // ---------------------------------------------------------------------
    // Gráfico "Faturamento mensal — comparativo anual"
    // ---------------------------------------------------------------------

    private List<DashboardPontoFinanceiroDTO> montarComparativoMensal(YearMonth mesAtual) {
        YearMonth mesmoMesAnoAnterior = mesAtual.minusYears(1);

        return List.of(
                new DashboardPontoFinanceiroDTO(rotuloMesEspacoAno(mesmoMesAnoAnterior),
                        somarFaturamento(inicioDoMes(mesmoMesAnoAnterior), fimDoMes(mesmoMesAnoAnterior))),
                new DashboardPontoFinanceiroDTO(rotuloMesEspacoAno(mesAtual),
                        somarFaturamento(inicioDoMes(mesAtual), fimDoMes(mesAtual)))
        );
    }

    // ---------------------------------------------------------------------
    // Gráfico "Evolução do faturamento" (jan → mês atual, ano atual x anterior)
    // ---------------------------------------------------------------------

    private DashboardEvolucaoFaturamentoDTO montarEvolucaoFaturamento(YearMonth mesAtual) {
        int anoAtual = mesAtual.getYear();
        int anoAnterior = anoAtual - 1;
        int ultimoMes = mesAtual.getMonthValue();

        Map<YearMonth, BigDecimal> faturamentoPorMes = faturamentoPorMes(
                YearMonth.of(anoAnterior, 1).atDay(1).atStartOfDay(),
                fimDoMes(mesAtual)
        );

        List<String> meses = new ArrayList<>();
        List<BigDecimal> mensalAtual = new ArrayList<>();
        List<BigDecimal> mensalAnterior = new ArrayList<>();
        List<BigDecimal> acumuladoAtual = new ArrayList<>();
        List<BigDecimal> acumuladoAnterior = new ArrayList<>();
        BigDecimal somaAtual = BigDecimal.ZERO;
        BigDecimal somaAnterior = BigDecimal.ZERO;

        for (int mes = 1; mes <= ultimoMes; mes++) {
            BigDecimal valorAtual = faturamentoPorMes.getOrDefault(YearMonth.of(anoAtual, mes), BigDecimal.ZERO);
            BigDecimal valorAnterior = faturamentoPorMes.getOrDefault(YearMonth.of(anoAnterior, mes), BigDecimal.ZERO);
            somaAtual = somaAtual.add(valorAtual);
            somaAnterior = somaAnterior.add(valorAnterior);

            meses.add(rotuloMesCurto(YearMonth.of(anoAtual, mes)));
            mensalAtual.add(valorAtual);
            mensalAnterior.add(valorAnterior);
            acumuladoAtual.add(somaAtual);
            acumuladoAnterior.add(somaAnterior);
        }

        return new DashboardEvolucaoFaturamentoDTO(anoAtual, anoAnterior, meses,
                mensalAtual, mensalAnterior, acumuladoAtual, acumuladoAnterior);
    }

    private Map<YearMonth, BigDecimal> faturamentoPorMes(LocalDateTime inicio, LocalDateTime fim) {
        Map<YearMonth, BigDecimal> resultado = new HashMap<>();
        for (Object[] linha : ordemServicoRepository.somarFaturamentoPorMes(STATUS_FATURADO, inicio, fim)) {
            int ano = ((Number) linha[0]).intValue();
            int mes = ((Number) linha[1]).intValue();
            resultado.put(YearMonth.of(ano, mes), valorOuZero((BigDecimal) linha[2]));
        }
        return resultado;
    }

    // ---------------------------------------------------------------------
    // Gráfico "Preventiva vs corretiva — esperado x realizado" (mês atual)
    // ---------------------------------------------------------------------

    private DashboardServicosPorTipoDTO montarServicosPorTipo(YearMonth mesAtual) {
        LocalDateTime inicio = inicioDoMes(mesAtual);
        LocalDateTime fim = fimDoMes(mesAtual);

        List<DashboardServicoPorTipoDTO> tipos = new ArrayList<>();
        for (TipoServico tipo : TipoServico.values()) {
            long esperado = ordemServicoRepository.countByTipoServicoAndStatusNotAndDataAberturaBetween(
                    tipo, StatusOrdemServico.cancelada, inicio, fim);
            long realizado = ordemServicoRepository.countByTipoServicoAndStatusAndDataFechamentoBetween(
                    tipo, StatusOrdemServico.finalizada, inicio, fim);

            tipos.add(new DashboardServicoPorTipoDTO(tipo.name(), capitalizar(tipo.name()), esperado, realizado));
        }

        return new DashboardServicosPorTipoDTO(rotuloMesAno(mesAtual), tipos);
    }

    // ---------------------------------------------------------------------
    // Utilitários
    // ---------------------------------------------------------------------

    private BigDecimal somarFaturamento(LocalDateTime inicio, LocalDateTime fim) {
        return valorOuZero(ordemServicoRepository.somarFaturamentoPorStatusEDataFechamento(STATUS_FATURADO, inicio, fim));
    }

    /** "set" */
    private String rotuloMesCurto(YearMonth mes) {
        return mes.getMonth().getDisplayName(TextStyle.SHORT, PT_BR).replace(".", "");
    }

    /** "set/2026" */
    private String rotuloMesAno(YearMonth mes) {
        return rotuloMesCurto(mes) + "/" + mes.getYear();
    }

    /** "Set 2026" */
    private String rotuloMesEspacoAno(YearMonth mes) {
        return capitalizar(rotuloMesCurto(mes)) + " " + mes.getYear();
    }

    /** "1º sem. 2026" */
    private String rotuloSemestre(int semestre, int ano) {
        return semestre + "º sem. " + ano;
    }

    private String capitalizar(String texto) {
        return texto.substring(0, 1).toUpperCase(PT_BR) + texto.substring(1);
    }

    private LocalDateTime inicioDoMes(YearMonth mes) {
        return mes.atDay(1).atStartOfDay();
    }

    private LocalDateTime fimDoMes(YearMonth mes) {
        return mes.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
