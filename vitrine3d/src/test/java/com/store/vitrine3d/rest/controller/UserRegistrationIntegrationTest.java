package com.store.vitrine3d.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.infrastructure.storage.StorageService;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:integrationdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.rate-limit.enabled=false"
})
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StoreRepository storeRepository;

    @MockitoBean
    private StorageService storageService;

    @Test
    void whenRegisterUser_thenSavedInDatabase() throws Exception {
        StoreRegisterRequest request = new StoreRegisterRequest();
        request.setEmail("integration@vitrine3d.com");
        request.setPassword("senha123");
        request.setUserName("Integration User");
        request.setStoreName("Integration Store");
        request.setWhatsappNumber("5511999999999");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@vitrine3d.com"))
                .andExpect(jsonPath("$.slug").value("integration-store"));

        var saved = storeRepository.findByEmail("integration@vitrine3d.com");
        assertTrue(saved.isPresent(), "Store must be persisted in the database");
        assertEquals("Integration Store", saved.get().getStoreName());
        assertEquals("integration-store", saved.get().getSlug());
        assertNotNull(saved.get().getId());
        assertNotEquals("senha123", saved.get().getPassword(), "Password must be BCrypt-hashed");
    }
}
