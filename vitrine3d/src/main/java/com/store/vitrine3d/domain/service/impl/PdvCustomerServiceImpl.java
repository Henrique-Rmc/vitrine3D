package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.CreditStatus;
import com.store.vitrine3d.domain.model.FlowCategory;
import com.store.vitrine3d.domain.model.FlowType;
import com.store.vitrine3d.domain.model.PdvCreditPayment;
import com.store.vitrine3d.domain.model.PdvCustomer;
import com.store.vitrine3d.domain.model.PdvCustomerCredit;
import com.store.vitrine3d.domain.model.PdvCustomerRelationship;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.SaleStatus;
import com.store.vitrine3d.domain.repository.PdvCustomerCreditRepository;
import com.store.vitrine3d.domain.repository.PdvCustomerRelationshipRepository;
import com.store.vitrine3d.domain.repository.PdvCustomerRepository;
import com.store.vitrine3d.domain.repository.PdvSaleRepository;
import com.store.vitrine3d.domain.service.PdvCashFlowService;
import com.store.vitrine3d.domain.service.PdvCustomerService;
import com.store.vitrine3d.rest.dto.PdvCashFlowRequest;
import com.store.vitrine3d.rest.dto.PdvCreditPaymentRequest;
import com.store.vitrine3d.rest.dto.PdvCustomerCreditRequest;
import com.store.vitrine3d.rest.dto.PdvCustomerRelationshipRequest;
import com.store.vitrine3d.rest.dto.PdvCustomerRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PdvCustomerServiceImpl implements PdvCustomerService {

    private final PdvCustomerRepository customerRepository;
    private final PdvCustomerCreditRepository creditRepository;
    private final PdvCustomerRelationshipRepository relationshipRepository;
    private final PdvSaleRepository saleRepository;
    private final PdvCashFlowService cashFlowService;

    public PdvCustomerServiceImpl(PdvCustomerRepository customerRepository,
                                   PdvCustomerCreditRepository creditRepository,
                                   PdvCustomerRelationshipRepository relationshipRepository,
                                   PdvSaleRepository saleRepository,
                                   PdvCashFlowService cashFlowService) {
        this.customerRepository = customerRepository;
        this.creditRepository = creditRepository;
        this.relationshipRepository = relationshipRepository;
        this.saleRepository = saleRepository;
        this.cashFlowService = cashFlowService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdvCustomer> listCustomers(UUID storeId) {
        return customerRepository.findByStoreIdOrderByNameAsc(storeId);
    }

    @Override
    public PdvCustomer createCustomer(Store store, PdvCustomerRequest req) {
        PdvCustomer customer = new PdvCustomer();
        customer.setStore(store);
        applyFields(customer, req);
        return customerRepository.save(customer);
    }

    @Override
    public PdvCustomer updateCustomer(UUID storeId, UUID customerId, PdvCustomerRequest req) {
        PdvCustomer customer = requireCustomer(storeId, customerId);
        applyFields(customer, req);
        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(UUID storeId, UUID customerId) {
        PdvCustomer customer = requireCustomer(storeId, customerId);

        if (creditRepository.existsByCustomerIdAndStatusIn(
                customerId, List.of(CreditStatus.OPEN, CreditStatus.PARTIAL))) {
            throw new BusinessRuleException("CUSTOMER_HAS_OPEN_DEBTS",
                    "Não é possível remover um cliente com débitos em aberto");
        }

        customerRepository.delete(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdvCustomerCredit> listCredits(UUID storeId, UUID customerId) {
        requireCustomer(storeId, customerId);
        return creditRepository.findByCustomerIdAndCustomerStoreIdOrderByCreatedAtDesc(customerId, storeId);
    }

    @Override
    public PdvCustomerCredit addCredit(UUID storeId, UUID customerId, PdvCustomerCreditRequest req) {
        PdvCustomer customer = requireCustomer(storeId, customerId);

        PdvCustomerCredit credit = new PdvCustomerCredit();
        credit.setCustomer(customer);
        credit.setOriginSaleId(req.getOriginSaleId());
        credit.setProductName(req.getProductName());
        credit.setProductId(req.getProductId());
        credit.setOriginalAmount(req.getOriginalAmount());
        credit.setTotalDue(req.getTotalDue());
        credit.setDueDate(req.getDueDate());
        credit.setNote(req.getNote());

        return creditRepository.save(credit);
    }

    @Override
    public PdvCreditPayment addPayment(UUID storeId, UUID customerId, UUID creditId, PdvCreditPaymentRequest req) {
        requireCustomer(storeId, customerId);
        PdvCustomerCredit credit = creditRepository.findByIdAndCustomerStoreId(creditId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Crédito", creditId.toString()));

        if (credit.getStatus() == CreditStatus.PAID) {
            throw new BusinessRuleException("CREDIT_ALREADY_PAID", "Crédito já está totalmente pago");
        }

        PdvCreditPayment payment = new PdvCreditPayment();
        payment.setCredit(credit);
        payment.setAmount(req.getAmount());
        payment.setPaymentMethod(req.getPaymentMethod());
        payment.setPaidAt(req.getPaidAt() != null ? req.getPaidAt() : Instant.now());

        credit.setAmountPaid(credit.getAmountPaid().add(req.getAmount()));
        credit.getPayments().add(payment);

        boolean nowPaid = credit.getAmountPaid().compareTo(credit.getTotalDue()) >= 0;
        credit.setStatus(nowPaid ? CreditStatus.PAID : CreditStatus.PARTIAL);

        creditRepository.save(credit);

        // Lança entrada no caixa pelo pagamento do débito
        PdvCashFlowRequest flowReq = new PdvCashFlowRequest();
        flowReq.setOfflineId(UUID.randomUUID().toString());
        flowReq.setType(FlowType.IN);
        flowReq.setCategory(FlowCategory.CREDIT_PAYMENT);
        flowReq.setAmount(payment.getAmount());
        flowReq.setDescription("Pagamento de débito - " + credit.getCustomer().getName());
        flowReq.setFlowDate(payment.getPaidAt());
        cashFlowService.addEntry(credit.getCustomer().getStore(), flowReq);

        // Fecha o loop: se o débito foi quitado e veio de uma venda, marca a venda como COMPLETED
        if (nowPaid && credit.getOriginSaleId() != null) {
            saleRepository.findById(credit.getOriginSaleId()).ifPresent(sale -> {
                if (sale.getStatus() == SaleStatus.PARTIAL) {
                    sale.setStatus(SaleStatus.COMPLETED);
                    saleRepository.save(sale);
                }
            });
        }

        return payment;
    }

    // ── Relacionamentos familiares ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PdvCustomerRelationship> listRelationships(UUID storeId, UUID customerId) {
        requireCustomer(storeId, customerId);
        return relationshipRepository.findAllByCustomerInvolved(storeId, customerId);
    }

    @Override
    public PdvCustomerRelationship addRelationship(UUID storeId, UUID customerId,
                                                    PdvCustomerRelationshipRequest req) {
        PdvCustomer customer = requireCustomer(storeId, customerId);
        PdvCustomer relative = requireCustomer(storeId, req.getRelativeId());

        if (customer.getId().equals(relative.getId())) {
            throw new BusinessRuleException("SELF_RELATIONSHIP", "Um cliente não pode ser parente de si mesmo");
        }
        if (relationshipRepository.existsByCustomerIdAndRelativeId(customerId, req.getRelativeId())) {
            throw new BusinessRuleException("DUPLICATE_RELATIONSHIP", "Esse vínculo já está cadastrado");
        }

        PdvCustomerRelationship rel = new PdvCustomerRelationship();
        rel.setStore(customer.getStore());
        rel.setCustomer(customer);
        rel.setRelative(relative);
        rel.setRelationship(req.getRelationship());
        rel.setNote(req.getNote());
        return relationshipRepository.save(rel);
    }

    @Override
    public void removeRelationship(UUID storeId, UUID customerId, Long relationshipId) {
        requireCustomer(storeId, customerId);
        PdvCustomerRelationship rel = relationshipRepository.findByIdAndStoreId(relationshipId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo", relationshipId.toString()));
        relationshipRepository.delete(rel);
    }

    private void applyFields(PdvCustomer customer, PdvCustomerRequest req) {
        customer.setName(req.getName());
        customer.setPhone(req.getPhone());
        customer.setEmail(req.getEmail());
        customer.setCpf(req.getCpf());
        customer.setAddress(req.getAddress());
    }

    private PdvCustomer requireCustomer(UUID storeId, UUID customerId) {
        return customerRepository.findByIdAndStoreId(customerId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", customerId.toString()));
    }
}
