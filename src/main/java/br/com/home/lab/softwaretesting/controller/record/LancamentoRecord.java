package br.com.home.lab.softwaretesting.controller.record;

import br.com.home.lab.softwaretesting.converter.StringToDateConverter;
import br.com.home.lab.softwaretesting.converter.StringToMoneyConverter;
import br.com.home.lab.softwaretesting.model.Categoria;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

public record LancamentoRecord(
        long id,
        String descricao,
        String valor,
        String dataLancamento,
        String tipoLancamento,
        String categoria
) {

    public Lancamento build(){
        return new Lancamento(id, descricao,
                getValor(),
                getDataLancamento(),
                getTipoLancamento(),
                getCategoria()
        );
    }

    private BigDecimal getValor() {
        return StringUtils.hasText(valor) ? new StringToMoneyConverter().convert(valor) : null;
    }

    private Date getDataLancamento() {
        return StringUtils.hasText(dataLancamento) ? new StringToDateConverter().convert(dataLancamento) : null;
    }

    private Categoria getCategoria() {
        return StringUtils.hasText(categoria) ? Categoria.valueOf(categoria) : null;
    }

    private TipoLancamento getTipoLancamento() {
        return StringUtils.hasText(tipoLancamento) ? TipoLancamento.valueOf(tipoLancamento) : null;
    }
}
