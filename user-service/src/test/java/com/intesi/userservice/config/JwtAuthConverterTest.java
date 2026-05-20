package com.intesi.userservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthConverterTest {

    private final JwtAuthConverter converter = new JwtAuthConverter();

    @BeforeEach
    void setUp() {
    	converter.setClientId("demo-task");
    }

    @Test
    void convert_insertRightData_forAdminUser() {
        Jwt jwt = buildJwt(Map.of(
        		"preferred_username", "admin_user",
                "realm_access", Map.of("roles", List.of("ADMIN")),
                "resource_access",
                Map.of(
                	converter.getClientId(), 
                	Map.of("roles", List.of("read_user", "create_user", "update_user", "delete_user")
                ))
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);
        
        assertThat(token.getName()).isEqualTo("admin_user");
        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN","read_user", "create_user", "update_user", "delete_user");
    }
    
    @Test
    void convert_insertRightData_forCreatorUser() {
        Jwt jwt = buildJwt(Map.of(
        		"preferred_username", "creator_user",
                "realm_access", Map.of("roles", List.of("OPERATOR")),
                "resource_access",
                Map.of(
                	converter.getClientId(), 
                	Map.of("roles", List.of("read_user", "create_user", "update_user")
                ))
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);
        
        assertThat(token.getName()).isEqualTo("creator_user");
        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_OPERATOR","read_user", "create_user", "update_user");
    }

    @Test
    void convert_insertRightData_forReaderUser() {
        Jwt jwt = buildJwt(Map.of(
        		"preferred_username", "reader_user",
                "realm_access", Map.of("roles", List.of("USER")),
                "resource_access",
                Map.of(
                	converter.getClientId(), 
                	Map.of("roles", List.of("read_user")
                ))
        ));

        AbstractAuthenticationToken token = converter.convert(jwt);
        
        assertThat(token.getName()).isEqualTo("reader_user");
        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER","read_user");
    }
    
    //Utility per costruire un JWT di test con i claims passati in input
    private Jwt buildJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claims(c -> c.putAll(claims))
                .build();
    }
}
