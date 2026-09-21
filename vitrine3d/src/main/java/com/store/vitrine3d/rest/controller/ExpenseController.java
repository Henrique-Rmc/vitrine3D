package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.domain.service.ExpenseService;
import com.store.vitrine3d.domain.service.impl.PdvAccessGuard;
import com.store.vitrine3d.rest.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "PDV - Gastos Operacionais", description = "Gerenciamento de gastos e estoque de insumos — requer plano Pro")
@RestController
@RequestMapping("/api/pdv/expenses")
public class ExpenseController {

    private final CurrentStoreResolver storeResolver;
    private final PdvAccessGuard accessGuard;
    private final ExpenseService expenseService;

    public ExpenseController(CurrentStoreResolver storeResolver,
                              PdvAccessGuard accessGuard,
                              ExpenseService expenseService) {
        this.storeResolver = storeResolver;
        this.accessGuard = accessGuard;
        this.expenseService = expenseService;
    }

    // ── Catálogo + estoque de insumos ─────────────────────────────────────────

    @GetMapping("/products")
    public List<ExpenseProductResponse> listProducts(
            @RequestParam(required = false) String q) {
        Store store = currentStore();
        return expenseService.listProducts(store.getId(), q).stream()
                .map(ExpenseProductResponse::from).toList();
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseProductResponse findOrCreateProduct(
            @RequestBody Map<String, String> body) {
        Store store = currentStore();
        String name = body.get("name");
        String unit = body.get("unit");
        return ExpenseProductResponse.from(expenseService.findOrCreateProduct(store, name, unit));
    }

    @PatchMapping("/products/{id}/stock")
    public ExpenseProductResponse adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseStockPatchRequest request) {
        Store store = currentStore();
        return ExpenseProductResponse.from(
                expenseService.adjustStock(store.getId(), id, request.getQuantityDelta()));
    }

    @GetMapping("/products/low-stock")
    public List<ExpenseProductResponse> getLowStockAlerts() {
        Store store = currentStore();
        return expenseService.getLowStockAlerts(store.getId()).stream()
                .map(ExpenseProductResponse::from).toList();
    }

    // ── Batches unitários ─────────────────────────────────────────────────────

    @PostMapping("/batches")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseBatchResponse createBatch(@Valid @RequestBody ExpenseBatchRequest request) {
        Store store = currentStore();
        return ExpenseBatchResponse.from(expenseService.createBatch(store, request));
    }

    @GetMapping("/batches")
    public Page<ExpenseBatchResponse> listBatches(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Store store = currentStore();
        Instant[] range = toRange(from, to);
        return expenseService.listBatches(store.getId(), range[0], range[1], page, size)
                .map(ExpenseBatchResponse::from);
    }

    @GetMapping("/batches/{id}")
    public ExpenseBatchResponse getBatch(@PathVariable UUID id) {
        Store store = currentStore();
        return ExpenseBatchResponse.from(expenseService.getBatch(store.getId(), id));
    }

    @GetMapping("/batches/summary")
    public Map<String, Object> getBatchesSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Store store = currentStore();
        Instant[] range = toRange(from, to);
        return Map.of("totalSpent", expenseService.sumBatchesByPeriod(store.getId(), range[0], range[1]));
    }

    // ── Recorrentes ───────────────────────────────────────────────────────────

    @GetMapping("/recurring")
    public List<RecurringExpenseResponse> listRecurring() {
        Store store = currentStore();
        return expenseService.listRecurring(store.getId()).stream()
                .map(RecurringExpenseResponse::from).toList();
    }

    @PostMapping("/recurring")
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringExpenseResponse createRecurring(@Valid @RequestBody RecurringExpenseRequest request) {
        Store store = currentStore();
        return RecurringExpenseResponse.from(expenseService.createRecurring(store, request));
    }

    @PutMapping("/recurring/{id}")
    public RecurringExpenseResponse updateRecurring(@PathVariable UUID id,
                                                     @Valid @RequestBody RecurringExpenseRequest request) {
        Store store = currentStore();
        return RecurringExpenseResponse.from(expenseService.updateRecurring(store.getId(), id, request));
    }

    @DeleteMapping("/recurring/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateRecurring(@PathVariable UUID id) {
        Store store = currentStore();
        expenseService.deactivateRecurring(store.getId(), id);
    }

    @PostMapping("/recurring/{id}/pay")
    public RecurringExpenseResponse payRecurring(@PathVariable UUID id) {
        Store store = currentStore();
        return RecurringExpenseResponse.from(expenseService.payRecurring(store.getId(), id));
    }

    @GetMapping("/recurring/alerts")
    public List<RecurringExpenseResponse> getAlerts() {
        Store store = currentStore();
        return expenseService.getAlerts(store.getId()).stream()
                .map(RecurringExpenseResponse::from).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Store currentStore() {
        Store store = storeResolver.getCurrentStore();
        accessGuard.assertAccess(store.getId());
        return store;
    }

    private Instant[] toRange(LocalDate from, LocalDate to) {
        Instant start = from != null
                ? from.atStartOfDay(ZoneOffset.UTC).toInstant()
                : Instant.EPOCH;
        Instant end = to != null
                ? to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                : Instant.now().plusSeconds(86400);
        return new Instant[]{start, end};
    }
}
