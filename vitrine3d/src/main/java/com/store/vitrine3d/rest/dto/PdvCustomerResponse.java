package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.CreditStatus;
import com.store.vitrine3d.domain.model.PdvCustomer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PdvCustomerResponse {
    private UUID id;
    private String name;
    private String phone;
    private String cpf;
    private String address;
    private Instant createdAt;
    private int openCreditsCount;
    private BigDecimal totalBalance;

    public static PdvCustomerResponse from(PdvCustomer customer) {
        PdvCustomerResponse dto = new PdvCustomerResponse();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setPhone(customer.getPhone());
        dto.setCpf(customer.getCpf());
        dto.setAddress(customer.getAddress());
        dto.setCreatedAt(customer.getCreatedAt());

        long open = customer.getCredits().stream()
                .filter(c -> c.getStatus() == CreditStatus.OPEN || c.getStatus() == CreditStatus.PARTIAL)
                .count();
        dto.setOpenCreditsCount((int) open);

        BigDecimal balance = customer.getCredits().stream()
                .filter(c -> c.getStatus() == CreditStatus.OPEN || c.getStatus() == CreditStatus.PARTIAL)
                .map(c -> c.getTotalDue().subtract(c.getAmountPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalBalance(balance);

        return dto;
    }
}
