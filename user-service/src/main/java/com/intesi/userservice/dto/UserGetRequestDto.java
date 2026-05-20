package com.intesi.userservice.dto;

import com.intesi.userservice.model.RoleName;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserGetRequestDto {

    private Long id;

    private String email;

    private Set<RoleName> roles;
}
