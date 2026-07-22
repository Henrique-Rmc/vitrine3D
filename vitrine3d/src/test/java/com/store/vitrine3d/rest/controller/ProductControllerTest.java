package com.store.vitrine3d.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.ProductService;
import com.store.vitrine3d.infrastructure.security.JwtTokenProvider;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.exception.GlobalExceptionHandler;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@org.springframework.test.context.TestPropertySource(properties = "app.rate-limit.enabled=false")
class ProductControllerTest {

    private static final UUID STORE_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ProductService productService;
    @MockitoBean private StoreRepository storeRepository;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    private Store buildMockStore() {
        Store store = new Store();
        store.setId(STORE_UUID);
        store.setStoreName("Loja 3D");
        store.setWhatsappNumber("5511999999999");
        store.setIsActive(true);
        return store;
    }

    private Product buildMockProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Goku SSJ3");
        product.setDescription("Figura articulada em PLA");
        product.setIsVisible(true);
        product.setFeatured(false);
        product.setStore(buildMockStore());
        return product;
    }

    // --- GET /api/products/store/{id}/public ---

    @Test
    void whenListPublicProducts_thenReturns200WithPagedList() throws Exception {
        var page = new PageImpl<>(List.of(buildMockProduct()), PageRequest.of(0, 15), 1);
        when(productService.findVisibleByStoreId(STORE_UUID, 0, 15)).thenReturn(page);

        mockMvc.perform(get("/api/products/store/" + STORE_UUID + "/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Goku SSJ3"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].whatsappUrl").value(
                        org.hamcrest.Matchers.containsString("wa.me/5511999999999")));
    }

    @Test
    void whenListPublicProductsForEmptyStore_thenReturns200WithEmptyPage() throws Exception {
        var emptyUuid = UUID.randomUUID();
        var page = new PageImpl<Product>(List.of(), PageRequest.of(0, 15), 0);
        when(productService.findVisibleByStoreId(emptyUuid, 0, 15)).thenReturn(page);

        mockMvc.perform(get("/api/products/store/" + emptyUuid + "/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // --- GET /api/products/{id} ---

    @Test
    void whenGetProductById_thenReturns200WithClickCount() throws Exception {
        when(productService.findById(1L)).thenReturn(buildMockProduct());
        when(productService.getClickCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Goku SSJ3"))
                .andExpect(jsonPath("$.clickCount").value(5));
    }

    @Test
    void whenGetProductByNonExistentId_thenReturns404() throws Exception {
        when(productService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Produto", 99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // --- GET /api/products/store/{id} ---

    @Test
    void whenListAllProductsByStore_thenReturnsPagedResults() throws Exception {
        Product hidden = buildMockProduct();
        hidden.setIsVisible(false);
        var page = new PageImpl<>(List.of(buildMockProduct(), hidden), PageRequest.of(0, 15), 2);
        when(productService.findByStoreId(STORE_UUID, 0, 15)).thenReturn(page);

        mockMvc.perform(get("/api/products/store/" + STORE_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    // --- POST /api/products ---

    @Test
    void whenCreateProductWithValidMultipart_thenReturns201() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Pikachu");
        request.setStoreId(STORE_UUID);
        request.setProductTypeId(1L);

        MockMultipartFile dataJson = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));

        when(productService.save(any(ProductCreateRequest.class), isNull()))
                .thenReturn(buildMockProduct());

        mockMvc.perform(multipart("/api/products")
                        .file(dataJson)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Goku SSJ3"));
    }

    // --- DELETE /api/products/{id} ---

    @Test
    void whenDeleteProduct_thenReturns204() throws Exception {
        mockMvc.perform(delete("/api/products/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
