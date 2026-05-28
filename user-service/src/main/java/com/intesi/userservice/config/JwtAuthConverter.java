package com.intesi.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Estrae dal JWT Keycloak le seguenti informazioni per costruire un token di autenticazione Spring Security:
 *
 * 1. realm_access.roles (RUOLO: uno fra ADMIN, OPERATOR, USER)
 *    Mappato come ROLE_<NOME> — usato per dicriminare quali campi mostrare nella response
 *
 * 2. resource_access.<client-id>.roles (PERMESSI/AUTORIZZAZIONI: read_user, create_user, update_user, delete_user)
 *    Mappati senza prefisso — usati per il controllo degli endpoint HTTP tramite @PreAuthorize annotation
 */
@Component
@Setter
@Getter
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.addAll(extractRealmRoles(jwt));
        authorities.addAll(extractResourcePermissions(jwt));
        return new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username"));
    }

    /**
     * Legge realm_access.roles e li mappa come ROLE_<NOME>.
     * Esempio: "ADMIN" --> "ROLE_ADMIN"
     */
    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Set.of();
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }

    /**
     * Legge resource_access.<client-id>.roles e li mappa senza prefisso.
     * Esempio: "read_user" --> "read_user"
     */
    @SuppressWarnings("unchecked")
    private Set<GrantedAuthority> extractResourcePermissions(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null || !resourceAccess.containsKey(clientId)) {
            return Set.of();
        }
        Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
        if (!clientAccess.containsKey("roles")) {
            return Set.of();
        }
        List<String> permissions = (List<String>) clientAccess.get("roles");
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
