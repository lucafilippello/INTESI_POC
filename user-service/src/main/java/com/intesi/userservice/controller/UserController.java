package com.intesi.userservice.controller;

import com.intesi.userservice.dto.UserCreateRequestDto;
import com.intesi.userservice.dto.UserResponseDto;
import com.intesi.userservice.dto.UserUpdateRequestDto;
import com.intesi.userservice.model.RoleName;
import com.intesi.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestione utenti: creazione, ricerca, aggiornamento, cancellazione e gestione ruoli")
public class UserController {

    private final UserService userService;

    // -------------------------------------------------------------------------
    // GET
    // -------------------------------------------------------------------------

    @Operation(summary = "Lista tutti gli utenti",
               description = "I campi visibili dipendono dal ruolo Keycloak del chiamante (ADMIN / OPERATOR / USER).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista utenti"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso read_user assente")
    })
    @PreAuthorize("hasAuthority('read_user')")
    @GetMapping("/all")
    public List<UserResponseDto> getUsers(Authentication auth) {
        return userService.findAll(auth);
    }

    @Operation(summary = "Cerca utente per ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utente trovato"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso read_user assente"),
        @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    @PreAuthorize("hasAuthority('read_user')")
    @GetMapping("/{id}")
    public UserResponseDto getUserById(
            @Parameter(description = "ID dell'utente") @PathVariable Long id,
            Authentication auth) {
        return userService.findById(id, auth);
    }

    @Operation(summary = "Cerca utente per email")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utente trovato"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso read_user assente"),
        @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    @PreAuthorize("hasAuthority('read_user')")
    @GetMapping("/email/{email}")
    public UserResponseDto getUserByEmail(
            @Parameter(description = "Email dell'utente") @PathVariable String email,
            Authentication auth) {
        return userService.findByEmail(email, auth);
    }

    @Operation(summary = "Cerca utenti per ruolo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista utenti con il ruolo specificato"),
        @ApiResponse(responseCode = "400", description = "Nome ruolo non valido"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso read_user assente"),
        @ApiResponse(responseCode = "404", description = "Nessun utente trovato con quel ruolo")
    })
    @PreAuthorize("hasAuthority('read_user')")
    @GetMapping("/role/{roleName}")
    public List<UserResponseDto> getUsersByRole(
            @Parameter(description = "Nome ruolo (OWNER, OPERATOR, MAINTAINER, DEVELOPER, REPORTER)") @PathVariable RoleName roleName,
            Authentication auth) {
        return userService.findByRoles(Set.of(roleName), auth);
    }

    // -------------------------------------------------------------------------
    // POST
    // -------------------------------------------------------------------------

    @Operation(summary = "Crea un nuovo utente")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Utente creato con successo"),
        @ApiResponse(responseCode = "400", description = "Body non valido (campi obbligatori mancanti)"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso create_user assente"),
        @ApiResponse(responseCode = "409", description = "Email già in uso")
    })
    @PreAuthorize("hasAuthority('create_user')")
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody UserCreateRequestDto request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request, auth));
    }

    // -------------------------------------------------------------------------
    // PATCH
    // -------------------------------------------------------------------------

    @Operation(summary = "Aggiorna i dati di un utente", description = "L'email non è modificabile.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Utente aggiornato"),
        @ApiResponse(responseCode = "400", description = "Body non valido"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso update_user assente"),
        @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    @PreAuthorize("hasAuthority('update_user')")
    @PatchMapping("/{id}")
    public UserResponseDto updateUser(
            @Parameter(description = "ID dell'utente da aggiornare") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto request,
            Authentication auth) {
        return userService.update(id, request, auth);
    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    @Operation(summary = "Elimina un utente")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Utente eliminato"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso delete_user assente"),
        @ApiResponse(responseCode = "404", description = "Utente non trovato")
    })
    @PreAuthorize("hasAuthority('delete_user')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID dell'utente da eliminare") @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Gestione ruoli
    // -------------------------------------------------------------------------

    @Operation(summary = "Aggiunge un ruolo a un utente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ruolo aggiunto, restituisce l'utente aggiornato"),
        @ApiResponse(responseCode = "400", description = "Nome ruolo non valido"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso update_user assente"),
        @ApiResponse(responseCode = "404", description = "Utente o ruolo non trovato")
    })
    @PreAuthorize("hasAuthority('update_user')")
    @PostMapping("/{id}/roles/{roleName}")
    public UserResponseDto addRole(
            @Parameter(description = "ID utente") @PathVariable Long id,
            @Parameter(description = "Nome ruolo da aggiungere") @PathVariable RoleName roleName,
            Authentication auth) {
        return userService.addRole(id, roleName, auth);
    }

    @Operation(summary = "Rimuove un ruolo da un utente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ruolo rimosso, restituisce l'utente aggiornato"),
        @ApiResponse(responseCode = "400", description = "Nome ruolo non valido"),
        @ApiResponse(responseCode = "401", description = "Token JWT mancante o non valido"),
        @ApiResponse(responseCode = "403", description = "Permesso update_user assente"),
        @ApiResponse(responseCode = "404", description = "Utente o ruolo non trovato")
    })
    @PreAuthorize("hasAuthority('update_user')")
    @DeleteMapping("/{id}/roles/{roleName}")
    public UserResponseDto removeRole(
            @Parameter(description = "ID utente") @PathVariable Long id,
            @Parameter(description = "Nome ruolo da rimuovere") @PathVariable RoleName roleName,
            Authentication auth) {
        return userService.removeRole(id, roleName, auth);
    }
}
