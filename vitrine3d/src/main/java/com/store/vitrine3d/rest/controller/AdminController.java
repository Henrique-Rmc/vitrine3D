package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.service.impl.AdminService;
import com.store.vitrine3d.rest.dto.AdminStoreFilter;
import com.store.vitrine3d.rest.dto.AdminStoreResponse;
import com.store.vitrine3d.rest.dto.PageResponse;
import com.store.vitrine3d.rest.dto.PlatformStatsResponse;
import com.store.vitrine3d.rest.dto.SubscriptionAdminUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Admin", description = "Painel administrativo — requer ROLE_ADMIN")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Lista todas as lojas com filtros opcionais")
    @GetMapping("/stores")
    public PageResponse<AdminStoreResponse> listStores(
            @ModelAttribute AdminStoreFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(adminService.listStores(filter, page, size), r -> r);
    }

    @Operation(summary = "Detalhe completo de uma loja")
    @GetMapping("/stores/{id}")
    public ResponseEntity<AdminStoreResponse> getStore(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getStore(id));
    }

    @Operation(summary = "Ativa ou inativa uma loja")
    @PatchMapping("/stores/{id}/toggle-active")
    public ResponseEntity<AdminStoreResponse> toggleActive(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.toggleActive(id));
    }

    @Operation(summary = "Exclui uma loja e seus dados")
    @DeleteMapping("/stores/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable UUID id) {
        adminService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Altera plano/status/datas da assinatura de uma loja")
    @PutMapping("/stores/{id}/subscription")
    public ResponseEntity<AdminStoreResponse> updateSubscription(
            @PathVariable UUID id,
            @RequestBody SubscriptionAdminUpdateRequest req) {
        return ResponseEntity.ok(adminService.updateSubscription(id, req));
    }

    @Operation(summary = "Estende o trial de uma loja (body: { \"days\": N })")
    @PostMapping("/stores/{id}/subscription/extend")
    public ResponseEntity<AdminStoreResponse> extendTrial(
            @PathVariable UUID id,
            @RequestBody Map<String, Integer> body) {
        int days = body.getOrDefault("days", 0);
        return ResponseEntity.ok(adminService.extendTrial(id, days));
    }

    @Operation(summary = "Totais da plataforma")
    @GetMapping("/stats")
    public ResponseEntity<PlatformStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
