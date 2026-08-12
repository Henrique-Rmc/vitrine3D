package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.PdvEmployee;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.PdvEmployeeRepository;
import com.store.vitrine3d.domain.service.PdvEmployeeService;
import com.store.vitrine3d.rest.dto.PdvEmployeeRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PdvEmployeeServiceImpl implements PdvEmployeeService {

    private final PdvEmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public PdvEmployeeServiceImpl(PdvEmployeeRepository employeeRepository,
                                   PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PdvEmployee create(Store store, PdvEmployeeRequest req) {
        if (req.getPin() == null || req.getPin().isBlank()) {
            throw new BusinessRuleException("PIN_REQUIRED", "PIN é obrigatório ao criar funcionário");
        }
        PdvEmployee employee = new PdvEmployee();
        employee.setStore(store);
        employee.setName(req.getName());
        employee.setPinHash(passwordEncoder.encode(req.getPin()));
        employee.setRole(req.getRole() != null ? req.getRole() : com.store.vitrine3d.domain.model.PdvEmployeeRole.CASHIER);
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PdvEmployee> list(UUID storeId) {
        return employeeRepository.findByStoreIdOrderByNameAsc(storeId);
    }

    @Override
    public PdvEmployee update(UUID storeId, UUID employeeId, PdvEmployeeRequest req) {
        PdvEmployee employee = require(storeId, employeeId);
        employee.setName(req.getName());
        if (req.getRole() != null) {
            employee.setRole(req.getRole());
        }
        if (req.getPin() != null && !req.getPin().isBlank()) {
            employee.setPinHash(passwordEncoder.encode(req.getPin()));
        }
        return employeeRepository.save(employee);
    }

    @Override
    public void delete(UUID storeId, UUID employeeId) {
        PdvEmployee employee = require(storeId, employeeId);
        employee.setIsActive(false);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PdvEmployee verifyPin(UUID storeId, UUID employeeId, String pin) {
        PdvEmployee employee = require(storeId, employeeId);
        if (!Boolean.TRUE.equals(employee.getIsActive())) {
            throw new BusinessRuleException("EMPLOYEE_INACTIVE", "Funcionário inativo");
        }
        if (!passwordEncoder.matches(pin, employee.getPinHash())) {
            throw new BusinessRuleException("INVALID_PIN", "PIN inválido");
        }
        return employee;
    }

    private PdvEmployee require(UUID storeId, UUID employeeId) {
        return employeeRepository.findByIdAndStoreId(employeeId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário", employeeId.toString()));
    }
}
