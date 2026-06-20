package com.store.vitrine3d.infrastructure.config;

import com.store.vitrine3d.infrastructure.security.JwtAuthenticationEntryPoint;
import com.store.vitrine3d.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                    JwtAuthenticationEntryPoint entryPoint) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Autenticação
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // Cadastro público
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                // Perfis públicos de loja — apenas um nível de path para não expor rotas futuras
                .requestMatchers(HttpMethod.GET, "/api/users/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/users/store/*").permitAll()
                // Vitrine pública
                .requestMatchers(HttpMethod.GET, "/api/products/store/*/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/store/*/featured").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products/*/whatsapp-click").permitAll()
                // Localização
                .requestMatchers(HttpMethod.GET, "/api/locations/**").permitAll()
                // Vitrine pública — busca com filtros
                .requestMatchers(HttpMethod.GET, "/api/products/store/*/search").permitAll()
                // Categorias e Materiais (leitura pública)
                .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/store/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/materials").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/materials/store/**").permitAll()
                // Documentação
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Tudo mais requer autenticação
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
