package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.PdvEmployee;
import com.store.vitrine3d.domain.model.PdvSale;
import com.store.vitrine3d.domain.model.PdvSaleItem;
import com.store.vitrine3d.domain.model.SaleStatus;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.PdvCustomerRepository;
import com.store.vitrine3d.domain.repository.PdvEmployeeRepository;
import com.store.vitrine3d.domain.repository.PdvSaleRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.service.PdvSaleService;
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

    public PdvSaleServiceImpl(PdvSaleRepository saleRepository,
                               PdvCustomerRepository customerRepository,
                               PdvEmployeeRepository employeeRepository,
                               ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.productRepository = productRepository;
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

        if (req.getCustomerId() != null) {
            sale.setCustomer(customerRepository.findByIdAndStoreId(req.getCustomerId(), store.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", req.getCustomerId().toString())));
        }
        if (req.getOperatorId() != null) {
            PdvEmployee operator = employeeRepository.findByIdAndStoreId(req.getOperatorId(), store.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário", req.getOperatorId().toString()));
            sale.setOperator(operator);
        }

        List<PdvSaleItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PdvSaleItemRequest itemReq : req.getItems()) {
            PdvSaleItem item = new PdvSaleItem();
            item.setSale(sale);
            item.setProductId(itemReq.getProductId());
            item.setProductName(itemReq.getProductName());
            item.setUnitPrice(itemReq.getUnitPrice());
            item.setQuantity(itemReq.getQuantity());
            BigDecimal subtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            item.setSubtotal(subtotal);
            total = total.add(subtotal);
            items.add(item);

            if (itemReq.getProductId() != null) {
                productRepository.findById(itemReq.getProductId()).ifPresent(product -> {
                    if (Boolean.TRUE.equals(product.getTrackStock())) {
                        product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
                        productRepository.save(product);
                    }
                });
            }
        }

        sale.setTotalAmount(total);
        sale.setAmountPaid(req.getAmountPaid());
        sale.setChangeAmount(req.getAmountPaid().subtract(total).max(BigDecimal.ZERO));
        sale.setItems(items);

        return saleRepository.save(sale);
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
        sale.setStatus(SaleStatus.CANCELLED);
        return saleRepository.save(sale);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicate(UUID storeId, String offlineId) {
        return saleRepository.existsByStoreIdAndOfflineId(storeId, offlineId);
    }
}
