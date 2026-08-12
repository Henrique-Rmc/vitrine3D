package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.PdvCreditPayment;
import com.store.vitrine3d.domain.model.PdvCustomer;
import com.store.vitrine3d.domain.model.PdvCustomerCredit;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.rest.dto.PdvCreditPaymentRequest;
import com.store.vitrine3d.rest.dto.PdvCustomerCreditRequest;
import com.store.vitrine3d.rest.dto.PdvCustomerRequest;

import java.util.List;
import java.util.UUID;

public interface PdvCustomerService {
    List<PdvCustomer> listCustomers(UUID storeId);
    PdvCustomer createCustomer(Store store, PdvCustomerRequest request);
    PdvCustomer updateCustomer(UUID storeId, UUID customerId, PdvCustomerRequest request);
    void deleteCustomer(UUID storeId, UUID customerId);
    List<PdvCustomerCredit> listCredits(UUID storeId, UUID customerId);
    PdvCustomerCredit addCredit(UUID storeId, UUID customerId, PdvCustomerCreditRequest request);
    PdvCreditPayment addPayment(UUID storeId, UUID customerId, UUID creditId, PdvCreditPaymentRequest request);
}
