package com.intesi.userservice.controller;

import com.intesi.userservice.config.JwtAuthConverter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Import(JwtAuthConverter.class)
@EnableMethodSecurity  // abilita @PreAuthorize sui metodi del controller
@Profile("test")  // attiva questa configurazione solo durante i test
public class TestSecurityConfig {

    // Rimpiazzo JwtDecoder (utilizzato durante il processo di autenticazione per decodificare il token JWT) con un mock che non fa nulla
    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }

    @Bean
    public SecurityFilterChain testFilterChain(HttpSecurity http, JwtAuthConverter jwtAuthConverter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // i permessi sui singoli endpoint sono gestiti via @PreAuthorize sul controller
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            );
        return http.build();
    }
}
