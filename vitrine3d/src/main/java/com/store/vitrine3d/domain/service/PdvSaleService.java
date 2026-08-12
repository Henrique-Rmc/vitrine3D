package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.PdvSale;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.rest.dto.PdvSaleRequest;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.UUID;

public interface PdvSaleService {
    PdvSale createSale(Store store, PdvSaleRequest request);
    Page<PdvSale> listSales(UUID storeId, Instant from, Instant to, int page, int size);
    PdvSale getSale(UUID storeId, UUID saleId);
    PdvSale cancelSale(UUID storeId, UUID saleId);
    boolean isDuplicate(UUID storeId, String offlineId);
}
