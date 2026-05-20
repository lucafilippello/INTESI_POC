package com.intesi.userservice.dto;

import com.intesi.userservice.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserCreateRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String taxCode;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotEmpty
    private Set<RoleName> roles;
}
