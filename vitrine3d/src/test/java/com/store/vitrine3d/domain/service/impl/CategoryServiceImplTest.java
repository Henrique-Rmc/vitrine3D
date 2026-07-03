package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.CategoryRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Teste de caracterizacao: fixa o comportamento de AbstractStoreMetadataService
 * (herdado por CategoryServiceImpl), incluindo a checagem de posse entre lojas.
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CurrentStoreResolver currentStoreResolver;

    @InjectMocks private CategoryServiceImpl categoryService;

    private static final UUID STORE_ID = UUID.randomUUID();
    private static final String OWNER_EMAIL = "owner@test.com";

    private Store ownerStore;

    @BeforeEach
    void setUp() {
        ownerStore = new Store();
        ownerStore.setId(STORE_ID);
        ownerStore.setEmail(OWNER_EMAIL);
    }

    @Test
    void findPublic_returnsGlobalCategories() {
        Category global = new Category();
        global.setIsGlobal(true);
        when(categoryRepository.findByIsGlobalTrue()).thenReturn(List.of(global));

        assertThat(categoryService.findPublic()).containsExactly(global);
    }

    @Test
    void findByStore_returnsGlobalAndStoreCategories() {
        Category storeCategory = new Category();
        when(categoryRepository.findByIsGlobalTrueOrStoreId(STORE_ID)).thenReturn(List.of(storeCategory));

        assertThat(categoryService.findByStore(STORE_ID)).containsExactly(storeCategory);
    }

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_setsCurrentStoreAndPersistsAsNonGlobal() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.create("Animes");

        assertThat(result.getName()).isEqualTo("Animes");
        assertThat(result.getIsGlobal()).isFalse();
        assertThat(result.getStore()).isSameAs(ownerStore);
    }

    @Test
    void update_asOwner_updatesName() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setIsGlobal(false);
        existing.setStore(ownerStore);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.update(1L, "Novo Nome");

        assertThat(result.getName()).isEqualTo("Novo Nome");
    }

    @Test
    void update_notOwner_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());

        Category existing = new Category();
        existing.setId(1L);
        existing.setIsGlobal(false);
        existing.setStore(otherStore);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);

        assertThatThrownBy(() -> categoryService.update(1L, "Novo Nome"))
                .isInstanceOf(AccessDeniedException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void update_globalCategory_throwsAccessDenied() {
        Category global = new Category();
        global.setId(1L);
        global.setIsGlobal(true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(global));

        assertThatThrownBy(() -> categoryService.update(1L, "Novo Nome"))
                .isInstanceOf(AccessDeniedException.class);
        verify(categoryRepository, never()).save(any());
        verify(currentStoreResolver, never()).getCurrentStore();
    }

    @Test
    void delete_asOwner_deletesCategory() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setIsGlobal(false);
        existing.setStore(ownerStore);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);

        categoryService.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_notOwner_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());

        Category existing = new Category();
        existing.setId(1L);
        existing.setIsGlobal(false);
        existing.setStore(otherStore);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(AccessDeniedException.class);
        verify(categoryRepository, never()).deleteById(any());
    }
}
