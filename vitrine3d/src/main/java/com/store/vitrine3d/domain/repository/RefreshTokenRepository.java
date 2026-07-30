package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.store.id = :storeId")
    void revokeAllByStoreId(@Param("storeId") UUID storeId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}
