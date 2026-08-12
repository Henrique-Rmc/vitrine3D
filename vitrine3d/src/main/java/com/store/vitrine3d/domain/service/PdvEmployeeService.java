package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.PdvEmployee;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.rest.dto.PdvEmployeeRequest;

import java.util.List;
import java.util.UUID;

public interface PdvEmployeeService {
    PdvEmployee create(Store store, PdvEmployeeRequest req);
    List<PdvEmployee> list(UUID storeId);
    PdvEmployee update(UUID storeId, UUID employeeId, PdvEmployeeRequest req);
    void delete(UUID storeId, UUID employeeId);
    PdvEmployee verifyPin(UUID storeId, UUID employeeId, String pin);
}
