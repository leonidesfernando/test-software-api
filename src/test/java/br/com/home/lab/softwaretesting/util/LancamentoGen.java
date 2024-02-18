package br.com.home.lab.softwaretesting.util;

import br.com.home.lab.softwaretesting.model.Categoria;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.TipoLancamento;

import java.math.BigDecimal;
import java.util.Date;

public class LancamentoGen {

    public static Lancamento novaRenda(){
        return novoLancamento(TipoLancamento.INCOME);
    }

    public static Lancamento novaDespesa(){
        return novoLancamento(TipoLancamento.EXPENSE);
    }

    protected static Lancamento novoLancamento(TipoLancamento tipoLancamento){
        Categoria[] categorias = Categoria.values();
        int indice = DataGen.number(0, categorias.length-1);
        return new LancamentoBuilder()
                .comValor(DataGen.moneyValue())
                .comTipo(tipoLancamento)
                .comDescricao(DataGen.productName())
                .comDataLancamento(DataGen.date())
                .comCategoria(categorias[indice])
                .build();
    }

    public static LancamentoBuilder builder(){
        return new LancamentoBuilder();
    }

    public static class LancamentoBuilder{
        private final Lancamento lancamento;

        LancamentoBuilder(){
            lancamento = new Lancamento();
        }

        public LancamentoBuilder comDataLancamento(Date date) {
            lancamento.setDataLancamento(date);
            return this;
        }

        public LancamentoBuilder comDescricao(String descricao){
            lancamento.setDescricao(descricao);
            return this;
        }

        public LancamentoBuilder comValor(double valor){
            lancamento.setValor(BigDecimal.valueOf(valor));
            return this;
        }
        public LancamentoBuilder comTipo(TipoLancamento tipo){
            lancamento.setTipoLancamento(tipo);
            return this;
        }
        public Lancamento build(){
            return lancamento;
        }

        public LancamentoBuilder comCategoria(Categoria categoria) {
            lancamento.setCategoria(categoria);
            return this;
        }
    }
}
