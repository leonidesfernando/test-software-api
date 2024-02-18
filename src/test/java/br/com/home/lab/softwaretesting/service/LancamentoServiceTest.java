package br.com.home.lab.softwaretesting.service;

import br.com.home.lab.softwaretesting.controller.record.BuscaForm;
import br.com.home.lab.softwaretesting.controller.record.ResultadoRecord;
import br.com.home.lab.softwaretesting.controller.record.TotalLancamentoCategoriaRecord;
import br.com.home.lab.softwaretesting.controller.record.TotalLancamentoRecord;
import br.com.home.lab.softwaretesting.converter.StringToMoneyConverter;
import br.com.home.lab.softwaretesting.model.Categoria;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import br.com.home.lab.softwaretesting.repository.LancamentoRepository;
import br.com.home.lab.softwaretesting.util.DataGen;
import br.com.home.lab.softwaretesting.util.LancamentoGen;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static br.com.home.lab.softwaretesting.model.Lancamento.CURRENT_MONTH_CLAUSE;
import static br.com.home.lab.softwaretesting.service.LancamentoService.MAXIMO_LANCAMENTOS;
import static br.com.home.lab.softwaretesting.util.LancamentoGen.novaDespesa;
import static br.com.home.lab.softwaretesting.util.LancamentoGen.novaRenda;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@SpringBootTest
public class LancamentoServiceTest extends AbstractTransactionalTestNGSpringContextTests {

    @Spy
    @InjectMocks
    private LancamentoService lancamentoService;

    @Mock
    private LancamentoRepository lancamentoRepository;

    @Mock
    private EntityManager entityManager;
    @Mock
    TypedQuery query;

    @BeforeClass
    public void init(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void salvarTest(){
        given(lancamentoRepository.save(any(Lancamento.class)))
                .willReturn(new Lancamento());

        lancamentoService.salvar(new Lancamento());

        verify(lancamentoRepository, times(1))
                .save(any(Lancamento.class));
    }

    @Test
    public void removerTest(){
        long id = 10L;
        var lancamento = LancamentoGen.builder()
                .comTipo(TipoLancamento.INCOME)
                .comDescricao(DataGen.productName())
                .comDataLancamento(DataGen.date())
                .comValor(DataGen.moneyValue())
                .comCategoria(Categoria.WAGE)
                .build();

        when(lancamentoRepository.getById(id)).thenReturn(lancamento);
        lancamentoService.remover(id);

        verify(lancamentoRepository, times(1))
                .getById(anyLong());
        verify(lancamentoRepository, times(1))
                .delete(any(Lancamento.class));
    }

    @Test(dataProvider = "lancamentos")
    public void getTotalSaidaTest(List<Lancamento> lancamentos, BigDecimal totalSaidaEsperado){
        BigDecimal totalObtido = lancamentoService.getTotalDespesa(lancamentos);
        assertThat(totalObtido).isEqualTo(totalSaidaEsperado);
    }

    @Test(dataProvider = "lancamentos")
    public void getTotalEntradaTest(List<Lancamento> lancamentos, BigDecimal totalEntradaEsperado){
        lancamentos.add(LancamentoGen.builder()
                .comTipo(TipoLancamento.INCOME)
                .comDescricao(DataGen.productName())
                .comDataLancamento(DataGen.date())
                .comCategoria(Categoria.WAGE)
                .build());

        when(lancamentoService.somaValoresPorTipo(lancamentos, TipoLancamento.INCOME)).thenCallRealMethod();
        when(lancamentoService.getTotalRenda(lancamentos)).thenCallRealMethod();
        assertThat(totalEntradaEsperado).isEqualTo(lancamentoService.getTotalRenda(lancamentos));
    }

    @Test
    public void getPaginasTest(){
        when(lancamentoService.calculaNumeroPaginas(anyInt())).thenCallRealMethod();
        when(lancamentoService.getPaginas(anyInt())).thenCallRealMethod();
        int totalRegistros = DataGen.number(5, 50);
        int numeroPaginas = lancamentoService.calculaNumeroPaginas(totalRegistros);
        var list = lancamentoService.getPaginas(totalRegistros);

        assertThat(list)
                .isEqualTo(IntStream.rangeClosed(1, numeroPaginas)
                        .boxed().collect(Collectors.toList()));
    }

    @Test(dataProvider = "entriesAndPages")
    public void calculaNumeroPaginasTest(int totalRegistos, int totalPaginas){
        assertThat(lancamentoService.calculaNumeroPaginas(totalRegistos)).isEqualTo(totalPaginas);
    }

    @Test
    public void calculaNumeroPaginasNegativeCaseTest(){
        assertThat(lancamentoService.calculaNumeroPaginas(500)).isNotEqualTo(5);
    }


    @Test
    public void calculaPrimeiroRegistroPorPaginaTest(){
        assertThat(lancamentoService.calculaPrimeiroRegistroPorPagina(0)).isZero();
        int pages = 10;
        IntStream.rangeClosed(1, pages).forEach(i ->
                assertThat(lancamentoService.calculaPrimeiroRegistroPorPagina(i))
                        .isEqualTo((i-1) * MAXIMO_LANCAMENTOS)
        );
    }

    @Test(expectedExceptions = {IllegalStateException.class})
    public void buscaPorIdInexistenteTest(){
        Long id = 10L;
        when(lancamentoRepository.findById(id)).thenReturn(Optional.empty());
        when(lancamentoService.buscaPorId(id)).thenThrow(new IllegalStateException("Deveria ter o Lancamento pelo id: " + id));
        lancamentoService.buscaPorId(id);
    }

    @Test
    public void buscaPorIdTest(){
        var lancamento = novaRenda();
        Long id = 10L;
        when(lancamentoRepository.findById(id)).thenReturn(Optional.of(lancamento));
        var opt = lancamentoRepository.findById(id);
        assertThat(opt.orElseThrow(() -> new IllegalStateException("Deveria ter o Lancamento pelo id: " + id)))
                .isEqualTo(lancamento);
    }

    @Test
    public void contaCurrentMonthTest(){
        String itemBusca = "itemBusca";
        long countValue = 10L;
        when(LancamentoService.getCountSql(itemBusca) + " and " + CURRENT_MONTH_CLAUSE).thenCallRealMethod();
        String sql = LancamentoService.getCountSql(itemBusca) + " and " + CURRENT_MONTH_CLAUSE;
        when(entityManager.createQuery(sql, Long.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(countValue);
        assertThat(lancamentoService.contaCurrentMonth(itemBusca)).isEqualTo(countValue);
    }

    @Test(dataProvider = "lancamentos")
    @SuppressWarnings("unchecked")
    public void buscaTodosTest(List<Lancamento> lancamentos){
        int page = 3;
        when(entityManager.createNamedQuery("lancamento.maisRecentes", Lancamento.class)).thenReturn(query);
        when(query.setFirstResult(lancamentoService.calculaPrimeiroRegistroPorPagina(page))).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS).getResultList()).thenReturn(lancamentos);

        assertThat(lancamentoService.buscaTodosMesCorrente(page)).hasSameSizeAs(lancamentos);
        verify(entityManager, times(1)).createNamedQuery("lancamento.maisRecentes", Lancamento.class);
    }

    @Test(dataProvider = "lancamentos")
    public void buscaTodosBySearchingTest(List<Lancamento> lancamentos){
        int pagina = 3;
        String itemBusca = "itemBusca";
        mockQueryToBuscaTodosMeses(lancamentos, itemBusca, pagina);
        var result = lancamentoService.buscaTodosBySearching(pagina, itemBusca);
        assertThat(lancamentos).hasSameSizeAs(result);
        verify(lancamentoService).buscaTodosBySearching(pagina, itemBusca);
    }

    @Test(dataProvider = "lancamentos")
    @SuppressWarnings("unchecked")
    public void buscaTodosOrderingByDataLancamentoTest(List<Lancamento> lancamentos){
        when(entityManager.createNamedQuery("lancamento.byDataLancamento", Lancamento.class)).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS).getResultList()).thenReturn(lancamentos);

        assertThat(lancamentoService.buscaTodosOrderingByDataLancamento()).hasSameSizeAs(lancamentos);
        verify(entityManager, times(1)).createNamedQuery("lancamento.byDataLancamento", Lancamento.class);
    }

    @Test(dataProvider = "lancamentos")
    public void buscaTest(List<Lancamento> lancamentos){
        String itemBusca = "itemBusca";
        mockQueryToBuscaTodosMesCorrente(lancamentos, itemBusca);

        when(lancamentoService.buscaTodosMesCorrente(1, itemBusca)).thenReturn(lancamentos);
        assertThat(lancamentoService.buscaTodosMesCorrente(1, itemBusca)).hasSameSizeAs(lancamentos);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void contaTodosLancamentosTest(){
        when(entityManager.createQuery(LancamentoService.getCountSql(null), Long.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(12L);
        lancamentoService.conta(null);
    }

    @Test
    public void contaLancamentosPorDescricaoTest(){
        String itemBusca = "uma busca";
        long totalRegistros = 12L;
        mockToCount(itemBusca, totalRegistros);

        lancamentoService.conta(itemBusca);
    }

    private void mockQueryToBuscaTodosMesCorrente(List<Lancamento> lancamentos, String itemBusca){
        when(entityManager.createNamedQuery("lancamento.maisRecentesBySearching", Lancamento.class)).thenReturn(query);
        when(query.setParameter("itemBusca", "%"+itemBusca+"%")).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);
        when(lancamentoService.buscaTodosMesCorrente(1, itemBusca)).thenReturn(lancamentos);
    }

    private void mockQueryToBuscaTodosMeses(List<Lancamento> lancamentos, String itemBusca, int pagina){
        when(entityManager.createNamedQuery("lancamento.BySearching", Lancamento.class)).thenReturn(query);
        when(query.setParameter("itemBusca", "%" + itemBusca + "%")).thenReturn(query);
        when(query.setFirstResult(lancamentoService.calculaPrimeiroRegistroPorPagina(pagina))).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS).getResultList()).thenReturn(lancamentos);
    }

    @SuppressWarnings("unchecked")
    private void mockCalculaTotalGeralDespesa(BigDecimal totalGeral){
        when(entityManager.createNamedQuery("lancamento.soma.por.tipolancamento", BigDecimal.class)).thenReturn(query);
        when(query.setParameter("tipoLancamento", TipoLancamento.EXPENSE)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(totalGeral);
        when(lancamentoService.calculaTotalPorTipoLancamento(TipoLancamento.EXPENSE)).thenReturn(totalGeral);
        when(lancamentoService.calculaTotalGeralDespesa()).thenReturn(totalGeral);
    }

    @SuppressWarnings("unchecked")
    private void mockCalculaTotalGeralRenda(BigDecimal totalGeralEntrada){
        when(entityManager.createNamedQuery("lancamento.soma.por.tipolancamento", BigDecimal.class)).thenReturn(query);
        when(query.setParameter("tipoLancamento", TipoLancamento.INCOME)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(totalGeralEntrada);
        when(lancamentoService.calculaTotalPorTipoLancamento(TipoLancamento.INCOME)).thenReturn(totalGeralEntrada);
        when(lancamentoService.calculaTotalGeralRenda()).thenReturn(totalGeralEntrada);
        assertThat(lancamentoService.calculaTotalGeralRenda()).isEqualTo(totalGeralEntrada);
    }

    @Test(dataProvider = "lancamentos")
    public void buscaAjaxTestMesCorrente(List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        BuscaForm buscaForm = new BuscaForm("itemBusca", true);
        mockQueryToBuscaTodosMesCorrente(lancamentos, buscaForm.itemBusca());
        buscaAjax(buscaForm, lancamentos, totalSaida, totalEntrada);
    }

    @Test(dataProvider = "lancamentos")
    public void buscaAjaxTestTodosMeses(List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        BuscaForm buscaForm = new BuscaForm("itemBusca", false);
        lancamentos.add(LancamentoGen.builder()
                .comTipo(TipoLancamento.INCOME)
                .comDescricao(DataGen.productName())
                .comDataLancamento(DataGen.date())
                .comValor(DataGen.moneyValue())
                .build());
        mockQueryToBuscaTodosMeses(lancamentos, buscaForm.itemBusca(), 1);
        buscaAjax(buscaForm, lancamentos, totalSaida, totalEntrada);
    }

    private void buscaAjax(BuscaForm buscaForm, List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        String itemBusca = buscaForm.itemBusca();
        long totalRegistros = 123L;
        mockToCount(buscaForm.itemBusca(), totalRegistros);

        var resultBuscaTodos = buscaForm.searchOnlyCurrentMonth() ?
                lancamentoService.buscaTodosMesCorrente(1, buscaForm.itemBusca())
                : lancamentoService.buscaTodosBySearching(1, itemBusca);

        assertThat(resultBuscaTodos).hasSameSizeAs(lancamentos);

        final BigDecimal totalGeralSaida = totalSaida.multiply(BigDecimal.valueOf(2.5D));
        final BigDecimal totalGeralEntrada = totalEntrada.multiply(BigDecimal.valueOf(3L));
        mockCalculaTotalGeralDespesa(totalGeralSaida);
        mockCalculaTotalGeralRenda(totalGeralEntrada);

        StringToMoneyConverter converter = new StringToMoneyConverter();
        doCallRealMethod().when(lancamentoService).getResultado(lancamentos, totalRegistros, itemBusca);
        ResultadoRecord resultado = lancamentoService.getResultado(lancamentos, totalRegistros, itemBusca);
        assertEquals(converter.convert(resultado.totalGeralEntrada()), totalGeralEntrada.setScale(2, RoundingMode.HALF_UP));
        assertThat(resultado.lancamentos()).hasSameSizeAs(lancamentos);
        assertThat(resultado.totalRegistros()).isEqualTo(totalRegistros);
        when(lancamentoService.buscaAjax(buscaForm)).thenReturn(resultado);
        lancamentoService.buscaAjax(buscaForm);
    }

    @Test(dataProvider = "lancamentos")
    @SuppressWarnings("unchecked")
    public void getTotalPorPeriodoTest(List<TotalLancamentoRecord> list){

        Date dataInicial = DataGen.date(2023, 6, 10);
        Date dataFinal = DataGen.date(2023, 7, 10);
        when(entityManager.createNamedQuery("lancamento.totalLancamentosPorPeriodo", TotalLancamentoRecord.class))
                .thenReturn(query);
        when(query.setParameter("dataInicial", dataInicial)).thenReturn(query);
        when(query.setParameter("dataFinal", dataFinal)).thenReturn(query);
        when(query.getResultList()).thenReturn(list);

        when(lancamentoService.getTotalPorPeriodo(dataInicial, dataFinal)).thenReturn(list);

        List<TotalLancamentoRecord> result = lancamentoService.getTotalPorPeriodo(dataInicial, dataFinal);
        assertThat(result).hasSameSizeAs(list);

        assertThat(result)
                .filteredOn(l -> l.tipo() == TipoLancamento.EXPENSE)
                .isNotNull();
        assertThat(result)
                .filteredOn(l -> l.tipo() == TipoLancamento.INCOME)
                .isNotNull();
    }

    @Test(dataProvider = "lancamentos")
    public void getTotalPorCategoriaTest(List<TotalLancamentoCategoriaRecord> list){
        Date dataInicial = DataGen.date(2023, 6, 10);
        Date dataFinal = DataGen.date(2023, 7, 10);
        when(entityManager.createNamedQuery("lancamento.totalLancamentosPorPeriodoPorCategoria", TotalLancamentoCategoriaRecord.class))
                .thenReturn(query);
        when(query.setParameter("dataInicial", dataInicial)).thenReturn(query);
        when(query.setParameter("dataFinal", dataFinal)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(list);

        when(lancamentoService.getTotalPorPeriodoPorCategoria(dataInicial, dataFinal)).thenReturn(list);
        List<TotalLancamentoCategoriaRecord> result = lancamentoService.getTotalPorPeriodoPorCategoria(dataInicial, dataFinal);
        assertThat(result).hasSameSizeAs(list);
        assertThat(result)
                .filteredOn(l -> l.tipo() == TipoLancamento.EXPENSE)
                .isNotNull();
        assertThat(result)
                .filteredOn(l -> l.tipo() == TipoLancamento.INCOME)
                .isNotNull();
    }

    @Test
    public void truncateTableTest(){
        doNothing().when(lancamentoRepository).truncateTable();
        lancamentoService.truncateTable();
        verify(lancamentoRepository).truncateTable();
    }

    @SuppressWarnings("unchecked")
    private void mockToCount(String itemBusca, long totalRegistros){
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(LancamentoService.getCountSql(itemBusca), Long.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(totalRegistros);
        when(lancamentoService.conta(itemBusca)).thenReturn(totalRegistros);
    }


    private BigDecimal addAndSum(List<Lancamento> list, Lancamento lancamento, BigDecimal total){
        list.add(lancamento);
        return total.add(lancamento.getValor());
    }

    @DataProvider(name = "lancamentos", parallel = true)
    protected Object[][] getLancamentos(Method method){
        BigDecimal entrada = BigDecimal.ZERO;
        BigDecimal saida = BigDecimal.ZERO;
        int size = DataGen.number(10, 30);
        List<Lancamento> list;
        switch (method.getName()) {
            case "getTotalPorPeriodoTest" -> {
                List<TotalLancamentoRecord> listTotalLancamento = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    var tipo = (i % 3 == 0) ? TipoLancamento.INCOME : TipoLancamento.EXPENSE;
                    listTotalLancamento.add(new TotalLancamentoRecord(
                            BigDecimal.valueOf(DataGen.moneyValue()), tipo)
                    );
                }
                return new Object[][]{{listTotalLancamento}};
            }
            case "getTotalPorCategoriaTest" -> {
                List<TotalLancamentoCategoriaRecord> listTotalLancamentoCategoria = new ArrayList<>();
                var categorias = Categoria.values();
                for (int i = 0; i < size; i++) {
                    var tipo = (i % 3 == 0) ? TipoLancamento.INCOME : TipoLancamento.EXPENSE;
                    int indice = DataGen.number(0, categorias.length - 1);
                    listTotalLancamentoCategoria.add(new TotalLancamentoCategoriaRecord(
                            BigDecimal.valueOf(DataGen.moneyValue()), tipo, categorias[indice])
                    );
                }
                return new Object[][]{{listTotalLancamentoCategoria}};
            }
            case "getTotalSaidaTest" -> {
                list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    saida = addAndSum(list, novaDespesa(), saida);
                }
                return new Object[][]{{list, saida}};
            }
            case "getTotalEntradaTest" -> {
                list = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    entrada = addAndSum(list, novaRenda(), entrada);
                }
                return new Object[][]{{list, entrada}};
            }
            case "buscaAjaxTestMesCorrente", "buscaAjaxTestTodosMeses", "getResultadoTest" -> {
                list = new ArrayList<>(MAXIMO_LANCAMENTOS);
                for (int i = 0; i < MAXIMO_LANCAMENTOS; i++) {
                    if ((i % 3) == 0) {
                        list.add(novaRenda());
                        entrada = addAndSum(list, novaRenda(), entrada);
                    } else {
                        list.add(novaDespesa());
                        saida = addAndSum(list, novaDespesa(), saida);
                    }
                }
                return new Object[][]{{list, saida, entrada}};
            }
            default -> {
                list = new ArrayList<>(MAXIMO_LANCAMENTOS);
                for (int i = 0; i < MAXIMO_LANCAMENTOS; i++) {
                    if ((i % 3) == 0) {
                        list.add(novaRenda());
                    } else {
                        list.add(novaDespesa());
                    }
                }
                return new Object[][]{{list}};
            }
        }
    }

    @DataProvider(name = "entriesAndPages", parallel = true)
    protected Object[][] entriesAndPages(){
        return new Object[][]{
                {0,0},
                {7,1},
                {200,20},
                {201,21},
                {500,50},
        };
    }
}
