package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.domain.service.PdvCashFlowService;
import com.store.vitrine3d.domain.service.PdvCustomerService;
import com.store.vitrine3d.domain.service.PdvEmployeeService;
import com.store.vitrine3d.domain.service.PdvSaleService;
import com.store.vitrine3d.domain.service.impl.PdvAccessGuard;
import com.store.vitrine3d.domain.service.impl.PdvSyncService;
import com.store.vitrine3d.rest.dto.*;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
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
import java.util.UUID;

@Tag(name = "PDV", description = "Ponto de venda — requer plano Premium")
@RestController
@RequestMapping("/api/pdv")
public class PdvController {

    private final CurrentStoreResolver storeResolver;
    private final PdvAccessGuard accessGuard;
    private final PdvSaleService saleService;
    private final PdvCashFlowService cashFlowService;
    private final PdvCustomerService customerService;
    private final PdvEmployeeService employeeService;
    private final PdvSyncService syncService;
    private final ProductRepository productRepository;

    public PdvController(CurrentStoreResolver storeResolver,
                         PdvAccessGuard accessGuard,
                         PdvSaleService saleService,
                         PdvCashFlowService cashFlowService,
                         PdvCustomerService customerService,
                         PdvEmployeeService employeeService,
                         PdvSyncService syncService,
                         ProductRepository productRepository) {
        this.storeResolver = storeResolver;
        this.accessGuard = accessGuard;
        this.saleService = saleService;
        this.cashFlowService = cashFlowService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.syncService = syncService;
        this.productRepository = productRepository;
    }

    // ── Sales ──────────────────────────────────────────────────────────────

    @PostMapping("/sales")
    @ResponseStatus(HttpStatus.CREATED)
    public PdvSaleResponse createSale(@Valid @RequestBody PdvSaleRequest request) {
        Store store = currentStore();
        return PdvSaleResponse.from(saleService.createSale(store, request));
    }

    @GetMapping("/sales")
    public Page<PdvSaleResponse> listSales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Store store = currentStore();
        Instant[] range = toRange(from, to);
        return saleService.listSales(store.getId(), range[0], range[1], page, size)
                .map(PdvSaleResponse::from);
    }

    @GetMapping("/sales/{id}")
    public PdvSaleResponse getSale(@PathVariable UUID id) {
        Store store = currentStore();
        return PdvSaleResponse.from(saleService.getSale(store.getId(), id));
    }

    @PatchMapping("/sales/{id}/cancel")
    public PdvSaleResponse cancelSale(@PathVariable UUID id) {
        Store store = currentStore();
        return PdvSaleResponse.from(saleService.cancelSale(store.getId(), id));
    }

    // ── Cash Flow ──────────────────────────────────────────────────────────

    @PostMapping("/cash-flow")
    @ResponseStatus(HttpStatus.CREATED)
    public PdvCashFlowResponse addCashFlow(@Valid @RequestBody PdvCashFlowRequest request) {
        Store store = currentStore();
        return PdvCashFlowResponse.from(cashFlowService.addEntry(store, request));
    }

    @GetMapping("/cash-flow")
    public Page<PdvCashFlowResponse> listCashFlow(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Store store = currentStore();
        Instant[] range = toRange(from, to);
        return cashFlowService.listEntries(store.getId(), range[0], range[1], page, size)
                .map(PdvCashFlowResponse::from);
    }

    @GetMapping("/cash-flow/summary")
    public PdvCashFlowSummaryResponse getCashFlowSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Store store = currentStore();
        Instant[] range = toRange(from, to);
        return cashFlowService.getSummary(store.getId(), range[0], range[1]);
    }

    // ── Customers ──────────────────────────────────────────────────────────

    @GetMapping("/customers")
    public List<PdvCustomerResponse> listCustomers() {
        Store store = currentStore();
        return customerService.listCustomers(store.getId()).stream()
                .map(PdvCustomerResponse::from).toList();
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public PdvCustomerResponse createCustomer(@Valid @RequestBody PdvCustomerRequest request) {
        Store store = currentStore();
        return PdvCustomerResponse.from(customerService.createCustomer(store, request));
    }

    @PutMapping("/customers/{id}")
    public PdvCustomerResponse updateCustomer(@PathVariable UUID id,
                                               @Valid @RequestBody PdvCustomerRequest request) {
        Store store = currentStore();
        return PdvCustomerResponse.from(customerService.updateCustomer(store.getId(), id, request));
    }

    @DeleteMapping("/customers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID id) {
        Store store = currentStore();
        customerService.deleteCustomer(store.getId(), id);
    }

    @GetMapping("/customers/{id}/credits")
    public List<PdvCustomerCreditResponse> listCredits(@PathVariable UUID id) {
        Store store = currentStore();
        return customerService.listCredits(store.getId(), id).stream()
                .map(PdvCustomerCreditResponse::from).toList();
    }

    @PostMapping("/customers/{id}/credits")
    @ResponseStatus(HttpStatus.CREATED)
    public PdvCustomerCreditResponse addCredit(@PathVariable UUID id,
                                                @Valid @RequestBody PdvCustomerCreditRequest request) {
        Store store = currentStore();
        return PdvCustomerCreditResponse.from(customerService.addCredit(store.getId(), id, request));
    }

    @PostMapping("/customers/{customerId}/credits/{creditId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PdvCreditPaymentResponse addPayment(@PathVariable UUID customerId,
                                                @PathVariable UUID creditId,
                                                @Valid @RequestBody PdvCreditPaymentRequest request) {
        Store store = currentStore();
        return PdvCreditPaymentResponse.from(
                customerService.addPayment(store.getId(), customerId, creditId, request));
    }

    // ── Employees ──────────────────────────────────────────────────────────

    @GetMapping("/employees")
    public List<PdvEmployeeResponse> listEmployees() {
        Store store = currentStore();
        return employeeService.list(store.getId()).stream()
                .map(PdvEmployeeResponse::from).toList();
    }

    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public PdvEmployeeResponse createEmployee(@Valid @RequestBody PdvEmployeeRequest request) {
        Store store = currentStore();
        return PdvEmployeeResponse.from(employeeService.create(store, request));
    }

    @PutMapping("/employees/{id}")
    public PdvEmployeeResponse updateEmployee(@PathVariable UUID id,
                                               @Valid @RequestBody PdvEmployeeRequest request) {
        Store store = currentStore();
        return PdvEmployeeResponse.from(employeeService.update(store.getId(), id, request));
    }

    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateEmployee(@PathVariable UUID id) {
        Store store = currentStore();
        employeeService.delete(store.getId(), id);
    }

    @PostMapping("/employees/pin")
    public PdvEmployeeResponse verifyPin(@Valid @RequestBody PdvPinAuthRequest request) {
        Store store = currentStore();
        return PdvEmployeeResponse.from(
                employeeService.verifyPin(store.getId(), request.getEmployeeId(), request.getPin()));
    }

    // ── Stock ──────────────────────────────────────────────────────────────

    @PatchMapping("/products/{id}/stock")
    public PdvStockResponse adjustStock(@PathVariable Long id,
                                         @Valid @RequestBody PdvStockPatchRequest request) {
        Store store = currentStore();
        var product = productRepository.findById(id)
                .filter(p -> p.getStore().getId().equals(store.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Produto", id.toString()));

        if (request.getTrackStock() != null) product.setTrackStock(request.getTrackStock());
        product.setStockQuantity(product.getStockQuantity() + request.getQuantityDelta());
        return PdvStockResponse.from(productRepository.save(product));
    }

    // ── Sync ───────────────────────────────────────────────────────────────

    @PostMapping("/sync")
    public PdvSyncResponse sync(@Valid @RequestBody PdvSyncRequest request) {
        Store store = currentStore();
        return syncService.sync(store, request);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

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
