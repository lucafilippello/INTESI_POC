package com.intesi.userservice.controller;

import com.intesi.userservice.dto.ActivityLogDto;
import com.intesi.userservice.model.ActivityType;
import com.intesi.userservice.service.LogService;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/logs")
@RequiredArgsConstructor
@Tag(name = "Activity Logs", description = "Consultazione dei log di attività (solo per ruolo ADMIN)")
public class ActivityLogController {

    private final LogService logService;

    @Operation(summary = "Restituisce tutti i log di attività")
    @ApiResponse(responseCode = "200", description = "Lista completa dei log")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public List<ActivityLogDto> getAll() {
        return logService.findAll();
    }

    @Operation(summary = "Filtra i log per utente")
    @ApiResponse(responseCode = "200", description = "Log trovati per l'utente specificato (lista vuota se nessuno)")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/user/{username}")
    public List<ActivityLogDto> getByLoggedUser(
            @Parameter(description = "Username Keycloak dell'utente") @PathVariable String username) {
        return logService.findByLoggedUser(username);
    }

    @Operation(summary = "Filtra i log per tipo di attività")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Log trovati per il tipo specificato (lista vuota se nessuno)"),
        @ApiResponse(responseCode = "400", description = "Tipo attività non valido")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/type/{type}")
    public List<ActivityLogDto> getByActivityType(
            @Parameter(description = "Tipo attività: USER_CREATION, USER_UPDATE, USER_DELETION, ROLE_ADD, ROLE_REMOVAL")
            @PathVariable ActivityType type) {
        return logService.findByActivityType(type);
    }
}
