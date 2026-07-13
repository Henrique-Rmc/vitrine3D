package com.store.vitrine3d.rest.controller;

import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.infrastructure.health.HealthCheckService;
import com.store.vitrine3d.infrastructure.security.JwtTokenProvider;
import com.store.vitrine3d.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthCheckService healthCheckService;

    @MockitoBean
    private StoreRepository storeRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void health_allComponentsUp_returns200() throws Exception {
        when(healthCheckService.checkComponents()).thenReturn(Map.of("database", "UP", "redis", "UP"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.database").value("UP"))
                .andExpect(jsonPath("$.components.redis").value("UP"));
    }

    @Test
    void health_componentDown_returns503() throws Exception {
        when(healthCheckService.checkComponents()).thenReturn(Map.of("database", "DOWN", "redis", "UP"));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.database").value("DOWN"));
    }
}
