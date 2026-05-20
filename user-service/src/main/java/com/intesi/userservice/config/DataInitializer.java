package com.intesi.userservice.config;

import com.intesi.userservice.model.Role;
import com.intesi.userservice.model.RoleName;
import com.intesi.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
/**
 * Inizializza il database con i ruoli definiti in RoleName all'avvio dell'applicazione.
 * Se un ruolo è già presente, non viene creato nuovamente.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
            }
        });
    }
}
