package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.*;
import com.store.vitrine3d.domain.repository.ExpenseBatchRepository;
import com.store.vitrine3d.domain.repository.ExpenseProductRepository;
import com.store.vitrine3d.domain.repository.PdvEmployeeRepository;
import com.store.vitrine3d.domain.repository.RecurringExpensePaymentRepository;
import com.store.vitrine3d.domain.repository.RecurringExpenseRepository;
import com.store.vitrine3d.domain.service.ExpenseService;
import com.store.vitrine3d.domain.service.PdvCashFlowService;
import com.store.vitrine3d.infrastructure.storage.StorageService;
import com.store.vitrine3d.rest.dto.RecurringExpensePaymentRequest;
import org.springframework.web.multipart.MultipartFile;
import com.store.vitrine3d.rest.dto.ExpenseBatchItemRequest;
import com.store.vitrine3d.rest.dto.ExpenseBatchRequest;
import com.store.vitrine3d.rest.dto.ExpenseProductRequest;
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
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseProductRepository productRepository;
    private final ExpenseBatchRepository batchRepository;
    private final RecurringExpenseRepository recurringRepository;
    private final RecurringExpensePaymentRepository paymentRepository;
    private final PdvCashFlowService cashFlowService;
    private final PdvEmployeeRepository employeeRepository;
    private final StorageService storageService;

    public ExpenseServiceImpl(ExpenseProductRepository productRepository,
                               ExpenseBatchRepository batchRepository,
                               RecurringExpenseRepository recurringRepository,
                               RecurringExpensePaymentRepository paymentRepository,
                               PdvCashFlowService cashFlowService,
                               PdvEmployeeRepository employeeRepository,
                               StorageService storageService) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
        this.recurringRepository = recurringRepository;
        this.paymentRepository = paymentRepository;
        this.cashFlowService = cashFlowService;
        this.employeeRepository = employeeRepository;
        this.storageService = storageService;
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
    public ExpenseProduct findOrCreateProduct(Store store, String name, String unit, MeasurementUnit stockUnit) {
        return productRepository.findByStoreIdAndNameIgnoreCase(store.getId(), name)
                .orElseGet(() -> {
                    ExpenseProduct product = new ExpenseProduct();
                    product.setStore(store);
                    product.setName(name);
                    product.setUnit(unit);
                    product.setStockUnit(stockUnit != null ? stockUnit : MeasurementUnit.UN);
                    product.setStockQuantity(BigDecimal.ZERO);
                    // lowStockAlert fica nulo: material criado automaticamente por uma compra
                    // não tem como saber o limite de reposição.
                    return productRepository.save(product);
                });
    }

    @Override
    public ExpenseProduct saveProduct(Store store, ExpenseProductRequest req) {
        ExpenseProduct product = findOrCreateProduct(store, req.getName(), req.getUnit(), req.getStockUnit());
        if (req.getUnit() != null) {
            product.setUnit(req.getUnit());
        }
        // Trocar a unidade de um material com saldo reinterpretaria o número já guardado
        // (3 KG viraria 3 G), então converte o saldo e o alerta junto.
        if (req.getStockUnit() != null && req.getStockUnit() != product.getStockUnit()) {
            MeasurementUnit from = product.getStockUnit();
            MeasurementUnit to = req.getStockUnit();
            product.setStockQuantity(from.convertTo(product.getStockQuantity(), to));
            if (product.getLowStockAlert() != null) {
                product.setLowStockAlert(from.convertTo(product.getLowStockAlert(), to));
            }
            product.setStockUnit(to);
        }
        if (req.getLowStockAlert() != null) {
            product.setLowStockAlert(req.getLowStockAlert());
        }
        return productRepository.save(product);
    }

    @Override
    public ExpenseProduct adjustStock(UUID storeId, Long productId, BigDecimal quantityDelta, MeasurementUnit unit) {
        ExpenseProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));

        if (!product.getStore().getId().equals(storeId)) {
            throw new ResourceNotFoundException("Produto não encontrado");
        }

        // O ajuste pode vir em outra unidade da mesma dimensão (ex.: consumir 500 G de um
        // material medido em KG). Unidades de dimensões diferentes são recusadas.
        MeasurementUnit source = unit != null ? unit : product.getStockUnit();
        BigDecimal delta = source.convertTo(quantityDelta, product.getStockUnit());

        BigDecimal newStock = product.getStockQuantity().add(delta).max(BigDecimal.ZERO);
        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseProduct> getLowStockAlerts(UUID storeId) {
        return productRepository.findLowStock(storeId);
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
            ExpenseProduct product = findOrCreateProduct(
                    store, itemReq.getProductName(), itemReq.getUnit(), itemReq.getUnitCode());

            // A compra pode vir em unidade diferente da do material (ex.: comprar 2 KG de
            // um material medido em G). O estoque sempre soma na unidade do material.
            MeasurementUnit purchaseUnit = itemReq.getUnitCode() != null
                    ? itemReq.getUnitCode()
                    : product.getStockUnit();
            BigDecimal quantityInStockUnit = purchaseUnit.convertTo(itemReq.getQuantity(), product.getStockUnit());

            product.setStockQuantity(product.getStockQuantity().add(quantityInStockUnit));
            productRepository.save(product);

            // O preço é por unidade comprada, então o subtotal usa a quantidade original —
            // 2 KG a R$10/KG são R$20, independentemente de o estoque ser medido em G.
            BigDecimal subtotal = itemReq.getUnitPrice().multiply(itemReq.getQuantity());

            ExpenseBatchItem item = new ExpenseBatchItem();
            item.setBatch(batch);
            item.setExpenseProductId(product.getId());
            item.setProductName(itemReq.getProductName());
            item.setUnit(itemReq.getUnit());
            item.setUnitCode(purchaseUnit);
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
    public RecurringExpensePayment payRecurring(UUID storeId, UUID id, RecurringExpensePaymentRequest req) {
        RecurringExpense expense = recurringRepository.findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recorrente não encontrada"));

        // O valor previsto na definição é só sugestão: conta de luz varia todo mês.
        BigDecimal amount = req != null && req.getAmount() != null ? req.getAmount() : expense.getAmount();
        Instant paidAt = req != null && req.getPaidAt() != null ? req.getPaidAt() : Instant.now();

        LocalDate paidDate = paidAt.atZone(ZoneOffset.UTC).toLocalDate();
        expense.setLastPaidDate(paidDate);
        expense.setNextDueDate(expense.getFrequency().nextDate(paidDate, expense.getDueDay()));
        recurringRepository.save(expense);

        PdvCashFlowRequest flowReq = new PdvCashFlowRequest();
        flowReq.setOfflineId(UUID.randomUUID().toString());
        flowReq.setType(FlowType.OUT);
        flowReq.setCategory(FlowCategory.RECURRING_EXPENSE);
        flowReq.setRecurringExpenseId(expense.getId());
        flowReq.setAmount(amount);
        flowReq.setDescription(expense.getName());
        flowReq.setFlowDate(paidAt);
        PdvCashFlow flow = cashFlowService.addEntry(expense.getStore(), flowReq);

        RecurringExpensePayment payment = new RecurringExpensePayment();
        payment.setRecurringExpense(expense);
        payment.setAmount(amount);
        payment.setPaidAt(paidAt);
        payment.setNote(req != null ? req.getNote() : null);
        payment.setCashFlowId(flow.getId());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringExpensePayment> listPayments(UUID storeId, UUID recurringExpenseId) {
        recurringRepository.findByIdAndStoreId(recurringExpenseId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recorrente não encontrada"));
        return paymentRepository.findByRecurringExpenseIdOrderByPaidAtDesc(recurringExpenseId);
    }

    @Override
    public RecurringExpensePayment addPaymentImages(UUID storeId, UUID paymentId, List<MultipartFile> images) {
        RecurringExpensePayment payment = requirePayment(storeId, paymentId);

        List<MultipartFile> files = images == null ? List.of() : images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
        if (files.isEmpty()) {
            throw new BusinessRuleException("NO_IMAGE_SENT", "Envie ao menos um comprovante");
        }
        if (payment.getImageUrls().size() + files.size() > RecurringExpensePayment.MAX_IMAGES) {
            throw new BusinessRuleException("TOO_MANY_IMAGES",
                    "Um pagamento aceita no máximo " + RecurringExpensePayment.MAX_IMAGES + " comprovantes");
        }

        files.forEach(file -> payment.getImageUrls().add(storageService.uploadFile(file)));
        return paymentRepository.save(payment);
    }

    @Override
    public RecurringExpensePayment removePaymentImage(UUID storeId, UUID paymentId, String imageUrl) {
        RecurringExpensePayment payment = requirePayment(storeId, paymentId);
        if (!payment.getImageUrls().remove(imageUrl)) {
            throw new ResourceNotFoundException("Comprovante não encontrado neste pagamento");
        }
        storageService.deleteFile(imageUrl);
        return paymentRepository.save(payment);
    }

    private RecurringExpensePayment requirePayment(UUID storeId, UUID paymentId) {
        return paymentRepository.findByIdAndStoreId(paymentId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado"));
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
