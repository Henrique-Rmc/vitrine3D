package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.ExpenseBatch;
import com.store.vitrine3d.domain.model.ExpenseProduct;
import com.store.vitrine3d.domain.model.RecurringExpense;
import com.store.vitrine3d.domain.model.RecurringExpensePayment;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.MeasurementUnit;
import com.store.vitrine3d.rest.dto.ExpenseBatchRequest;
import com.store.vitrine3d.rest.dto.ExpenseProductRequest;
import com.store.vitrine3d.rest.dto.RecurringExpensePaymentRequest;
import com.store.vitrine3d.rest.dto.RecurringExpenseRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ExpenseService {

    // Catálogo + estoque de insumos
    List<ExpenseProduct> listProducts(UUID storeId, String query);
    ExpenseProduct findOrCreateProduct(Store store, String name, String unit, MeasurementUnit stockUnit);
    ExpenseProduct saveProduct(Store store, ExpenseProductRequest req);
    ExpenseProduct adjustStock(UUID storeId, Long productId, BigDecimal quantityDelta, MeasurementUnit unit);
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
    List<RecurringExpense> getAlerts(UUID storeId);

    // Pagamentos das contas fixas (histórico + comprovantes)
    RecurringExpensePayment payRecurring(UUID storeId, UUID id, RecurringExpensePaymentRequest req);
    List<RecurringExpensePayment> listPayments(UUID storeId, UUID recurringExpenseId);
    RecurringExpensePayment addPaymentImages(UUID storeId, UUID paymentId, List<MultipartFile> images);
    RecurringExpensePayment removePaymentImage(UUID storeId, UUID paymentId, String imageUrl);
}
