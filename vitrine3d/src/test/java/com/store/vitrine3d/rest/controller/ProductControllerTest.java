package com.store.vitrine3d.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.model.Category;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.MakerWorldScraperService;
import com.store.vitrine3d.domain.service.ProductService;
import com.store.vitrine3d.infrastructure.security.JwtTokenProvider;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.exception.GlobalExceptionHandler;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private MakerWorldScraperService scraperService;

    @MockitoBean
    private StoreRepository storeRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private Store buildMockStore() {
        Store store = new Store();
        store.setId(1L);
        store.setStoreName("Loja 3D");
        store.setWhatsappNumber("5511999999999");
        store.setIsActive(true);
        return store;
    }

    private Category buildMockCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Animes");
        category.setIsGlobal(true);
        return category;
    }

    private Product buildMockProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Goku SSJ3");
        product.setDescription("Figura articulada em PLA");
        product.setMaterial("PLA");
        product.setMulticolor(false);
        product.setDimensions("20x15x10cm");
        product.setIsVisible(true);
        product.setCategory(buildMockCategory());
        product.setStore(buildMockStore());
        return product;
    }

    // --- GET /api/products/store/{id}/public ---

    @Test
    void whenListPublicProducts_thenReturns200WithProductList() throws Exception {
        when(productService.findVisibleByStoreId(1L)).thenReturn(List.of(buildMockProduct()));

        mockMvc.perform(get("/api/products/store/1/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Goku SSJ3"))
                .andExpect(jsonPath("$[0].categoryName").value("Animes"))
                .andExpect(jsonPath("$[0].whatsappUrl").value(
                        org.hamcrest.Matchers.containsString("wa.me/5511999999999")));
    }

    @Test
    void whenListPublicProductsForEmptyStore_thenReturns200WithEmptyList() throws Exception {
        when(productService.findVisibleByStoreId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/products/store/99/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/products/{id} ---

    @Test
    void whenGetProductById_thenReturns200() throws Exception {
        when(productService.findById(1L)).thenReturn(buildMockProduct());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Goku SSJ3"))
                .andExpect(jsonPath("$.categoryName").value("Animes"))
                .andExpect(jsonPath("$.storeId").value(1));
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
    void whenListAllProductsByStore_thenReturnsAllIncludingHidden() throws Exception {
        Product hidden = buildMockProduct();
        hidden.setIsVisible(false);
        when(productService.findByStoreId(1L)).thenReturn(List.of(buildMockProduct(), hidden));

        mockMvc.perform(get("/api/products/store/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // --- POST /api/products ---

    @Test
    void whenCreateProductWithValidMultipart_thenReturns201() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Pikachu");
        request.setMulticolor(true);
        request.setCategoryId(1L);
        request.setStoreId(1L);

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
