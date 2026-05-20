package com.intesi.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequestDto {

    @NotBlank
    private String username;

    private String taxCode;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;
}
