package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.rest.dto.PdvBalanceResponse;

import java.time.Instant;
import java.util.UUID;

public interface PdvBalanceService {
    PdvBalanceResponse getBalance(UUID storeId, Instant from, Instant to);
}
