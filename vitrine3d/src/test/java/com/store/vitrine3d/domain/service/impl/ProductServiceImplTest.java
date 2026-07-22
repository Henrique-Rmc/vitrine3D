package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.WhatsappClick;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.ProductTypeRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.repository.WhatsappClickRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.infrastructure.storage.StorageService;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.dto.ProductUpdateRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private ProductTypeRepository productTypeRepository;
    @Mock private StorageService storageService;
    @Mock private WhatsappClickRepository whatsappClickRepository;
    @Mock private CurrentStoreResolver currentStoreResolver;
    @Mock private ProductAttributeValidator attributeValidator;
    @Mock private ProductAttributeFilterBuilder attributeFilterBuilder;

    @InjectMocks private ProductServiceImpl productService;

    private static final UUID STORE_ID = UUID.randomUUID();
    private static final String OWNER_EMAIL = "owner@test.com";

    private Store ownerStore;

    @BeforeEach
    void setUp() {
        ownerStore = new Store();
        ownerStore.setId(STORE_ID);
        ownerStore.setEmail(OWNER_EMAIL);
        ownerStore.setWhatsappNumber("5511999999999");
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Test
    void save_withPrice_persistsPrice() {
        ProductCreateRequest request = buildCreateRequest();
        request.setPrice(new BigDecimal("49.90"));

        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(ownerStore));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(attributeValidator.validateForCreate(any(), any(), any())).thenReturn(java.util.Map.of());
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.save(request, null);

        assertThat(result.getPrice()).isEqualByComparingTo("49.90");
    }

    @Test
    void save_withoutPrice_priceIsNull() {
        ProductCreateRequest request = buildCreateRequest();

        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(ownerStore));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(attributeValidator.validateForCreate(any(), any(), any())).thenReturn(java.util.Map.of());
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.save(request, null);

        assertThat(result.getPrice()).isNull();
    }

    @Test
    void save_withDifferentStore_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());

        ProductCreateRequest request = buildCreateRequest();
        request.setStoreId(otherStore.getId());

        when(storeRepository.findById(otherStore.getId())).thenReturn(Optional.of(otherStore));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);

        assertThatThrownBy(() -> productService.save(request, null))
                .isInstanceOf(AccessDeniedException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void save_withImage_uploadsAndSetsUrl() {
        ProductCreateRequest request = buildCreateRequest();
        var image = mock(org.springframework.web.multipart.MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);

        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(ownerStore));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(attributeValidator.validateForCreate(any(), any(), any())).thenReturn(java.util.Map.of());
        when(storageService.uploadFile(image)).thenReturn("http://minio/test.png");
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.save(request, java.util.List.of(image));

        assertThat(result.getImageUrls()).containsExactly("http://minio/test.png");
    }

    @Test
    void save_withProductType_ownedByStore_setsProductType() {
        ProductCreateRequest request = buildCreateRequest();
        request.setProductTypeId(50L);

        com.store.vitrine3d.domain.model.ProductType camisa = new com.store.vitrine3d.domain.model.ProductType();
        camisa.setId(50L);
        camisa.setStore(ownerStore);

        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(ownerStore));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productTypeRepository.findById(50L)).thenReturn(Optional.of(camisa));
        when(attributeValidator.validateForCreate(any(), eq(50L), any())).thenReturn(java.util.Map.of());
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.save(request, null);

        assertThat(result.getProductType()).isSameAs(camisa);
    }

    @Test
    void save_withProductTypeFromAnotherStore_throwsAccessDenied() {
        ProductCreateRequest request = buildCreateRequest();
        request.setProductTypeId(50L);

        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        com.store.vitrine3d.domain.model.ProductType camisa = new com.store.vitrine3d.domain.model.ProductType();
        camisa.setId(50L);
        camisa.setStore(otherStore);

        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(ownerStore));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productTypeRepository.findById(50L)).thenReturn(Optional.of(camisa));

        assertThatThrownBy(() -> productService.save(request, null))
                .isInstanceOf(AccessDeniedException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void save_withMoreThanFiveImages_throwsBusinessRuleException() {
        ProductCreateRequest request = buildCreateRequest();
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(ownerStore));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);

        var images = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> {
                    var file = mock(org.springframework.web.multipart.MultipartFile.class);
                    when(file.isEmpty()).thenReturn(false);
                    return file;
                })
                .toList();

        assertThatThrownBy(() -> productService.save(request, images))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("5");
        verify(productRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_withNewPrice_updatesPrice() {
        Product existing = buildProduct(new BigDecimal("20.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setPrice(new BigDecimal("99.99"));

        Product result = productService.update(1L, request, null);

        assertThat(result.getPrice()).isEqualByComparingTo("99.99");
    }

    @Test
    void update_withNullPrice_doesNotClearExistingPrice() {
        Product existing = buildProduct(new BigDecimal("20.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductUpdateRequest request = new ProductUpdateRequest();

        Product result = productService.update(1L, request, null);

        assertThat(result.getPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void update_notOwner_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());

        Product product = buildProduct(null);
        product.setStore(otherStore);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);

        assertThatThrownBy(() -> productService.update(1L, new ProductUpdateRequest(), null))
                .isInstanceOf(AccessDeniedException.class);
        verify(productRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // toggleFeatured
    // -------------------------------------------------------------------------

    @Test
    void toggleFeatured_whenFalseAndBelowLimit_setsTrueAndSaves() {
        Product product = buildProduct(null);
        product.setFeatured(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productRepository.countByStoreIdAndFeaturedTrue(STORE_ID)).thenReturn(1L);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.toggleFeatured(1L);

        assertThat(result.getFeatured()).isTrue();
    }

    @Test
    void toggleFeatured_whenAtMaxLimit_throwsIllegalArgument() {
        Product product = buildProduct(null);
        product.setFeatured(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productRepository.countByStoreIdAndFeaturedTrue(STORE_ID)).thenReturn(5L);

        assertThatThrownBy(() -> productService.toggleFeatured(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("5");
        verify(productRepository, never()).save(any());
    }

    @Test
    void toggleFeatured_whenTrue_setsFalseRegardlessOfCount() {
        Product product = buildProduct(null);
        product.setFeatured(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.toggleFeatured(1L);

        assertThat(result.getFeatured()).isFalse();
        verify(productRepository, never()).countByStoreIdAndFeaturedTrue(any());
    }

    // -------------------------------------------------------------------------
    // toggleVisibility
    // -------------------------------------------------------------------------

    @Test
    void toggleVisibility_whenVisible_setsInvisible() {
        Product product = buildProduct(null);
        product.setIsVisible(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.toggleVisibility(1L);

        assertThat(result.getIsVisible()).isFalse();
    }

    @Test
    void toggleVisibility_whenInvisible_setsVisible() {
        Product product = buildProduct(null);
        product.setIsVisible(false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.toggleVisibility(1L);

        assertThat(result.getIsVisible()).isTrue();
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void delete_asOwner_deletesProduct() {
        Product product = buildProduct(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_notOwner_throwsAccessDenied() {
        Store other = new Store();
        other.setId(UUID.randomUUID());

        Product product = buildProduct(null);
        product.setStore(other);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentStoreResolver.getCurrentStore()).thenReturn(ownerStore);

        assertThatThrownBy(() -> productService.delete(1L))
                .isInstanceOf(AccessDeniedException.class);
        verify(productRepository, never()).deleteById(any());
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void findById_notFound_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // registerWhatsappClick
    // -------------------------------------------------------------------------

    @Test
    void registerWhatsappClick_savesClickAndReturnsCount() {
        Product product = buildProduct(null);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(whatsappClickRepository.countByProductId(1L)).thenReturn(7L);

        ArgumentCaptor<WhatsappClick> clickCaptor = ArgumentCaptor.forClass(WhatsappClick.class);

        long count = productService.registerWhatsappClick(1L);

        verify(whatsappClickRepository).save(clickCaptor.capture());
        assertThat(count).isEqualTo(7L);
        assertThat(clickCaptor.getValue().getProduct()).isSameAs(product);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private ProductCreateRequest buildCreateRequest() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("Goku SSJ");
        req.setStoreId(STORE_ID);
        return req;
    }

    private Product buildProduct(BigDecimal price) {
        Product p = new Product();
        p.setId(1L);
        p.setName("Goku SSJ");
        p.setIsVisible(true);
        p.setFeatured(false);
        p.setPrice(price);
        p.setStore(ownerStore);
        return p;
    }
}
