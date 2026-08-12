package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.FlowType;
import com.store.vitrine3d.domain.model.PdvCashFlow;
import com.store.vitrine3d.domain.model.PdvEmployee;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.PdvCashFlowRepository;
import com.store.vitrine3d.domain.repository.PdvEmployeeRepository;
import com.store.vitrine3d.domain.service.PdvCashFlowService;
import com.store.vitrine3d.rest.dto.PdvCashFlowRequest;
import com.store.vitrine3d.rest.dto.PdvCashFlowSummaryResponse;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class PdvCashFlowServiceImpl implements PdvCashFlowService {

    private final PdvCashFlowRepository cashFlowRepository;
    private final PdvEmployeeRepository employeeRepository;

    public PdvCashFlowServiceImpl(PdvCashFlowRepository cashFlowRepository,
                                   PdvEmployeeRepository employeeRepository) {
        this.cashFlowRepository = cashFlowRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public PdvCashFlow addEntry(Store store, PdvCashFlowRequest req) {
        if (isDuplicate(store.getId(), req.getOfflineId())) {
            throw new BusinessRuleException("DUPLICATE_CASH_FLOW", "Lançamento já registrado com este offlineId");
        }

        PdvCashFlow flow = new PdvCashFlow();
        flow.setOfflineId(req.getOfflineId());
        flow.setStore(store);
        flow.setType(req.getType());
        flow.setCategory(req.getCategory());
        flow.setAmount(req.getAmount());
        flow.setDescription(req.getDescription());
        flow.setFlowDate(req.getFlowDate());
        if (req.getOperatorId() != null) {
            PdvEmployee operator = employeeRepository.findByIdAndStoreId(req.getOperatorId(), store.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário", req.getOperatorId().toString()));
            flow.setOperator(operator);
        }

        return cashFlowRepository.save(flow);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PdvCashFlow> listEntries(UUID storeId, Instant from, Instant to, int page, int size) {
        return cashFlowRepository.findByStoreIdAndFlowDateBetweenOrderByFlowDateDesc(
                storeId, from, to, PageRequest.of(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public PdvCashFlowSummaryResponse getSummary(UUID storeId, Instant from, Instant to) {
        BigDecimal totalIn = cashFlowRepository.sumAmountByTypeAndPeriod(storeId, FlowType.IN, from, to);
        BigDecimal totalOut = cashFlowRepository.sumAmountByTypeAndPeriod(storeId, FlowType.OUT, from, to);
        long countIn = cashFlowRepository.countByTypeAndPeriod(storeId, FlowType.IN, from, to);
        long countOut = cashFlowRepository.countByTypeAndPeriod(storeId, FlowType.OUT, from, to);
        return new PdvCashFlowSummaryResponse(totalIn, totalOut, totalIn.subtract(totalOut), countIn, countOut);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDuplicate(UUID storeId, String offlineId) {
        return cashFlowRepository.existsByStoreIdAndOfflineId(storeId, offlineId);
    }
}
