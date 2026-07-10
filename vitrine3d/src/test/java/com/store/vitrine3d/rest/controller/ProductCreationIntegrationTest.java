package com.store.vitrine3d.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.infrastructure.storage.StorageService;
import com.store.vitrine3d.rest.dto.ProductCreateRequest;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:productdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.rate-limit.enabled=false"
})
class ProductCreationIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProductRepository productRepository;

    @MockitoBean
    private StorageService storageService;

    private String jwtToken;
    private UUID storeId;

    @BeforeEach
    void setUp() throws Exception {
        when(storageService.uploadFile(any())).thenReturn("http://minio/test/image.png");

        // 1 — registra loja
        StoreRegisterRequest registerRequest = new StoreRegisterRequest();
        registerRequest.setEmail("produto@vitrine3d.com");
        registerRequest.setPassword("senha123");
        registerRequest.setUserName("Loja Teste");
        registerRequest.setStoreName("Vitrine 3D Teste");
        registerRequest.setWhatsappNumber("5511999999999");

        MvcResult registerResult = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        storeId = UUID.fromString(objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("id").asText());

        // 2 — login, captura token
        String loginBody = """
                {"email":"produto@vitrine3d.com","password":"senha123"}
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    void whenCreateProduct_thenSavedInDatabase() throws Exception {
        ProductCreateRequest productRequest = new ProductCreateRequest();
        productRequest.setName("Goku SSJ4");
        productRequest.setDescription("Figura articulada em resina");
        productRequest.setStoreId(storeId);

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(productRequest));

        MvcResult result = mockMvc.perform(multipart("/api/products")
                        .file(dataPart)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Goku SSJ4"))
                .andExpect(jsonPath("$.isVisible").value(true))
                .andExpect(jsonPath("$.storeId").value(storeId.toString()))
                .andReturn();

        Long productId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        assertTrue(productRepository.findById(productId).isPresent(), "Product must be in database");
        assertEquals("Goku SSJ4", productRepository.findById(productId).get().getName());
    }

    @Test
    void whenCreateProductWithoutAuth_thenReturns401() throws Exception {
        ProductCreateRequest productRequest = new ProductCreateRequest();
        productRequest.setName("Pikachu");
        productRequest.setStoreId(storeId);

        MockMultipartFile dataPart = new MockMultipartFile(
                "data", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(productRequest));

        mockMvc.perform(multipart("/api/products").file(dataPart))
                .andExpect(status().isUnauthorized());
    }
}
