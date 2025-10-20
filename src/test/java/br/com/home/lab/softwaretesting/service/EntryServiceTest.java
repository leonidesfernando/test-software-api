package br.com.home.lab.softwaretesting.service;

import br.com.home.lab.softwaretesting.controller.record.FormSearch;
import br.com.home.lab.softwaretesting.controller.record.ResultRecord;
import br.com.home.lab.softwaretesting.controller.record.TotalLancamentoCategoriaRecord;
import br.com.home.lab.softwaretesting.controller.record.TotalLancamentoRecord;
import br.com.home.lab.softwaretesting.converter.StringToMoneyConverter;
import br.com.home.lab.softwaretesting.model.Category;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import br.com.home.lab.softwaretesting.model.User;
import br.com.home.lab.softwaretesting.repository.LancamentoRepository;
import br.com.home.lab.softwaretesting.repository.UserRepository;
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
import static br.com.home.lab.softwaretesting.service.EntryService.MAXIMO_LANCAMENTOS;
import static br.com.home.lab.softwaretesting.service.EntryService.getCountSql;
import static br.com.home.lab.softwaretesting.util.LancamentoGen.novaDespesa;
import static br.com.home.lab.softwaretesting.util.LancamentoGen.novaRenda;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Execution(ExecutionMode.CONCURRENT)
@SpringBootTest
class EntryServiceTest {

    private static final long USER_ID_MOCK = 1L;
    private  static final User LOGGED_USER_MOCK = new User(USER_ID_MOCK);

    @Spy
    @InjectMocks
    private EntryService entryService;

    @Mock
    private LancamentoRepository lancamentoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private EntityManager entityManager;
    @Mock
    TypedQuery query;

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveTest(){
        given(lancamentoRepository.save(any(Lancamento.class)))
                .willReturn(new Lancamento());

        entryService.save(new Lancamento());

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
                .comCategoria(Category.WAGE)
                .build();

        when(lancamentoRepository.getById(id)).thenReturn(lancamento);
        entryService.remover(id);

        verify(lancamentoRepository, times(1))
                .getById(anyLong());
        verify(lancamentoRepository, times(1))
                .delete(any(Lancamento.class));
    }

    @ParameterizedTest
    @MethodSource("totalSaidaData")
    void getTotalSaidaTest(List<Lancamento> lancamentos, BigDecimal totalSaidaEsperado){
        BigDecimal totalObtido = entryService.getTotalDespesa(lancamentos);
        assertThat(totalObtido).isEqualTo(totalSaidaEsperado);
    }

    @ParameterizedTest
    @MethodSource("totalEntradaData")
    void getTotalEntradaTest(List<Lancamento> lancamentos, BigDecimal totalEntradaEsperado){
        lancamentos.add(LancamentoGen.builder()
                .comTipo(TipoLancamento.INCOME)
                .comDescricao(DataGen.productName())
                .comDataLancamento(DataGen.date())
                .comCategoria(Category.WAGE)
                .comUsuario()
                .build());

        when(entryService.somaValoresPorTipo(lancamentos, TipoLancamento.INCOME)).thenCallRealMethod();
        when(entryService.getTotalRenda(lancamentos)).thenCallRealMethod();
        assertThat(totalEntradaEsperado).isEqualTo(entryService.getTotalRenda(lancamentos));
    }

    @Test
    void getPaginasTest(){
        when(entryService.calculaNumeroPaginas(anyInt())).thenCallRealMethod();
        when(entryService.getPaginas(anyInt())).thenCallRealMethod();
        int totalRegistros = DataGen.number(5, 50);
        int numeroPaginas = entryService.calculaNumeroPaginas(totalRegistros);
        var list = entryService.getPaginas(totalRegistros);

        assertThat(list)
                .isEqualTo(IntStream.rangeClosed(1, numeroPaginas)
                        .boxed().toList());
    }

    @ParameterizedTest
    @MethodSource("entriesAndPages")
    void calculaNumeroPaginasTest(int totalRegistos, int totalPaginas){
        assertThat(entryService.calculaNumeroPaginas(totalRegistos)).isEqualTo(totalPaginas);
    }

    @Test
    void calculaNumeroPaginasNegativeCaseTest(){
        assertThat(entryService.calculaNumeroPaginas(500)).isNotEqualTo(5);
    }


    @Test
    void calculaPrimeiroRegistroPorPaginaTest(){
        assertThat(entryService.calculaPrimeiroRegistroPorPagina(0)).isZero();
        int pages = 10;
        IntStream.rangeClosed(1, pages).forEach(i ->
                assertThat(entryService.calculaPrimeiroRegistroPorPagina(i))
                        .isEqualTo((i-1) * MAXIMO_LANCAMENTOS)
        );
    }

    @Test
    void buscaPorIdInexistenteTest(){
        Long id = 99L;
        when(lancamentoRepository.findById(id)).thenReturn(Optional.empty());
        IllegalStateException exe = assertThrows(IllegalStateException.class, () ->
                entryService.searchById(id)
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
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        String username = "user";
        User user = new User("Name",username, "", "a");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        userDetailsService.loadUserByUsername(username);

        verify(userDetailsService).loadUserByUsername(username);
    }

    @Test
    void contaCurrentMonthTest(){
        String itemBusca = "searchItem";
        long countValue = 10L;

        try(MockedStatic<EntryService> lancamentoServiceMockedStatic = Mockito.mockStatic(EntryService.class)){
            lancamentoServiceMockedStatic.when(() -> getCountSql(itemBusca)).thenCallRealMethod();
        }

        String sql = getCountSql(itemBusca) + " and " + CURRENT_MONTH_CLAUSE;
        when(entityManager.createQuery(sql, Long.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(countValue);
        assertThat(entryService.contaCurrentMonth(itemBusca, LOGGED_USER_MOCK)).isEqualTo(countValue);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    @SuppressWarnings("unchecked")
    void buscaTodosTest(List<Lancamento> lancamentos){
        int page = 3;
        when(entityManager.createNamedQuery("lancamento.maisRecentes", Lancamento.class)).thenReturn(query);
        when(query.setFirstResult(entryService.calculaPrimeiroRegistroPorPagina(page))).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);

        assertThat(entryService.buscaTodosMesCorrente(page)).hasSameSizeAs(lancamentos);
        verify(entityManager, times(1)).createNamedQuery("lancamento.maisRecentes", Lancamento.class);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    void buscaTodosBySearchingTest(List<Lancamento> lancamentos){
        int pagina = 3;
        String itemBusca = "searchItem";
        mockQueryToBuscaTodosMeses(lancamentos, itemBusca, pagina);

        var result = entryService.buscaTodosBySearching(pagina, itemBusca, LOGGED_USER_MOCK);
        assertThat(lancamentos).hasSameSizeAs(result);

        verify(entryService).buscaTodosBySearching(pagina, itemBusca, LOGGED_USER_MOCK);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    @SuppressWarnings("unchecked")
    void buscaTodosOrderingByDataLancamentoTest(List<Lancamento> lancamentos){
        when(entityManager.createNamedQuery("lancamento.byDataLancamento", Lancamento.class)).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);

        assertThat(entryService.buscaTodosOrderingByDataLancamento()).hasSameSizeAs(lancamentos);
        verify(entityManager, times(1)).createNamedQuery("lancamento.byDataLancamento", Lancamento.class);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    void buscaTest(List<Lancamento> lancamentos){
        String itemBusca = "searchItem";
        mockQueryToBuscaTodosMesCorrente(lancamentos, itemBusca, 1);
        assertThat(entryService.buscaTodosMesCorrente(1, itemBusca, LOGGED_USER_MOCK)).hasSameSizeAs(lancamentos);
    }

    @ParameterizedTest
    @MethodSource("genDefaultData")
    void buscaTodosPorPaginaTexto(List<Lancamento> lancamentos){
        String itemBusca = "itemBusca";
        int pagina = 1;

        mockQueryToBuscaTodosMesCorrente(lancamentos, itemBusca, pagina);
        int size = entryService.buscaTodosMesCorrente(pagina, itemBusca, LOGGED_USER_MOCK).size();
        assertEquals(size, lancamentos.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void contaTodosLancamentosTest(){
        when(entityManager.createQuery(getCountSql(null), Long.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(12L);
        assertThat(entryService.conta(null, LOGGED_USER_MOCK)).isEqualTo(12L);
    }

    @Test
    void contaLancamentosPorDescricaoTest(){
        String itemBusca = "uma busca";
        long totalRegistros = 12L;
        mockToCount(itemBusca, totalRegistros);
        assertThat(entryService.conta(itemBusca, LOGGED_USER_MOCK)).isEqualTo(totalRegistros);
    }

    private void mockQueryToBuscaTodosMesCorrente(List<Lancamento> lancamentos, String itemBusca, int pagina){
        when(entityManager.createNativeQuery(Lancamento.LANCAMENTO_MAIS_RECENTES_BY_SEARCHING, Lancamento.class)).thenReturn(query);
        when(query.setParameter("userId", USER_ID_MOCK)).thenReturn(query);
        when(query.setParameter("searchItem", "%"+itemBusca+"%")).thenReturn(query);
        when(query.setFirstResult(anyInt())).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);

        when(entryService.buscaTodosMesCorrente(pagina, itemBusca, LOGGED_USER_MOCK)).thenReturn(lancamentos);
    }

    private void mockQueryToBuscaTodosMeses(List<Lancamento> lancamentos, String itemBusca, int pagina){
        when(entityManager.createNamedQuery("lancamento.BySearching", Lancamento.class)).thenReturn(query);
        when(query.setParameter("user", LOGGED_USER_MOCK)).thenReturn(query);
        when(query.setParameter("searchItem", itemBusca)).thenReturn(query);
        when(query.setFirstResult(entryService.calculaPrimeiroRegistroPorPagina(pagina))).thenReturn(query);
        when(query.setMaxResults(MAXIMO_LANCAMENTOS)).thenReturn(query);
        when(query.getResultList()).thenReturn(lancamentos);
    }

    @SuppressWarnings("unchecked")
    private void mockCalculaTotalGeralDespesa(BigDecimal totalGeral){
        when(entityManager.createNamedQuery("lancamento.soma.por.tipolancamento", BigDecimal.class)).thenReturn(query);
        when(query.setParameter("tipoLancamento", TipoLancamento.EXPENSE)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(totalGeral);
        when(entryService.calculaTotalPorTipoLancamento(TipoLancamento.EXPENSE)).thenReturn(totalGeral);
        when(entryService.calculaTotalGeralDespesa()).thenReturn(totalGeral);
    }

    @SuppressWarnings("unchecked")
    private void mockCalculaTotalGeralRenda(BigDecimal totalGeralEntrada){
        when(entityManager.createNamedQuery("lancamento.soma.por.tipolancamento", BigDecimal.class)).thenReturn(query);
        when(query.setParameter("tipoLancamento", TipoLancamento.INCOME)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(totalGeralEntrada);
        when(entryService.calculaTotalPorTipoLancamento(TipoLancamento.INCOME)).thenReturn(totalGeralEntrada);
        when(entryService.calculaTotalGeralRenda()).thenReturn(totalGeralEntrada);
        assertThat(entryService.calculaTotalGeralRenda()).isEqualTo(totalGeralEntrada);
    }

    @ParameterizedTest
    @MethodSource("genAjaxSearchData")
    void ajaxSearchTestMesCorrente(List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        FormSearch formSearch = new FormSearch("searchItem", true, 0, 1L);
        ajaxSearch(formSearch, lancamentos, totalSaida, totalEntrada);
    }

    @ParameterizedTest
    @MethodSource("genAjaxSearchData")
    void ajaxSearchTestTodosMeses(List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        FormSearch formSearch = new FormSearch("searchItem", false, 1, 1L);
        mockQueryToBuscaTodosMeses(lancamentos, formSearch.searchItem(), 1);
        ajaxSearch(formSearch, lancamentos, totalSaida, totalEntrada);
    }

    private void ajaxSearch(FormSearch formSearch, List<Lancamento> lancamentos, final BigDecimal totalSaida, final BigDecimal totalEntrada){
        String itemBusca = formSearch.searchItem();
        long totalRegistros = 123L;

        mockToCount(formSearch.searchItem(), totalRegistros);

        var resultBuscaTodosMesCorrente = formSearch.searchOnlyCurrentMonth();

        if(resultBuscaTodosMesCorrente){
            mockQueryToBuscaTodosMesCorrente(lancamentos, itemBusca, 1);
            entryService.buscaTodosMesCorrente(1, formSearch.searchItem(), LOGGED_USER_MOCK);
        }else{
            //TODO: adicionar o mock
            entryService.buscaTodosBySearching(1, itemBusca, LOGGED_USER_MOCK);
        }

        final BigDecimal totalGeralSaida = totalSaida.multiply(BigDecimal.valueOf(2.5D));
        final BigDecimal totalGeralEntrada = totalEntrada.multiply(BigDecimal.valueOf(3L));
        mockCalculaTotalGeralDespesa(totalGeralSaida);
        mockCalculaTotalGeralRenda(totalGeralEntrada);

        String sql = getCountSql(itemBusca) + " and " + CURRENT_MONTH_CLAUSE;
        when(entityManager.createQuery(sql, Long.class)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        StringToMoneyConverter converter = new StringToMoneyConverter();
        doCallRealMethod().when(entryService).getResultado(lancamentos, totalRegistros, itemBusca, 1);
        ResultRecord resultado = entryService.getResultado(lancamentos, totalRegistros, itemBusca, 1);
        assertEquals(converter.convert(resultado.grandTotalWinnings()), totalGeralEntrada.setScale(2, RoundingMode.HALF_UP));
        assertThat(resultado.entries()).hasSameSizeAs(lancamentos);
        assertThat(resultado.totalRecords()).isEqualTo(totalRegistros);
        when(userRepository.findById(1L)).thenReturn(Optional.of(LOGGED_USER_MOCK));
        when(entryService.ajaxSearch(formSearch)).thenReturn(resultado);
        entryService.ajaxSearch(formSearch);
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

        when(entryService.getTotalPorPeriodo(dataInicial, dataFinal)).thenReturn(list);

        List<TotalLancamentoRecord> result = entryService.getTotalPorPeriodo(dataInicial, dataFinal);
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

        when(entryService.getTotalPorPeriodoPorCategoria(dataInicial, dataFinal)).thenReturn(list);
        List<TotalLancamentoCategoriaRecord> result = entryService.getTotalPorPeriodoPorCategoria(dataInicial, dataFinal);
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
        doNothing().when(lancamentoRepository).removeAllByUser(anyLong());
        entryService.removeAllByUser(1L);
        verify(lancamentoRepository).removeAllByUser(anyLong());
    }

    @SuppressWarnings("unchecked")
    private void mockToCount(String itemBusca, long totalRegistros){
        TypedQuery<Long> longTypedQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(getCountSql(itemBusca), Long.class)).thenReturn(longTypedQuery);
        when(longTypedQuery.getSingleResult()).thenReturn(totalRegistros);
        when(entryService.conta(itemBusca, LOGGED_USER_MOCK)).thenReturn(totalRegistros);
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
        var list = new ArrayList<>(EntryService.MAXIMO_LANCAMENTOS);
        for(int i = 0; i < EntryService.MAXIMO_LANCAMENTOS; i++){
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
        List<Lancamento> list = new ArrayList<>(EntryService.MAXIMO_LANCAMENTOS);
        for(int i = 0; i < EntryService.MAXIMO_LANCAMENTOS; i++){
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
        var categorias = Category.values();
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
