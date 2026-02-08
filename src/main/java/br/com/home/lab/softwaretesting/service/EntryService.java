package br.com.home.lab.softwaretesting.service;

import br.com.home.lab.softwaretesting.controller.record.EntryRecord;
import br.com.home.lab.softwaretesting.controller.record.FormSearch;
import br.com.home.lab.softwaretesting.controller.record.ResultRecord;
import br.com.home.lab.softwaretesting.controller.record.TotalLancamentoCategoriaRecord;
import br.com.home.lab.softwaretesting.controller.record.TotalLancamentoRecord;
import br.com.home.lab.softwaretesting.converter.MoneyToStringConverter;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import br.com.home.lab.softwaretesting.model.User;
import br.com.home.lab.softwaretesting.repository.LancamentoRepository;
import br.com.home.lab.softwaretesting.repository.UserRepository;
import br.com.home.lab.softwaretesting.util.Constantes;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static br.com.home.lab.softwaretesting.model.Lancamento.CURRENT_MONTH_CLAUSE;

@Service
@AllArgsConstructor
public class EntryService {

    static final String SQL_COUNT_BASE = "select count(*) from Lancamento l where  ";
    static final String SQL_COUNT_WHERE = " ( (upper(l.descricao) like upper( :searchItem)) " +
            "  or (upper(l.tipoLancamento) like upper( :searchItem)) " +
            " or (upper(l.category) like upper( :searchItem)) ) and l.user = :user ";

    @PersistenceContext
    private EntityManager entityManager;
    static final int MAXIMO_LANCAMENTOS = 10;
    private LancamentoRepository lancamentoRepository;

    @NotNull
    private UserRepository userRepository;


    @Transactional
    public Lancamento save(Lancamento lancamento) {
        return lancamentoRepository.save(lancamento);
    }

    @Transactional
    public void remover(long id) {
        lancamentoRepository.delete(lancamentoRepository.getById(id));
    }

    public List<Lancamento> buscaTodosMesCorrente(int pagina) {

        return entityManager.createNamedQuery("lancamento.maisRecentes", Lancamento.class)
                .setFirstResult(calculaPrimeiroRegistroPorPagina(pagina))
                .setMaxResults(MAXIMO_LANCAMENTOS).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Lancamento> buscaTodosMesCorrente(int pagina, String itemBusca, User loggedUser) {
        return entityManager.createNativeQuery(Lancamento.LANCAMENTO_MAIS_RECENTES_BY_SEARCHING, Lancamento.class)
                .setParameter("userId", loggedUser.getId())
                .setParameter("searchItem", "%"+itemBusca+"%")
                .setFirstResult(calculaPrimeiroRegistroPorPagina(pagina))
                .setMaxResults(MAXIMO_LANCAMENTOS).getResultList();
    }

    public List<Lancamento> buscaTodosBySearching(int pagina, String itemBusca, User loggedUser) {
        return entityManager.createNamedQuery("lancamento.BySearching", Lancamento.class)
                .setParameter("user", loggedUser)
                .setParameter("searchItem", itemBusca)
                .setFirstResult(calculaPrimeiroRegistroPorPagina(pagina))
                .setMaxResults(MAXIMO_LANCAMENTOS).getResultList();
    }

    public List<Lancamento> buscaTodosOrderingByDataLancamento(){
        return entityManager.createNamedQuery("lancamento.byDataLancamento", Lancamento.class)
                .setMaxResults(MAXIMO_LANCAMENTOS).getResultList();
    }

    public BigDecimal calculaTotalGeralRenda() {
        return calculaTotalPorTipoLancamento(TipoLancamento.INCOME);
    }

    public BigDecimal calculaTotalGeralDespesa() {
        return calculaTotalPorTipoLancamento(TipoLancamento.EXPENSE);
    }

    protected BigDecimal calculaTotalPorTipoLancamento(TipoLancamento tipoLancamento) {
        return entityManager.createNamedQuery("lancamento.soma.por.tipolancamento", BigDecimal.class)
                .setParameter("tipoLancamento", tipoLancamento)
                .getSingleResult();
    }

    protected int calculaPrimeiroRegistroPorPagina(int pagina) {
        if (pagina == 0) {
            pagina++;
        }
        return (pagina - 1) * MAXIMO_LANCAMENTOS;
    }

    public int tamanhoPagina() {
        return MAXIMO_LANCAMENTOS;
    }

    public int calculaNumeroPaginas(int totalRegistros) {
        int numero = (totalRegistros / tamanhoPagina());
        if (totalRegistros > 0 && (totalRegistros % tamanhoPagina()) > 0){
                numero++;
        }
        return numero;
    }

    /**
     * Foi criado apenas para construir a paginacao com o Thymeleaf
     *
     * @param totalRegistros - total de registros a ser considerado
     * @return - número de páginas em cima do total de registros considerados
     */
    public List<Integer> getPaginas(int totalRegistros) {

        int numero = calculaNumeroPaginas(totalRegistros);
        List<Integer> paginas = new ArrayList<>(numero);

        for (int i = 1; i <= numero; i++) {
            paginas.add(i);
        }
        return paginas;
    }

    public long conta(String itemBusca, User loggedUser) {
        TypedQuery<Long> query = entityManager.createQuery(getCountSql(itemBusca), Long.class);
        query.setParameter("user", loggedUser);
        return query.getSingleResult();
    }

    public long contaCurrentMonth(String itemBusca, User loggedUser){
        String sql = getCountSql(itemBusca) + " and " + CURRENT_MONTH_CLAUSE;
        TypedQuery<Long> query = entityManager.createQuery(sql, Long.class);
        query.setParameter("user", loggedUser);
        return query.getSingleResult();
    }

    static String getCountSql(String itemBusca){
        String sql = SQL_COUNT_BASE;
        if (StringUtils.hasText(itemBusca)) {
            sql += SQL_COUNT_WHERE.replaceAll(":searchItem", getSqlIlikeClause(itemBusca));
        }else{
            sql += CURRENT_MONTH_CLAUSE;
        }
        return sql;
    }

    static String getSqlIlikeClause(String item){
        return "'%"+item+"%'";
    }

    public ResultRecord ajaxSearch(FormSearch formSearch) {
        final var itemBusca = formSearch.searchItem();
        final var page = formSearch.page() > 0 ? formSearch.page() : 1;
        final Optional<User> optUser = userRepository.findById(formSearch.userId());
        if (optUser.isEmpty()) {
            throw new IllegalStateException(
                    "Deveria ter o usuário pelo id: " + formSearch.userId()
            );
        }
        final User loggedUser = optUser.get();

        if(formSearch.searchOnlyCurrentMonth()){
            return getResultado(buscaTodosMesCorrente(page, itemBusca, loggedUser), contaCurrentMonth(itemBusca, loggedUser), itemBusca, page);
        }
        return getResultado(buscaTodosBySearching(page, itemBusca, loggedUser), conta(itemBusca, loggedUser), itemBusca, page);
    }

    ResultRecord getResultado(final List<Lancamento> resultado, long totalRegistros, String itemSearch, int page) {
        final List<EntryRecord> entries = new ArrayList<>(resultado.size());
        MoneyToStringConverter converter = new MoneyToStringConverter();
        DateFormat dateFormat = new SimpleDateFormat(Constantes.dd_MM_yyyy_SLASH);
        resultado.forEach(r -> {
                    String categoria = "";
                    if (Objects.nonNull(r.getCategory())) {
                        categoria = r.getCategory().getNome();
                    }
                    entries.add(
                            new EntryRecord(r.getId(),
                                    r.getDescricao(),
                                    converter.convert(r.getValor()),
                                    dateFormat.format(r.getDataLancamento()),
                                    r.getTipoLancamento().getTipo(),
                                    categoria,
                                    r.getUser().getId())

                    );
                }
        );
        //TODO: Check if these converters will be necessary in the future
        return new ResultRecord(converter.convert(getTotalDespesa(resultado)),
                converter.convert(getTotalRenda(resultado)),
                converter.convert(calculaTotalGeralDespesa()),
                converter.convert(calculaTotalGeralRenda()),
                entries,
                page,
                resultado.size(),
                totalRegistros,
                getPaginas(Math.toIntExact(totalRegistros)),
                itemSearch
                );
    }

    public Lancamento searchById(Long id) {
        Optional<Lancamento> opt = lancamentoRepository.findById(id);

        if (opt.isEmpty()) {
            throw new IllegalStateException(
                    "Deveria ter o Lancamento pelo id: " + id
            );
        }

        return opt.get();
    }


    public BigDecimal getTotalRenda(final List<Lancamento> lancamentos) {
        return somaValoresPorTipo(lancamentos, TipoLancamento.INCOME);
    }

    public BigDecimal getTotalDespesa(final List<Lancamento> lancamentos) {
        return somaValoresPorTipo(lancamentos, TipoLancamento.EXPENSE);
    }

    protected BigDecimal somaValoresPorTipo(List<Lancamento> lancamentos, TipoLancamento tipo) {
        return lancamentos.stream()
                .filter(l -> l.getTipoLancamento() == tipo && Objects.nonNull(l.getValor()))
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<TotalLancamentoRecord> getTotalPorPeriodo(Date dataInicial, Date dataFinal) {
        return entityManager.createNamedQuery("lancamento.totalLancamentosPorPeriodo", TotalLancamentoRecord.class)
                .setParameter("dataInicial", dataInicial)
                .setParameter("dataFinal", dataFinal)
                .getResultList();
    }

    public List<TotalLancamentoCategoriaRecord> getTotalPorPeriodoPorCategoria(Date dataInicial, Date dataFinal) {
        return entityManager.createNamedQuery("lancamento.totalLancamentosPorPeriodoPorCategoria", TotalLancamentoCategoriaRecord.class)
                .setParameter("dataInicial", dataInicial)
                .setParameter("dataFinal", dataFinal)
                .getResultList();
    }

    @Transactional
    public void removeAllByUser(long userId){
        lancamentoRepository.removeAllByUser(userId);
    }
}
