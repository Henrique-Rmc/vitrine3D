package com.store.vitrine3d.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.UserService;
import com.store.vitrine3d.infrastructure.security.JwtTokenProvider;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import com.store.vitrine3d.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
@org.springframework.test.context.TestPropertySource(properties = "app.rate-limit.enabled=false")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private StoreRepository storeRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private static final UUID STORE_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private Store buildMockStore() {
        Store store = new Store();
        store.setId(STORE_UUID);
        store.setEmail("loja@teste.com");
        store.setUserName("lojista");
        store.setStoreName("Loja 3D");
        store.setWhatsappNumber("5511999999999");
        store.setStoreDescription("Loja de impressão 3D");
        store.setIsActive(true);
        return store;
    }

    private StoreRegisterRequest buildValidRequest() {
        StoreRegisterRequest request = new StoreRegisterRequest();
        request.setEmail("loja@teste.com");
        request.setPassword("senha123");
        request.setUserName("lojista");
        request.setStoreName("Loja 3D");
        request.setWhatsappNumber("5511999999999");
        return request;
    }

    @Test
    void whenRegisterWithValidData_thenReturns201() throws Exception {
        when(userService.register(any(StoreRegisterRequest.class))).thenReturn(buildMockStore());

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(STORE_UUID.toString()))
                .andExpect(jsonPath("$.email").value("loja@teste.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void whenRegisterWithInvalidEmail_thenReturns400WithFieldDetails() throws Exception {
        StoreRegisterRequest invalidRequest = buildValidRequest();
        invalidRequest.setEmail("email-invalido");

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void whenRegisterWithShortPassword_thenReturns400WithFieldDetails() throws Exception {
        StoreRegisterRequest invalidRequest = buildValidRequest();
        invalidRequest.setPassword("123");

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void whenRegisterWithBlankStoreName_thenReturns400() throws Exception {
        StoreRegisterRequest invalidRequest = buildValidRequest();
        invalidRequest.setStoreName("");

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.storeName").exists());
    }

    @Test
    void whenGetByExistingId_thenReturns200() throws Exception {
        when(userService.findById(STORE_UUID)).thenReturn(Optional.of(buildMockStore()));

        mockMvc.perform(get("/api/users/" + STORE_UUID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeName").value("Loja 3D"));
    }

    @Test
    void whenGetByNonExistentId_thenReturns404() throws Exception {
        UUID missing = UUID.fromString("99999999-9999-9999-9999-999999999999");
        when(userService.findById(missing)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/" + missing))
                .andExpect(status().isNotFound());
    }
}
