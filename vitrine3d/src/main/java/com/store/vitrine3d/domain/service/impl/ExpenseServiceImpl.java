package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.*;
import com.store.vitrine3d.domain.repository.ExpenseBatchRepository;
import com.store.vitrine3d.domain.repository.ExpenseProductRepository;
import com.store.vitrine3d.domain.repository.PdvEmployeeRepository;
import com.store.vitrine3d.domain.repository.RecurringExpenseRepository;
import com.store.vitrine3d.domain.service.ExpenseService;
import com.store.vitrine3d.domain.service.PdvCashFlowService;
import com.store.vitrine3d.rest.dto.ExpenseBatchItemRequest;
import com.store.vitrine3d.rest.dto.ExpenseBatchRequest;
import com.store.vitrine3d.rest.dto.PdvCashFlowRequest;
import com.store.vitrine3d.rest.dto.RecurringExpenseRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseProductRepository productRepository;
    private final ExpenseBatchRepository batchRepository;
    private final RecurringExpenseRepository recurringRepository;
    private final PdvCashFlowService cashFlowService;
    private final PdvEmployeeRepository employeeRepository;

    public ExpenseServiceImpl(ExpenseProductRepository productRepository,
                               ExpenseBatchRepository batchRepository,
                               RecurringExpenseRepository recurringRepository,
                               PdvCashFlowService cashFlowService,
                               PdvEmployeeRepository employeeRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.recurringRepository = recurringRepository;
        this.cashFlowService = cashFlowService;
        this.employeeRepository = employeeRepository;
    }

    // ── Catálogo + estoque ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseProduct> listProducts(UUID storeId, String query) {
        if (query == null || query.isBlank()) {
            return productRepository.findByStoreIdOrderByNameAsc(storeId);
        }
        return productRepository.findByStoreIdAndNameContainingIgnoreCaseOrderByNameAsc(storeId, query);
    }

    @Override
    public ExpenseProduct findOrCreateProduct(Store store, String name, String unit) {
        return productRepository.findByStoreIdAndNameIgnoreCase(store.getId(), name)
                .orElseGet(() -> {
                    ExpenseProduct product = new ExpenseProduct();
                    product.setStore(store);
                    product.setName(name);
                    product.setUnit(unit);
                    product.setStockQuantity(0);
                    return productRepository.save(product);
                });
    }

    @Override
    public ExpenseProduct adjustStock(UUID storeId, Long productId, int quantityDelta) {
        ExpenseProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!product.getStore().getId().equals(storeId)) {
            throw new ResourceNotFoundException("Produto não encontrado");
        }

        int newStock = Math.max(0, product.getStockQuantity() + quantityDelta);
        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseProduct> getLowStockAlerts(UUID storeId) {
        return productRepository.findByStoreIdAndStockQuantityLessThanEqualOrderByNameAsc(storeId, 1);
    }

    // ── Batches unitários ────────────────────────────────────────────────────

    @Override
    public ExpenseBatch createBatch(Store store, ExpenseBatchRequest req) {
        if (isBatchDuplicate(store.getId(), req.getOfflineId())) {
            throw new BusinessRuleException("DUPLICATE_EXPENSE_BATCH",
                    "Batch já registrado com este offlineId");
        }

        ExpenseBatch batch = new ExpenseBatch();
        batch.setStore(store);
        batch.setOfflineId(req.getOfflineId());
        batch.setBatchDate(req.getBatchDate());
        batch.setNote(req.getNote());

        if (req.getOperatorId() != null) {
            PdvEmployee operator = employeeRepository.findById(req.getOperatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Operador não encontrado"));
            batch.setOperator(operator);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ExpenseBatchItemRequest itemReq : req.getItems()) {
            ExpenseProduct product = findOrCreateProduct(store, itemReq.getProductName(), itemReq.getUnit());

            product.setStockQuantity(product.getStockQuantity() + itemReq.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = itemReq.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            ExpenseBatchItem item = new ExpenseBatchItem();
            item.setBatch(batch);
            item.setExpenseProductId(product.getId());
            item.setProductName(itemReq.getProductName());
            item.setUnit(itemReq.getUnit());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setQuantity(itemReq.getQuantity());
            item.setSubtotal(subtotal);
            batch.getItems().add(item);

            totalAmount = totalAmount.add(subtotal);
        }

        batch.setTotalAmount(totalAmount);
        ExpenseBatch saved = batchRepository.save(batch);

        PdvCashFlowRequest flowReq = new PdvCashFlowRequest();
        flowReq.setOfflineId(UUID.randomUUID().toString());
        flowReq.setType(FlowType.OUT);
        flowReq.setCategory(FlowCategory.EXPENSE);
        flowReq.setAmount(totalAmount);
        flowReq.setDescription("Gastos operacionais" +
                (batch.getNote() != null ? " - " + batch.getNote() : ""));
        flowReq.setFlowDate(batch.getBatchDate());
        cashFlowService.addEntry(store, flowReq);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBatchDuplicate(UUID storeId, String offlineId) {
        return batchRepository.existsByStoreIdAndOfflineId(storeId, offlineId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseBatch> listBatches(UUID storeId, Instant from, Instant to, int page, int size) {
        return batchRepository.findByStoreIdAndBatchDateBetweenOrderByBatchDateDesc(
                storeId, from, to, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseBatch getBatch(UUID storeId, UUID batchId) {
        return batchRepository.findByIdAndStoreId(batchId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch não encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumBatchesByPeriod(UUID storeId, Instant from, Instant to) {
        return batchRepository.sumTotalByPeriod(storeId, from, to);
    }

    // ── Recorrentes ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RecurringExpense> listRecurring(UUID storeId) {
        return recurringRepository.findByStoreIdAndIsActiveTrueOrderByNameAsc(storeId);
    }

    @Override
    public RecurringExpense createRecurring(Store store, RecurringExpenseRequest req) {
        RecurringExpense expense = new RecurringExpense();
        expense.setStore(store);
        expense.setName(req.getName());
        expense.setDescription(req.getDescription());
        expense.setAmount(req.getAmount());
        expense.setFrequency(req.getFrequency());
        expense.setDueDay(req.getDueDay());
        expense.setAlertDaysBefore(req.getAlertDaysBefore());
        expense.setNextDueDate(req.getNextDueDate());
        expense.setActive(true);
        return recurringRepository.save(expense);
    }

    @Override
    public RecurringExpense updateRecurring(UUID storeId, UUID id, RecurringExpenseRequest req) {
        RecurringExpense expense = recurringRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recorrente não encontrada"));

        expense.setName(req.getName());
        expense.setDescription(req.getDescription());
        expense.setAmount(req.getAmount());
        expense.setFrequency(req.getFrequency());
        expense.setDueDay(req.getDueDay());
        expense.setAlertDaysBefore(req.getAlertDaysBefore());
        expense.setNextDueDate(req.getNextDueDate());
        return recurringRepository.save(expense);
    }

    @Override
    public void deactivateRecurring(UUID storeId, UUID id) {
        RecurringExpense expense = recurringRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recorrente não encontrada"));
        expense.setActive(false);
        recurringRepository.save(expense);
    }

    @Override
    public RecurringExpense payRecurring(UUID storeId, UUID id) {
        RecurringExpense expense = recurringRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recorrente não encontrada"));

        LocalDate today = LocalDate.now();
        expense.setLastPaidDate(today);
        expense.setNextDueDate(expense.getFrequency().nextDate(today, expense.getDueDay()));
        RecurringExpense saved = recurringRepository.save(expense);

        PdvCashFlowRequest flowReq = new PdvCashFlowRequest();
        flowReq.setOfflineId(UUID.randomUUID().toString());
        flowReq.setType(FlowType.OUT);
        flowReq.setCategory(FlowCategory.RECURRING_EXPENSE);
        flowReq.setRecurringExpenseId(expense.getId());
        flowReq.setAmount(expense.getAmount());
        flowReq.setDescription(expense.getName());
        flowReq.setFlowDate(Instant.now());
        cashFlowService.addEntry(expense.getStore(), flowReq);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringExpense> getAlerts(UUID storeId) {
        LocalDate today = LocalDate.now();
        return recurringRepository.findByStoreIdAndIsActiveTrueOrderByNameAsc(storeId)
                .stream()
                .filter(e -> e.getNextDueDate().isBefore(today.plusDays(e.getAlertDaysBefore() + 1)))
                .toList();
    }
}
