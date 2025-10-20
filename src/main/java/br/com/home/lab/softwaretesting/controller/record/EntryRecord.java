package br.com.home.lab.softwaretesting.controller.record;

import br.com.home.lab.softwaretesting.converter.StringToDateConverter;
import br.com.home.lab.softwaretesting.converter.StringToMoneyConverter;
import br.com.home.lab.softwaretesting.model.Category;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.TipoLancamento;
import br.com.home.lab.softwaretesting.model.User;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

public record EntryRecord(
        long id,
        String description,
        String amount,
        String entryDate,
        String entryType,
        String category,
        long userId
) {

    public Lancamento build(){
        return new Lancamento(id, description,
                getAmount(),
                getEntryDate(),
                getEntryType(),
                getCategory(),
                new User(userId())
        );
    }

    private BigDecimal getAmount() {
        return StringUtils.hasText(amount) ? new StringToMoneyConverter().convert(amount) : null;
    }

    private Date getEntryDate() {
        return StringUtils.hasText(entryDate) ? new StringToDateConverter().convert(entryDate) : null;
    }

    private Category getCategory() {
        return StringUtils.hasText(category) ? Category.valueOf(category) : null;
    }

    private TipoLancamento getEntryType() {
        return StringUtils.hasText(entryType) ? TipoLancamento.valueOf(entryType) : null;
    }
}
