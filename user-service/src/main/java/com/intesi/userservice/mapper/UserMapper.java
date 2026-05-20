package com.intesi.userservice.mapper;

import com.intesi.userservice.dto.UserCreateRequestDto;
import com.intesi.userservice.dto.UserResponseDto;
import com.intesi.userservice.dto.UserUpdateRequestDto;
import com.intesi.userservice.model.Role;
import com.intesi.userservice.model.RoleName;
import com.intesi.userservice.model.User;
import org.mapstruct.*;

/**
 * Mapper utente.
 *
 * Tre metodi di conversione User → UserResponse in base al ruolo Keycloak
 * del chiamante (la scelta del metodo spetta al service):
 *   - toAdminResponse    → tutti i campi visibili
 *   - toOperatorResponse → taxCode nascosto
 *   - toUserResponse     → taxCode e roles nascosti
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    // User --> UserResponse
    @Mapping(target = "roles", source = "roles")
    UserResponseDto toAdminResponse(User user);

    @Mapping(target = "taxCode", ignore = true)
    @Mapping(target = "roles", source = "roles")
    UserResponseDto toOperatorResponse(User user);

    @Mapping(target = "taxCode", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserResponseDto toUserResponse(User user);

    // Conversione Role --> RoleName usata dai metodi sopra
    default RoleName roleToRoleName(Role role) {
        return role.getName();
    }

    // UserCreateRequest --> User
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)   // risolti dal service via RoleRepository
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreateRequestDto request);

    // UserUpdateRequest --> User (aggiorna i campi modificabili sull'entità esistente) ---
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)   // email non modificabile
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(UserUpdateRequestDto request, @MappingTarget User user);
}
