package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.ExpenseBatch;
import com.store.vitrine3d.domain.model.ExpenseProduct;
import com.store.vitrine3d.domain.model.RecurringExpense;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.rest.dto.ExpenseBatchRequest;
import com.store.vitrine3d.rest.dto.RecurringExpenseRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ExpenseService {

    // Catálogo + estoque de insumos
    List<ExpenseProduct> listProducts(UUID storeId, String query);
    ExpenseProduct findOrCreateProduct(Store store, String name, String unit);
    ExpenseProduct adjustStock(UUID storeId, Long productId, int quantityDelta);
    List<ExpenseProduct> getLowStockAlerts(UUID storeId);

    // Batches unitários
    ExpenseBatch createBatch(Store store, ExpenseBatchRequest req);
    boolean isBatchDuplicate(UUID storeId, String offlineId);
    Page<ExpenseBatch> listBatches(UUID storeId, Instant from, Instant to, int page, int size);
    ExpenseBatch getBatch(UUID storeId, UUID batchId);
    BigDecimal sumBatchesByPeriod(UUID storeId, Instant from, Instant to);

    // Recorrentes
    List<RecurringExpense> listRecurring(UUID storeId);
    RecurringExpense createRecurring(Store store, RecurringExpenseRequest req);
    RecurringExpense updateRecurring(UUID storeId, UUID id, RecurringExpenseRequest req);
    void deactivateRecurring(UUID storeId, UUID id);
    RecurringExpense payRecurring(UUID storeId, UUID id);
    List<RecurringExpense> getAlerts(UUID storeId);
}
