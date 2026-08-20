package com.store.vitrine3d.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseWarmupRetryFilterTest {

    // Backoff de 1ms — testa a logica de retry sem esperar os 500ms/1500ms reais de producao.
    private final DatabaseWarmupRetryFilter filter = new DatabaseWarmupRetryFilter(new long[] {1, 1});

    @Test
    void succeedsOnFirstAttempt_doesNotRetry() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        FilterChain chain = (req, res) -> {
            calls.incrementAndGet();
            writeJson((HttpServletResponse) res, 200, "{\"ok\":true}");
        };

        MockHttpServletRequest request = jsonRequest("{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(calls.get()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo("{\"ok\":true}");
    }

    @Test
    void databaseWarmingUp_retriesAndEventuallySucceeds() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        List<String> bodiesSeen = new ArrayList<>();
        FilterChain chain = (req, res) -> {
            int attempt = calls.incrementAndGet();
            bodiesSeen.add(new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            if (attempt < 3) {
                writeJson((HttpServletResponse) res, 503,
                        "{\"code\":\"DATABASE_WARMING_UP\",\"message\":\"retomando\"}");
            } else {
                writeJson((HttpServletResponse) res, 200, "{\"ok\":true}");
            }
        };

        MockHttpServletRequest request = jsonRequest("{\"label\":\"Perfumes\"}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(calls.get()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo("{\"ok\":true}");
        assertThat(bodiesSeen).containsExactly(
                "{\"label\":\"Perfumes\"}", "{\"label\":\"Perfumes\"}", "{\"label\":\"Perfumes\"}");
    }

    @Test
    void nonRetryableFailure_doesNotRetry() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        FilterChain chain = (req, res) -> {
            calls.incrementAndGet();
            writeJson((HttpServletResponse) res, 422, "{\"code\":\"BUSINESS_RULE\",\"message\":\"nao rola\"}");
        };

        MockHttpServletRequest request = jsonRequest("{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(calls.get()).isEqualTo(1);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    void databaseWarmingUp_exhaustsRetries_returnsLastFailure() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        FilterChain chain = (req, res) -> {
            calls.incrementAndGet();
            writeJson((HttpServletResponse) res, 503,
                    "{\"code\":\"DATABASE_WARMING_UP\",\"message\":\"retomando\"}");
        };

        MockHttpServletRequest request = jsonRequest("{}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertThat(calls.get()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(503);
    }

    private MockHttpServletRequest jsonRequest(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/products/store/x/product-types/1");
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        request.setContentType("application/json");
        return request;
    }

    private void writeJson(HttpServletResponse response, int status, String body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(body);
        response.getWriter().flush();
    }
}
