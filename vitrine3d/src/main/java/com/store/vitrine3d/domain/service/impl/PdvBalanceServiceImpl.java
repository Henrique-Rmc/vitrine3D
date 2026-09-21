package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.FlowCategory;
import com.store.vitrine3d.domain.model.FlowType;
import com.store.vitrine3d.domain.repository.PdvCashFlowRepository;
import com.store.vitrine3d.domain.repository.PdvCustomerCreditRepository;
import com.store.vitrine3d.domain.repository.PdvSaleRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.service.ExpenseService;
import com.store.vitrine3d.domain.service.PdvBalanceService;
import com.store.vitrine3d.rest.dto.PdvBalanceResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PdvBalanceServiceImpl implements PdvBalanceService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final PdvSaleRepository saleRepository;
    private final PdvCashFlowRepository cashFlowRepository;
    private final PdvCustomerCreditRepository creditRepository;
    private final ProductRepository productRepository;
    private final ExpenseService expenseService;

    public PdvBalanceServiceImpl(PdvSaleRepository saleRepository,
                                  PdvCashFlowRepository cashFlowRepository,
                                  PdvCustomerCreditRepository creditRepository,
                                  ProductRepository productRepository,
                                  ExpenseService expenseService) {
        this.saleRepository = saleRepository;
        this.cashFlowRepository = cashFlowRepository;
        this.creditRepository = creditRepository;
        this.productRepository = productRepository;
        this.expenseService = expenseService;
    }

    @Override
    public PdvBalanceResponse getBalance(UUID storeId, Instant from, Instant to) {
        BigDecimal receitaBruta    = saleRepository.sumGrossRevenueByPeriod(storeId, from, to);
        BigDecimal receitaLiquida  = saleRepository.sumNetRevenueByPeriod(storeId, from, to);
        long       numeroVendas    = saleRepository.countSalesByPeriod(storeId, from, to);
        BigDecimal cmvParcial      = saleRepository.sumCogsByPeriod(storeId, from, to);
        BigDecimal subtotalComCusto = saleRepository.sumSubtotalWithCostByPeriod(storeId, from, to);
        BigDecimal subtotalTotal   = saleRepository.sumSubtotalAllByPeriod(storeId, from, to);

        // Insumos vêm da tabela de batches e contas fixas do fluxo de caixa por categoria.
        // A DRE nunca lê OUT/EXPENSE direto, senão os insumos seriam contados duas vezes.
        BigDecimal despesasInsumos = expenseService.sumBatchesByPeriod(storeId, from, to);
        BigDecimal despesasFixas   = cashFlowRepository.sumAmountByTypeCategoryAndPeriod(
                storeId, FlowType.OUT, FlowCategory.RECURRING_EXPENSE, from, to);
        BigDecimal retiradas       = cashFlowRepository.sumAmountByTypeCategoryAndPeriod(
                storeId, FlowType.OUT, FlowCategory.WITHDRAWAL, from, to);

        BigDecimal totalIn  = cashFlowRepository.sumAmountByTypeAndPeriod(storeId, FlowType.IN, from, to);
        BigDecimal totalOut = cashFlowRepository.sumAmountByTypeAndPeriod(storeId, FlowType.OUT, from, to);

        // Cobertura do CMV: um custo parcial infla a margem silenciosamente, então
        // abaixo de 100% o CMV e as margens viram null em vez de um número enganoso.
        BigDecimal cobertura = subtotalTotal.compareTo(BigDecimal.ZERO) > 0
                ? subtotalComCusto.divide(subtotalTotal, 4, RoundingMode.HALF_UP)
                : BigDecimal.ONE;
        boolean custoCompleto = cobertura.compareTo(BigDecimal.ONE) >= 0;

        BigDecimal despesasTotal = despesasInsumos.add(despesasFixas).add(retiradas);

        BigDecimal cmv = null, lucroBruto = null, lucroLiquido = null;
        if (custoCompleto) {
            cmv          = cmvParcial;
            lucroBruto   = receitaLiquida.subtract(cmv);
            lucroLiquido = lucroBruto.subtract(despesasTotal);
        }

        PdvBalanceResponse.Periodo periodo = new PdvBalanceResponse.Periodo();
        periodo.setFrom(from);
        periodo.setTo(to);

        PdvBalanceResponse.Dre dre = new PdvBalanceResponse.Dre();
        dre.setReceitaBruta(receitaBruta);
        dre.setDescontos(receitaBruta.subtract(receitaLiquida));
        dre.setReceitaLiquida(receitaLiquida);
        dre.setCmv(cmv);
        dre.setLucroBruto(lucroBruto);
        dre.setDespesasInsumos(despesasInsumos);
        dre.setDespesasFixas(despesasFixas);
        dre.setRetiradas(retiradas);
        dre.setLucroLiquido(lucroLiquido);

        PdvBalanceResponse.Indicadores indicadores = new PdvBalanceResponse.Indicadores();
        indicadores.setNumeroVendas(numeroVendas);
        indicadores.setMargemBruta(percentOf(lucroBruto, receitaLiquida));
        indicadores.setMargemLiquida(percentOf(lucroLiquido, receitaLiquida));
        indicadores.setTicketMedio(numeroVendas > 0
                ? receitaLiquida.divide(BigDecimal.valueOf(numeroVendas), 2, RoundingMode.HALF_UP)
                : null);

        PdvBalanceResponse.Posicao posicao = new PdvBalanceResponse.Posicao();
        posicao.setSaldoEmCaixa(totalIn.subtract(totalOut));
        posicao.setContasAReceber(creditRepository.sumOpenBalanceByStore(storeId));
        posicao.setEstoqueACusto(productRepository.sumStockValueAtCost(storeId));

        PdvBalanceResponse.Qualidade qualidade = new PdvBalanceResponse.Qualidade();
        qualidade.setCmvCoverage(cobertura);
        qualidade.setEstimado(!custoCompleto);

        PdvBalanceResponse response = new PdvBalanceResponse();
        response.setPeriodo(periodo);
        response.setBasis("ACCRUAL");
        response.setDre(dre);
        response.setIndicadores(indicadores);
        response.setPosicao(posicao);
        response.setQualidade(qualidade);
        return response;
    }

    private BigDecimal percentOf(BigDecimal value, BigDecimal base) {
        if (value == null || base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return value.multiply(HUNDRED).divide(base, 2, RoundingMode.HALF_UP);
    }
}
