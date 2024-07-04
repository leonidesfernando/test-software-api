package br.com.home.lab.softwaretesting.service;

import br.com.home.lab.softwaretesting.controller.record.FormSearch;
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
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static br.com.home.lab.softwaretesting.model.Lancamento.CURRENT_MONTH_CLAUSE;
import static br.com.home.lab.softwaretesting.service.LancamentoService.MAXIMO_LANCAMENTOS;
import static br.com.home.lab.softwaretesting.util.LancamentoGen.novaDespesa;
import static br.com.home.lab.softwaretesting.util.LancamentoGen.novaRenda;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.CONCURRENT)
@SpringBootTest
class LancamentoServiceTest {

    @Spy
    @InjectMocks
    private LancamentoService lancamentoService;

    @Mock
    private LancamentoRepository lancamentoRepository;

    @Mock
    private EntityManager entityManager;
    @Mock
    TypedQuery query;

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void salvarTest(){
        given(lancamentoRepository.save(any(Lancamento.class)))
                .willReturn(new Lancamento());

        lancamentoService.salvar(new Lancamento());

        verify(lancamentoRepository, times(1))
                .save(any(Lancamento.class));
    }

    @Test
    void removerTest(){
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

    @ParameterizedTest
    @MethodSource("totalSaidaData")
    void getTotalSaidaTest(List<Lancamento> lancamentos, BigDecimal totalSaidaEsperado){
        BigDecimal totalObtido = lancamentoService.getTotalDespesa(lancamentos);
        assertThat(totalObtido).isEqualTo(totalSaidaEsperado);
    }

    @ParameterizedTest
    @MethodSource("totalEntradaData")
    void getTotalEntradaTest(List<Lancamento> lancamentos, BigDecimal totalEntradaEsperado){
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
    void getPaginasTest(){
        when(lancamentoService.calculaNumeroPaginas(anyInt())).thenCallRealMethod();
        when(lancamentoService.getPaginas(anyInt())).thenCallRealMethod();
        int totalRegistros = DataGen.number(5, 50);
        int numeroPaginas = lancamentoService.calculaNumeroPaginas(totalRegistros);
        var list = lancamentoService.getPaginas(totalRegistros);

        assertThat(list)
                .isEqualTo(IntStream.rangeClosed(1, numeroPaginas)
                        .boxed().collect(toList()));
    }

    @ParameterizedTest
    @MethodSource("entriesAndPages")
    void calculaNumeroPaginasTest(int totalRegistos, int totalPaginas){
        assertThat(lancamentoService.calculaNumeroPaginas(totalRegistos)).isEqualTo(totalPaginas);
    }

    @Test
    void calculaNumeroPaginasNegativeCaseTest(){
        assertThat(lancamentoService.calculaNumeroPaginas(500)).isNotEqualTo(5);
    }


    @Test
    void calculaPrimeiroRegistroPorPaginaTest(){
        assertThat(lancamentoService.calculaPrimeiroRegistroPorPagina(0)).isZero();
        int pages = 10;
        IntStream.rangeClosed(1, pages).forEach(i ->
                assertThat(lancamentoService.calculaPrimeiroRegistroPorPagina(i))
                        .isEqualTo((i-1) * MAXIMO_LANCAMENTOS)
        );
    }

    @Test
    void buscaPorIdInexistenteTest(){
        Long id = 10L;
        when(lancamentoRepository.findById(id)).thenReturn(Optional.empty());
        IllegalStateException exe = assertThrows(IllegalStateException.class, () ->
                lancamentoService.buscaPorId(id)
        );
        assertEquals("Deveria ter o Lancamento pelo id: " + id, exe.getMessage());
    }

    @Test
    void buscaPorIdTest(){
        var lancamento = novaRenda();
        Long id = 10L;
        when(lancamentoRepository.findById(id)).thenReturn(Optional.of(lancamento));
        var opt = lancamentoRepository.findById(id);
        assertThat(opt.orElseThrow(() -> new IllegalStateException("Deveria ter o Lancamento pelo id: " + id)))
                .isEqualTo(lancamento);
    }

    @Test
    void contaCurrentMonthTest(){
        String itemBusca = "searchItem";
        long countValue = 10L;

        try(MockedStatic<LancamentoService> lancamentoServiceMockedStatic = Mockito.mockStatic(LancamentoService.class)){
            lancamentoServiceMockedStatic.when(() -> LancamentoService.getCountSql(itemBusca)).thenCallRealMethod();
        }

        String sql = LancamentoService.getCountSql(itemBusca) + " and " + CURRENT_MONTH_CLAUSE;
        when(entityManager.createQuery(sql, Long.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(countValue);
        assertThat(lancamentoService.contaCurrentMonth(itemBusca)).isEqualTo(countValue);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    @SuppressWarnings("unchecked")
    void buscaTodosTest(List<Lancamento> lancamentos){
        int page = 3;
        when(entityManager.createNamedQuery("lancamento.maisRecentes", Lancamento.class)).thenReturn(query);
        when(query.setFirstResult(lancamentoService.calculaPrimeiroRegistroPorPagina(page))).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);

        assertThat(lancamentoService.buscaTodosMesCorrente(page)).hasSameSizeAs(lancamentos);
        verify(entityManager, times(1)).createNamedQuery("lancamento.maisRecentes", Lancamento.class);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    void buscaTodosBySearchingTest(List<Lancamento> lancamentos){
        int pagina = 3;
        String itemBusca = "searchItem";
        mockQueryToBuscaTodosMeses(lancamentos, itemBusca, pagina);
        var result = lancamentoService.buscaTodosBySearching(pagina, itemBusca);
        assertThat(lancamentos).hasSameSizeAs(result);
        verify(lancamentoService).buscaTodosBySearching(pagina, itemBusca);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    @SuppressWarnings("unchecked")
    void buscaTodosOrderingByDataLancamentoTest(List<Lancamento> lancamentos){
        when(entityManager.createNamedQuery("lancamento.byDataLancamento", Lancamento.class)).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);

        assertThat(lancamentoService.buscaTodosOrderingByDataLancamento()).hasSameSizeAs(lancamentos);
        verify(entityManager, times(1)).createNamedQuery("lancamento.byDataLancamento", Lancamento.class);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    void buscaTest(List<Lancamento> lancamentos){
        String itemBusca = "searchItem";
        mockQueryToBuscaTodosMesCorrente(lancamentos, itemBusca);

        when(lancamentoService.buscaTodosMesCorrente(1, itemBusca)).thenReturn(lancamentos);
        assertThat(lancamentoService.buscaTodosMesCorrente(1, itemBusca)).hasSameSizeAs(lancamentos);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    void buscaTodosPorPaginaTexto(List<Lancamento> lancamentos){
        String itemBusca = "itemBusca";
        int pagina = 1;
        when(entityManager.createNamedQuery("lancamento.maisRecentesBySearching", Lancamento.class)).thenReturn(query);
        when(query.setParameter("itemBusca", "%"+itemBusca+"%")).thenReturn(query);
        when(query.setFirstResult(lancamentoService.calculaPrimeiroRegistroPorPagina(pagina))).thenReturn(query);
        when(query.setMaxResults(LancamentoService.MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);
        when(lancamentoService.buscaTodosMesCorrente(pagina, itemBusca)).thenReturn(lancamentos);

        int size = lancamentoService.buscaTodosMesCorrente(pagina, itemBusca).size();
        assertEquals(size, lancamentos.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void contaTodosLancamentosTest(){
        long totalRegistros = 12L;
        when(entityManager.createQuery(LancamentoService.getCountSql(null), Long.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(totalRegistros);
        assertEquals(lancamentoService.conta(null), totalRegistros);
    }

    @Test
    void contaLancamentosPorDescricaoTest(){
        String itemBusca = "uma busca";
        long totalRegistros = 12L;
        mockToCount(itemBusca, totalRegistros);

        assertEquals(lancamentoService.conta(itemBusca), totalRegistros);
    }

    private void mockQueryToBuscaTodosMesCorrente(List<Lancamento> lancamentos, String itemBusca){
        when(entityManager.createNamedQuery("lancamento.maisRecentesBySearching", Lancamento.class)).thenReturn(query);
        when(query.setParameter("searchItem", "%"+itemBusca+"%")).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);
        when(lancamentoService.buscaTodosMesCorrente(1, itemBusca)).thenReturn(lancamentos);
    }

    private void mockQueryToBuscaTodosMeses(List<Lancamento> lancamentos, String itemBusca, int pagina){
        when(entityManager.createNamedQuery("lancamento.BySearching", Lancamento.class)).thenReturn(query);
        when(query.setParameter("searchItem", "%" + itemBusca + "%")).thenReturn(query);
        when(query.setFirstResult(lancamentoService.calculaPrimeiroRegistroPorPagina(pagina))).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);
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

    @ParameterizedTest
    @MethodSource("genAjaxSearchData")
    void buscaAjaxTestMesCorrente(List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        FormSearch formSearch = new FormSearch("searchItem", true);
        mockQueryToBuscaTodosMesCorrente(lancamentos, formSearch.searchItem());
        buscaAjax(formSearch, lancamentos, totalSaida, totalEntrada);
    }

    @ParameterizedTest
    @MethodSource("genAjaxSearchData")
    void buscaAjaxTestTodosMeses(List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        FormSearch formSearch = new FormSearch("searchItem", false);
        lancamentos.add(LancamentoGen.builder()
                .comTipo(TipoLancamento.INCOME)
                .comDescricao(DataGen.productName())
                .comDataLancamento(DataGen.date())
                .comValor(DataGen.moneyValue())
                .build());
        mockQueryToBuscaTodosMeses(lancamentos, formSearch.searchItem(), 1);
        buscaAjax(formSearch, lancamentos, totalSaida, totalEntrada);
    }

    private void buscaAjax(FormSearch formSearch, List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        String itemBusca = formSearch.searchItem();
        long totalRegistros = 123L;
        mockToCount(formSearch.searchItem(), totalRegistros);

        var resultBuscaTodos = formSearch.searchOnlyCurrentMonth() ?
                lancamentoService.buscaTodosMesCorrente(1, formSearch.searchItem())
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
        when(lancamentoService.buscaAjax(formSearch)).thenReturn(resultado);
        lancamentoService.buscaAjax(formSearch);
    }

    @ParameterizedTest
    @MethodSource("totalPorPeriodoData")
    @SuppressWarnings("unchecked")
    void getTotalPorPeriodoTest(List<TotalLancamentoRecord> list){

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

    @ParameterizedTest
    @MethodSource("totalPorCategoriaData")
    void getTotalPorCategoriaTest(List<TotalLancamentoCategoriaRecord> list){
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
    void truncateTableTest(){
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


    private static BigDecimal addAndSum(List<Lancamento> list, Lancamento lancamento, BigDecimal total){
        list.add(lancamento);
        return total.add(lancamento.getValor());
    }

    private static Stream<Arguments> totalEntradaData(){
        var params = getData();
        List<Lancamento> list = new ArrayList<>(params.size);
        for(int i = 0; i < params.size; i++){
            params.entrada = addAndSum(list, novaRenda(), params.entrada);
        }
        return Stream.of(Arguments.of(list, params.entrada));
    }
    private static Stream<Arguments> totalSaidaData(){
        var params = getData();
        List<Lancamento> list = new ArrayList<>(params.size);
        for(int i = 0; i < params.size; i++){
            params.saida = addAndSum(list, novaDespesa(), params.saida);
        }
        return Stream.of(Arguments.of(list, params.saida));
    }

    private static Stream<Arguments> genDefaultData(){
        var list = new ArrayList<>(LancamentoService.MAXIMO_LANCAMENTOS);
        for(int i = 0; i < LancamentoService.MAXIMO_LANCAMENTOS; i++){
            if((i % 2) == 0){
                list.add(novaRenda());
            }else {
                list.add(novaDespesa());
            }
        }
        return Stream.of(Arguments.of(list));
    }

    private static Stream<Arguments> genAjaxSearchData(){
        var param = getData();
        List<Lancamento> list = new ArrayList<>(LancamentoService.MAXIMO_LANCAMENTOS);
        for(int i = 0; i < LancamentoService.MAXIMO_LANCAMENTOS; i++){
            if((i % 3) == 0){
                list.add(novaRenda());
                param.entrada = addAndSum(list, novaRenda(), param.entrada);
            }else {
                list.add(novaDespesa());
                param.saida = addAndSum(list, novaDespesa(), param.saida);
            }
        }
        return Stream.of(Arguments.of(list, param.saida, param.entrada));
    }

    private static Stream<Arguments> totalPorCategoriaData(){
        List<TotalLancamentoCategoriaRecord> listTotalLancamentoCategoria = new ArrayList<>();
        int size = DataGen.number(5, 30);
        var categorias = Categoria.values();
        for (int i = 0; i < size; i++) {
            var tipo = (i % 3 == 0) ? TipoLancamento.INCOME : TipoLancamento.EXPENSE;
            int indice = DataGen.number(0, categorias.length - 1);
            listTotalLancamentoCategoria.add(new TotalLancamentoCategoriaRecord(
                    BigDecimal.valueOf(DataGen.moneyValue()), tipo, categorias[indice])
            );
        }
        return Stream.of(Arguments.of(listTotalLancamentoCategoria));
    }

    private static Stream<Arguments> totalPorPeriodoData(){
        List<TotalLancamentoRecord> listTotalLancamento = new ArrayList<>();
        int size = DataGen.number(4, 30);
        for(int i = 0; i < size; i++){
            var tipo = (i % 3 == 0) ? TipoLancamento.INCOME : TipoLancamento.EXPENSE;
            listTotalLancamento.add(new TotalLancamentoRecord(
                    BigDecimal.valueOf(DataGen.moneyValue()), tipo)
            );
        }
        return Stream.of(Arguments.of(listTotalLancamento));
    }

    private static Stream<Arguments> entriesAndPages(){
        return Stream.of(
                Arguments.of(0,0),
                Arguments.of(7,1),
                Arguments.of(200,20),
                Arguments.of(201,21),
                Arguments.of(500,50)
        );
    }

    private static DataTestInitial getData(){
        return new DataTestInitial(BigDecimal.ZERO, BigDecimal.ZERO, DataGen.number(30));
    }
    @AllArgsConstructor
    static class DataTestInitial{
        BigDecimal entrada;
        BigDecimal saida;
        int size;
    }
}
