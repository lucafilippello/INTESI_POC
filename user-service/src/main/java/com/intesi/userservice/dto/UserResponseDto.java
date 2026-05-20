package com.intesi.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.intesi.userservice.model.RoleName;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String taxCode;      // null se il chiamante non ha visibilità
    private String name;
    private String surname;
    private Set<RoleName> roles; // null se il chiamante non ha visibilità
}
