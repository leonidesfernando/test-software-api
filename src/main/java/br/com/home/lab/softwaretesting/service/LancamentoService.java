package br.com.home.lab.softwaretesting.service;

import br.com.home.lab.softwaretesting.controller.record.*;
import br.com.home.lab.softwaretesting.converter.MoneyToStringConverter;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import br.com.home.lab.softwaretesting.repository.LancamentoRepository;
import br.com.home.lab.softwaretesting.util.Constantes;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static br.com.home.lab.softwaretesting.model.Lancamento.CURRENT_MONTH_CLAUSE;

@Service
@AllArgsConstructor
public class LancamentoService {

    static final String SQL_COUNT_BASE = "select count(*) from Lancamento l where  ";
    static final String SQL_COUNT_WHERE = " (upper(l.descricao) like upper( :itemBusca)) " +
            "  or (upper(l.tipoLancamento) like upper( :itemBusca)) " +
            " or (upper(l.categoria) like upper( :itemBusca))";

    @PersistenceContext
    private EntityManager entityManager;
    static final int MAXIMO_LANCAMENTOS = 10;
    private LancamentoRepository lancamentoRepository;


    @Transactional
    public Lancamento salvar(Lancamento lancamento) {
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

    public List<Lancamento> buscaTodosMesCorrente(int pagina, String itemBusca){

        return entityManager.createNamedQuery("lancamento.maisRecentesBySearching", Lancamento.class)
                .setParameter("itemBusca", "%" + itemBusca + "%")
                .setFirstResult(calculaPrimeiroRegistroPorPagina(pagina))
                .setMaxResults(MAXIMO_LANCAMENTOS).getResultList();
    }
    public List<Lancamento> buscaTodosBySearching(int pagina, String itemBusca){
        return entityManager.createNamedQuery("lancamento.BySearching", Lancamento.class)
                .setParameter("itemBusca", "%" + itemBusca + "%")
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

    public long conta(String itemBusca) {
        TypedQuery<Long> query = entityManager.createQuery(getCountSql(itemBusca), Long.class);
        return query.getSingleResult();
    }

    public long contaCurrentMonth(String itemBusca){
        String sql = getCountSql(itemBusca) + " and " + CURRENT_MONTH_CLAUSE;
        TypedQuery<Long> query = entityManager.createQuery(sql, Long.class);
        return query.getSingleResult();
    }

    static String getCountSql(String itemBusca){
        String sql = SQL_COUNT_BASE;
        if (StringUtils.hasText(itemBusca)) {
            sql += SQL_COUNT_WHERE.replaceAll(":itemBusca", getSqlIlikeClause(itemBusca));
        }else{
            sql += CURRENT_MONTH_CLAUSE;
        }
        return sql;
    }

    static String getSqlIlikeClause(String item){
        return "'%"+item+"%'";
    }

    public ResultadoRecord buscaAjax(BuscaForm buscaForm) {
        final var itemBusca = buscaForm.itemBusca();
        return getResultado(buscaForm.searchOnlyCurrentMonth()  ?
                        buscaTodosMesCorrente(1, itemBusca) :
                        buscaTodosBySearching(1, itemBusca),
                conta(itemBusca), itemBusca);
    }

    ResultadoRecord getResultado(final List<Lancamento> resultado, long totalRegistros, String itemBusca) {
        final List<LancamentoRecord> lancamentos = new ArrayList<>(resultado.size());
        MoneyToStringConverter converter = new MoneyToStringConverter();
        DateFormat dateFormat = new SimpleDateFormat(Constantes.dd_MM_yyyy_SLASH);
        resultado.forEach(r -> {
                    String categoria = "";
                    if (Objects.nonNull(r.getCategory())) {
                        categoria = r.getCategory().getNome();
                    }
                    lancamentos.add(
                            new LancamentoRecord(r.getId(),
                                    r.getDescricao(),
                                    converter.convert(r.getValor()),
                                    dateFormat.format(r.getDataLancamento()),
                                    r.getTipoLancamento().getTipo(),
                                    categoria)

                    );
                }
        );
        //TODO: Check if these converters will be necessary in the future
        return new ResultadoRecord(converter.convert(getTotalDespesa(resultado)),
                converter.convert(getTotalRenda(resultado)),
                converter.convert(calculaTotalGeralDespesa()),
                converter.convert(calculaTotalGeralRenda()),
                lancamentos,
                1,
                resultado.size(),
                totalRegistros,
                itemBusca
                );
    }

    public Lancamento buscaPorId(Long id) {
        Optional<Lancamento> opt = lancamentoRepository.findById(id);
        return opt.orElseThrow(() -> new IllegalStateException("Deveria ter o Lancamento pelo id: " + id));
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
    public void truncateTable(){
        lancamentoRepository.truncateTable();
    }
}
