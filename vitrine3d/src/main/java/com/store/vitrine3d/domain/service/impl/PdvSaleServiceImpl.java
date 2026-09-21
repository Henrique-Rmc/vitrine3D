package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.FlowCategory;
import com.store.vitrine3d.domain.model.FlowType;
import com.store.vitrine3d.domain.model.PdvCustomer;
import com.store.vitrine3d.domain.model.PdvCustomerCredit;
import com.store.vitrine3d.domain.model.PdvEmployee;
import com.store.vitrine3d.domain.model.PdvSale;
import com.store.vitrine3d.domain.model.PdvSaleItem;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.SaleStatus;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.PdvCustomerCreditRepository;
import com.store.vitrine3d.domain.repository.PdvCustomerRepository;
import com.store.vitrine3d.domain.repository.PdvEmployeeRepository;
import com.store.vitrine3d.domain.repository.PdvSaleRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.service.PdvCashFlowService;
import com.store.vitrine3d.domain.service.PdvSaleService;
import com.store.vitrine3d.rest.dto.PdvCashFlowRequest;
import com.store.vitrine3d.rest.dto.PdvDiscountSummaryResponse;
import com.store.vitrine3d.rest.dto.PdvSaleItemRequest;
import com.store.vitrine3d.rest.dto.PdvSaleRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PdvSaleServiceImpl implements PdvSaleService {

    private final PdvSaleRepository saleRepository;
    private final PdvCustomerRepository customerRepository;
    private final PdvEmployeeRepository employeeRepository;
    private final ProductRepository productRepository;
    private final PdvCustomerCreditRepository creditRepository;
    private final PdvCashFlowService cashFlowService;

    public PdvSaleServiceImpl(PdvSaleRepository saleRepository,
                               PdvCustomerRepository customerRepository,
                               PdvEmployeeRepository employeeRepository,
                               ProductRepository productRepository,
                               PdvCustomerCreditRepository creditRepository,
                               PdvCashFlowService cashFlowService) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.productRepository = productRepository;
        this.creditRepository = creditRepository;
        this.cashFlowService = cashFlowService;
    }

    @Override
    public PdvSale createSale(Store store, PdvSaleRequest req) {
        if (isDuplicate(store.getId(), req.getOfflineId())) {
            throw new BusinessRuleException("DUPLICATE_SALE", "Venda já registrada com este offlineId");
        }

        PdvSale sale = new PdvSale();
        sale.setOfflineId(req.getOfflineId());
        sale.setStore(store);
        sale.setPaymentMethod(req.getPaymentMethod());
        sale.setSaleDate(req.getSaleDate());
        sale.setNote(req.getNote());

        PdvCustomer customer = null;
        if (req.getCustomerId() != null) {
            customer = customerRepository.findByIdAndStoreId(req.getCustomerId(), store.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", req.getCustomerId().toString()));
            sale.setCustomer(customer);
        }
        if (req.getOperatorId() != null) {
            PdvEmployee operator = employeeRepository.findByIdAndStoreId(req.getOperatorId(), store.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário", req.getOperatorId().toString()));
            sale.setOperator(operator);
        }

        List<PdvSaleItem> items = new ArrayList<>();
        BigDecimal totalAmount   = BigDecimal.ZERO;
        BigDecimal originalTotal = BigDecimal.ZERO;

        for (PdvSaleItemRequest itemReq : req.getItems()) {
            BigDecimal effectiveOriginal = itemReq.getOriginalUnitPrice() != null
                    ? itemReq.getOriginalUnitPrice()
                    : itemReq.getUnitPrice();

            Product product = itemReq.getProductId() != null
                    ? productRepository.findById(itemReq.getProductId()).orElse(null)
                    : null;

            PdvSaleItem item = new PdvSaleItem();
            item.setSale(sale);
            item.setProductId(itemReq.getProductId());
            item.setProductName(itemReq.getProductName());
            item.setOriginalUnitPrice(itemReq.getOriginalUnitPrice());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setQuantity(itemReq.getQuantity());

            // Snapshot do custo: request > catálogo > null (item sem custo conhecido)
            item.setUnitCost(itemReq.getUnitCost() != null
                    ? itemReq.getUnitCost()
                    : (product != null ? product.getCostPrice() : null));

            BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            item.setSubtotal(subtotal);
            totalAmount   = totalAmount.add(subtotal);
            originalTotal = originalTotal.add(effectiveOriginal.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            items.add(item);

            if (product != null && Boolean.TRUE.equals(product.getTrackStock())) {
                product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
                productRepository.save(product);
            }
        }

        int cmp = req.getAmountPaid().compareTo(totalAmount);

        // Só registra original se houve desconto em algum item
        if (originalTotal.compareTo(totalAmount) > 0) {
            sale.setOriginalAmount(originalTotal);
        }

        sale.setTotalAmount(totalAmount);
        sale.setAmountPaid(req.getAmountPaid());
        sale.setItems(items);

        if (cmp < 0) {
            // Pagamento parcial — exige cliente vinculado para rastrear o débito
            if (customer == null) {
                throw new BusinessRuleException("CUSTOMER_REQUIRED_FOR_PARTIAL",
                        "Informe o cliente para vendas com saldo em aberto");
            }
            sale.setChangeAmount(BigDecimal.ZERO);
            sale.setStatus(SaleStatus.PARTIAL);
        } else {
            sale.setChangeAmount(req.getAmountPaid().subtract(totalAmount));
            sale.setStatus(SaleStatus.COMPLETED);
        }

        PdvSale saved = saleRepository.save(sale);

        // Lança entrada no caixa: valor líquido recebido (amountPaid - troco)
        BigDecimal cashReceived = saved.getAmountPaid().subtract(saved.getChangeAmount());
        if (cashReceived.compareTo(BigDecimal.ZERO) > 0) {
            PdvCashFlowRequest flowReq = new PdvCashFlowRequest();
            flowReq.setOfflineId(UUID.randomUUID().toString());
            flowReq.setType(FlowType.IN);
            flowReq.setCategory(FlowCategory.SALE);
            flowReq.setAmount(cashReceived);
            flowReq.setDescription("Venda" + (saved.getCustomer() != null
                    ? " - " + saved.getCustomer().getName() : ""));
            flowReq.setFlowDate(saved.getSaleDate());
            cashFlowService.addEntry(store, flowReq);
        }

        // Gera débito automático se houve pagamento parcial
        if (cmp < 0) {
            String productSummary = items.size() == 1
                    ? items.get(0).getProductName()
                    : "Venda com " + items.size() + " itens";

            PdvCustomerCredit credit = new PdvCustomerCredit();
            credit.setCustomer(customer);
            credit.setOriginSaleId(saved.getId());
            credit.setProductName(productSummary);
            credit.setOriginalAmount(totalAmount);
            credit.setTotalDue(totalAmount.subtract(req.getAmountPaid()));
            creditRepository.save(credit);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PdvSale> listSales(UUID storeId, Instant from, Instant to, int page, int size) {
        return saleRepository.findByStoreIdAndSaleDateBetweenOrderBySaleDateDesc(
                storeId, from, to, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public PdvSale getSale(UUID storeId, UUID saleId) {
        return saleRepository.findByIdAndStoreId(saleId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Venda", saleId.toString()));
    }

    @Override
    public PdvSale cancelSale(UUID storeId, UUID saleId) {
        PdvSale sale = getSale(storeId, saleId);
        if (sale.getStatus() == SaleStatus.CANCELLED) {
            throw new BusinessRuleException("SALE_ALREADY_CANCELLED", "Venda já está cancelada");
        }

        // Débito gerado por esta venda: só pode ser cancelado se ninguém pagou nada.
        // Apagar um crédito com pagamentos destruiria histórico financeiro.
        PdvCustomerCredit credit = creditRepository.findByOriginSaleId(saleId).orElse(null);
        if (credit != null) {
            if (credit.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessRuleException("SALE_HAS_CREDIT_PAYMENTS",
                        "Venda possui débito com pagamentos registrados; estorne os pagamentos antes de cancelar");
            }
            creditRepository.delete(credit);
        }

        // Devolve ao estoque o que saiu na venda
        for (PdvSaleItem item : sale.getItems()) {
            if (item.getProductId() == null) continue;
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                if (Boolean.TRUE.equals(product.getTrackStock())) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            });
        }

        // Estorna do caixa o valor que havia entrado
        BigDecimal cashReceived = sale.getAmountPaid().subtract(sale.getChangeAmount());
        if (cashReceived.compareTo(BigDecimal.ZERO) > 0) {
            PdvCashFlowRequest flowReq = new PdvCashFlowRequest();
            flowReq.setOfflineId(UUID.randomUUID().toString());
            flowReq.setType(FlowType.OUT);
            flowReq.setCategory(FlowCategory.SALE);
            flowReq.setAmount(cashReceived);
            flowReq.setDescription("Estorno de venda" + (sale.getCustomer() != null
                    ? " - " + sale.getCustomer().getName() : ""));
            flowReq.setFlowDate(Instant.now());
            cashFlowService.addEntry(sale.getStore(), flowReq);
        }

        sale.setStatus(SaleStatus.CANCELLED);
        return saleRepository.save(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicate(UUID storeId, String offlineId) {
        return saleRepository.existsByStoreIdAndOfflineId(storeId, offlineId);
    }

    @Override
    @Transactional(readOnly = true)
    public PdvDiscountSummaryResponse getDiscountSummary(UUID storeId, Instant from, Instant to) {
        BigDecimal totalOriginal = saleRepository.sumOriginalAmountByPeriod(storeId, from, to);
        BigDecimal totalCharged  = saleRepository.sumChargedAmountForDiscountedByPeriod(storeId, from, to);
        PdvDiscountSummaryResponse summary = new PdvDiscountSummaryResponse();
        summary.setTotalOriginal(totalOriginal);
        summary.setTotalCharged(totalCharged);
        summary.setTotalDiscounted(totalOriginal.subtract(totalCharged));
        return summary;
    }
}
