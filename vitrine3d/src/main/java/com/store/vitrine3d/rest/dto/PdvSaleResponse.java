package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.PaymentMethod;
import com.store.vitrine3d.domain.model.PdvSale;
import com.store.vitrine3d.domain.model.SaleStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class PdvSaleResponse {
    private UUID id;
    private String offlineId;
    private UUID customerId;
    private String customerName;
    private UUID operatorId;
    private String operatorName;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private SaleStatus status;
    private String note;
    private Instant saleDate;
    private Instant syncedAt;
    private List<PdvSaleItemResponse> items;

    public static PdvSaleResponse from(PdvSale sale) {
        PdvSaleResponse dto = new PdvSaleResponse();
        dto.setId(sale.getId());
        dto.setOfflineId(sale.getOfflineId());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setAmountPaid(sale.getAmountPaid());
        dto.setChangeAmount(sale.getChangeAmount());
        dto.setStatus(sale.getStatus());
        dto.setNote(sale.getNote());
        dto.setSaleDate(sale.getSaleDate());
        dto.setSyncedAt(sale.getSyncedAt());
        if (sale.getCustomer() != null) {
            dto.setCustomerId(sale.getCustomer().getId());
            dto.setCustomerName(sale.getCustomer().getName());
        }
        if (sale.getOperator() != null) {
            dto.setOperatorId(sale.getOperator().getId());
            dto.setOperatorName(sale.getOperator().getName());
        }
        dto.setItems(sale.getItems().stream().map(PdvSaleItemResponse::from).toList());
        return dto;
    }
}
