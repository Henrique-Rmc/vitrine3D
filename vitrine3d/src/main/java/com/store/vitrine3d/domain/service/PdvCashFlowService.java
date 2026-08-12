package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.PdvCashFlow;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.rest.dto.PdvCashFlowRequest;
import com.store.vitrine3d.rest.dto.PdvCashFlowSummaryResponse;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

public interface PdvCashFlowService {
    PdvCashFlow addEntry(Store store, PdvCashFlowRequest request);
    Page<PdvCashFlow> listEntries(UUID storeId, Instant from, Instant to, int page, int size);
    PdvCashFlowSummaryResponse getSummary(UUID storeId, Instant from, Instant to);
    boolean isDuplicate(UUID storeId, String offlineId);
}
