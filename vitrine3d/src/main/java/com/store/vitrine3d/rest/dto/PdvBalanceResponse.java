package com.store.vitrine3d.rest.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Balanço do período — DRE em cascata + posição patrimonial.
 *
 * Regime de competência: a venda é reconhecida na data em que ocorreu, mesmo com saldo
 * devedor. É o único regime em que o CMV casa com a receita que o gerou, mantendo a
 * margem correta. O dinheiro efetivamente recebido aparece em posicao.saldoEmCaixa.
 */
@Data
public class PdvBalanceResponse {

    private Periodo periodo;
    /** Sempre "ACCRUAL" — deixa explícito para o frontend que receita != caixa. */
    private String basis;
    private Dre dre;
    private Indicadores indicadores;
    private Posicao posicao;
    private Qualidade qualidade;

    @Data
    public static class Periodo {
        private Instant from;
        private Instant to;
    }

    @Data
    public static class Dre {
        private BigDecimal receitaBruta;
        private BigDecimal descontos;
        private BigDecimal receitaLiquida;
        /** null quando nem todos os itens têm custo conhecido — ver qualidade.estimado. */
        private BigDecimal cmv;
        /** null quando cmv é null. */
        private BigDecimal lucroBruto;
        private BigDecimal despesasInsumos;
        private BigDecimal despesasFixas;
        private BigDecimal retiradas;
        /** null quando cmv é null. */
        private BigDecimal lucroLiquido;
    }

    @Data
    public static class Indicadores {
        /** Percentual (0-100). null quando o CMV é desconhecido ou não houve receita. */
        private BigDecimal margemBruta;
        private BigDecimal margemLiquida;
        /** null quando não houve vendas no período. */
        private BigDecimal ticketMedio;
        private long numeroVendas;
    }

    @Data
    public static class Posicao {
        /** Entradas menos saídas de caixa no período — mesma fonte de /cash-flow/summary. */
        private BigDecimal saldoEmCaixa;
        private BigDecimal contasAReceber;
        private BigDecimal estoqueACusto;
    }

    @Data
    public static class Qualidade {
        /** Fração da receita cujo item tem custo conhecido (0.0 a 1.0). */
        private BigDecimal cmvCoverage;
        /** true quando cmvCoverage < 1: CMV e margens vêm null em vez de um número enganoso. */
        private boolean estimado;
    }
}
