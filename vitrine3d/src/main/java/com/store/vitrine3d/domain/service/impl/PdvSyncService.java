package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.service.ExpenseService;
import com.store.vitrine3d.domain.service.PdvCashFlowService;
import com.store.vitrine3d.domain.service.PdvSaleService;
import com.store.vitrine3d.rest.dto.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PdvSyncService {

    private final PdvSaleService saleService;
    private final PdvCashFlowService cashFlowService;
    private final ExpenseService expenseService;

    public PdvSyncService(PdvSaleService saleService,
                          PdvCashFlowService cashFlowService,
                          ExpenseService expenseService) {
        this.saleService = saleService;
        this.cashFlowService = cashFlowService;
        this.expenseService = expenseService;
    }

    public PdvSyncResponse sync(Store store, PdvSyncRequest req) {
        int salesProcessed = 0, salesSkipped = 0;
        int flowsProcessed = 0, flowsSkipped = 0;
        int expensesProcessed = 0, expensesSkipped = 0;
        List<String> errors = new ArrayList<>();

        for (PdvSaleRequest saleReq : req.getSales()) {
            try {
                if (saleService.isDuplicate(store.getId(), saleReq.getOfflineId())) {
                    salesSkipped++;
                } else {
                    saleService.createSale(store, saleReq);
                    salesProcessed++;
                }
            } catch (Exception e) {
                errors.add("Venda " + saleReq.getOfflineId() + ": " + e.getMessage());
            }
        }

        for (PdvCashFlowRequest flowReq : req.getCashFlows()) {
            try {
                if (cashFlowService.isDuplicate(store.getId(), flowReq.getOfflineId())) {
                    flowsSkipped++;
                } else {
                    cashFlowService.addEntry(store, flowReq);
                    flowsProcessed++;
                }
            } catch (Exception e) {
                errors.add("Caixa " + flowReq.getOfflineId() + ": " + e.getMessage());
            }
        }

        for (ExpenseBatchRequest batchReq : req.getExpenseBatches()) {
            try {
                if (expenseService.isBatchDuplicate(store.getId(), batchReq.getOfflineId())) {
                    expensesSkipped++;
                } else {
                    expenseService.createBatch(store, batchReq);
                    expensesProcessed++;
                }
            } catch (Exception e) {
                errors.add("Gasto " + batchReq.getOfflineId() + ": " + e.getMessage());
            }
        }

        return new PdvSyncResponse(salesProcessed, salesSkipped,
                flowsProcessed, flowsSkipped,
                expensesProcessed, expensesSkipped,
                errors);
    }
}
