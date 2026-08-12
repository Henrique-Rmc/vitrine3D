package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.CreditStatus;
import com.store.vitrine3d.domain.model.PdvCreditPayment;
import com.store.vitrine3d.domain.model.PdvCustomer;
import com.store.vitrine3d.domain.model.PdvCustomerCredit;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.PdvCustomerCreditRepository;
import com.store.vitrine3d.domain.repository.PdvCustomerRepository;
import com.store.vitrine3d.domain.service.PdvCustomerService;
import com.store.vitrine3d.rest.dto.PdvCreditPaymentRequest;
import com.store.vitrine3d.rest.dto.PdvCustomerCreditRequest;
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

    public PdvCustomerServiceImpl(PdvCustomerRepository customerRepository,
                                   PdvCustomerCreditRepository creditRepository) {
        this.customerRepository = customerRepository;
        this.creditRepository = creditRepository;
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
        credit.setStatus(credit.getAmountPaid().compareTo(credit.getTotalDue()) >= 0
                ? CreditStatus.PAID : CreditStatus.PARTIAL);

        creditRepository.save(credit);
        return payment;
    }

    private void applyFields(PdvCustomer customer, PdvCustomerRequest req) {
        customer.setName(req.getName());
        customer.setPhone(req.getPhone());
        customer.setCpf(req.getCpf());
        customer.setAddress(req.getAddress());
    }

    private PdvCustomer requireCustomer(UUID storeId, UUID customerId) {
        return customerRepository.findByIdAndStoreId(customerId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", customerId.toString()));
    }
}
