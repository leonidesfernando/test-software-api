package br.com.home.lab.softwaretesting.model;

import br.com.home.lab.softwaretesting.converter.MoneyDeserialize;
import br.com.home.lab.softwaretesting.util.Constantes;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@NamedNativeQueries( value = {
    @NamedNativeQuery(name = "lancamento.maisRecentesBySearching", query = Lancamento.LANCAMENTO_MAIS_RECENTES_BY_SEARCHING, resultClass = Lancamento.class)
})

@NamedQueries(value = {
        @NamedQuery(name = "lancamento.maisRecentes", query = "select l from Lancamento l where " + Lancamento.CURRENT_MONTH_CLAUSE + " order by l.dataLancamento"),
        @NamedQuery(name = "lancamento.byDataLancamento", query = "select l from Lancamento l order by l.dataLancamento"),


        @NamedQuery(name = "lancamento.BySearching", query = "select l from Lancamento l where " + Lancamento.SEARCH_BY_DESCRIPTION_OR_CATEGORY_ENTRY_TYPE + " order by l.dataLancamento"),

        @NamedQuery(name = "lancamento.totalLancamentosPorPeriodo",
                query = "select new br.com.home.lab.softwaretesting.controller.record.TotalLancamentoRecord(sum(l.valor), l.tipoLancamento) " +
                        " from Lancamento l where l.dataLancamento between :dataInicial and :dataFinal group by l.tipoLancamento"),
        @NamedQuery(name = "lancamento.totalLancamentosPorPeriodoPorCategoria",
                query = "select new br.com.home.lab.softwaretesting.controller.record.TotalLancamentoCategoriaRecord(sum(l.valor), l.tipoLancamento, l.category) " +
                        " from Lancamento l where l.dataLancamento between :dataInicial and :dataFinal group by l.tipoLancamento, l.category order by l.tipoLancamento"),
        @NamedQuery(name = "lancamento.soma.por.tipolancamento", query = "select sum(l.valor) from Lancamento l where l.tipoLancamento = :tipoLancamento")
})

@Entity
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@AllArgsConstructor
public class Lancamento {

    public static final String LANCAMENTO_MAIS_RECENTES_BY_SEARCHING = "SELECT * FROM Lancamento l WHERE EXTRACT(YEAR FROM l.data_Lancamento) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "AND EXTRACT(MONTH FROM l.data_Lancamento) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND (UPPER(l.descricao) LIKE UPPER(:searchItem) OR " +
            "UPPER(l.tipo_Lancamento) LIKE UPPER(:searchItem) OR " +
            "UPPER(l.category) LIKE UPPER(:searchItem)) " +
            "ORDER BY l.data_Lancamento";

    public static final String CURRENT_MONTH_CLAUSE = "year(l.dataLancamento) = year(current_date) and " +
            " month(l.dataLancamento) = month(current_date)";

    public static final String SEARCH_BY_DESCRIPTION_OR_CATEGORY_ENTRY_TYPE  = "(upper(l.descricao) like upper( :searchItem)) or " +
            " (upper(l.tipoLancamento) like upper( :searchItem)) or " +
            " (upper(l.category) like upper( :searchItem)) ";

    @Id
    @GeneratedValue
    @Getter @Setter
    private long id;

    @NotBlank(message = "A descrição deve ser informada")
    @Getter
    @Setter
    private String descricao;

    @JsonDeserialize(using = MoneyDeserialize.class)
    @NotNull(message = "O valor deve ser informado")
    @Min(message = "O valor deve ser maior que zero", value = 0)
    @Getter
    @Setter
    private BigDecimal valor;

    @JsonFormat(pattern = Constantes.dd_MM_yyyy_SLASH)
    @NotNull(message = "A data deve ser informada")
    @Getter
    @Setter
    private Date dataLancamento;

    @NotNull(message = "O tipo de lancamento deve ser informado")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private TipoLancamento tipoLancamento = TipoLancamento.EXPENSE;

    @NotNull(message = "A categoria deve ser informada")
    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private Category category;
}
