package com.intesi.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            )
            .authorizeHttpRequests(auth -> auth
                // accesso alla console db senza autenticazione
                .requestMatchers("/h2-console/**").permitAll()
                // accesso a Swagger UI e spec OpenAPI senza autenticazione
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // i permessi sui singoli endpoint /users/** sono gestiti singolarmente via @PreAuthorize nel controller
                .anyRequest().authenticated()
            )
            // necessario per estrarre ruolo/autorizzazioni dal token Keycloak ed inserirle in un oggetto Authentication di Spring
            .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            )
            // necessario per visualizzare H2 console nel browser (usa frames)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
