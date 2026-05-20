package com.intesi.userservice.service;

import com.intesi.userservice.audit.AuditLogger;
import com.intesi.userservice.dto.UserCreateRequestDto;
import com.intesi.userservice.dto.UserResponseDto;
import com.intesi.userservice.dto.UserUpdateRequestDto;
import com.intesi.userservice.events.handling.ActivityLogEventProducer;
import com.intesi.userservice.exception.ResourceNotFoundException;
import com.intesi.userservice.mapper.UserMapper;
import com.intesi.userservice.model.ActivityType;
import com.intesi.userservice.model.Role;
import com.intesi.userservice.model.RoleName;
import com.intesi.userservice.model.User;
import com.intesi.userservice.repository.RoleRepository;
import com.intesi.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogger auditLogger;
    private final ActivityLogEventProducer eventProducer;
    private final UserMapper userMapper;

    public List<UserResponseDto> findAll(Authentication auth) {
        auditLogger.log("LIST_USERS", "all users");
        return userRepository.findAll().stream()
                .map(user -> toResponse(user, auth))
                .collect(Collectors.toList());
    }

    public UserResponseDto findById(Long id, Authentication auth) {
        User user = findUserOrThrow(id);
        auditLogger.log("GET_USER", "userId=" + id);
        return toResponse(user, auth);
    }

    public UserResponseDto findByEmail(String email, Authentication auth) {
        User user = findUserOrThrow(email);
        auditLogger.log("GET_USER", "userEmail=" + email);
        return toResponse(user, auth);
    }

    public List<UserResponseDto> findByRoles(Set<RoleName> roleNames, Authentication auth) {
        Set<Role> roles = resolveRoles(roleNames);
        List<User> users = findUsersByRolesOrThrow(roles);
        auditLogger.log("LIST_USERS_BY_ROLE", "users with roles: " + roleNames);
        return users.stream()
                .map(user -> toResponse(user, auth))
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto create(UserCreateRequestDto request, Authentication auth) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        User user = userMapper.toEntity(request);
        user.setRoles(resolveRoles(request.getRoles()));
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        String logMsg = String.format("User created: [id:%s,email:%s]", saved.getId(), saved.getEmail());
        eventProducer.sendUserCreatedEvent(logMsg);
        auditLogger.log(ActivityType.USER_CREATION.name(), logMsg);
        return toResponse(saved, auth);
    }

    @Transactional
    public UserResponseDto update(Long id, UserUpdateRequestDto request, Authentication auth) {
        User user = findUserOrThrow(id);
        userMapper.updateEntity(request, user);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        String logMsg = String.format("User updated: [id:%s,email:%s]", user.getId(), user.getEmail());
        eventProducer.sendUserUpdatedEvent(logMsg);
        auditLogger.log(ActivityType.USER_UPDATE.name(), logMsg);
        return toResponse(user, auth);
    }

    @Transactional
    public void delete(Long id) {
        User user = findUserOrThrow(id);
        String userEmail = user.getEmail();
        userRepository.deleteById(id);
        String logMsg = String.format("User deleted: [id:%s,email:%s]", id, userEmail);
        eventProducer.sendUserDeletedEvent(logMsg);
        auditLogger.log(ActivityType.USER_DELETION.name(), logMsg);
    }

    @Transactional
    public UserResponseDto addRole(Long id, RoleName roleName, Authentication auth) {
        User user = findUserOrThrow(id);
        Role role = findRoleOrThrow(roleName);
        user.getRoles().add(role);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        String logMsg = String.format("Role added to user %d (email:%s). Actual roles: %s",
                id, user.getEmail(), user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.joining(",")));
        eventProducer.sendRoleAddedEvent(logMsg);
        auditLogger.log(ActivityType.ROLE_ADD.name(), logMsg);
        return toResponse(user, auth);
    }

    @Transactional
    public UserResponseDto removeRole(Long id, RoleName roleName, Authentication auth) {
        User user = findUserOrThrow(id);
        Role role = findRoleOrThrow(roleName);
        user.getRoles().remove(role);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        String logMsg = String.format("Role removed from user %d (email:%s). Actual roles: %s",
                id, user.getEmail(), user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.joining(",")));
        eventProducer.sendRoleRemovedEvent(logMsg);
        auditLogger.log(ActivityType.ROLE_REMOVAL.name(), logMsg);
        return toResponse(user, auth);
    }

    // I campi esposti dipendono dal ruolo Keycloak del chiamante
    private UserResponseDto toResponse(User user, Authentication auth) {
        Collection<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (authorities.contains("ROLE_ADMIN")) {
            return userMapper.toAdminResponse(user);
        } else if (authorities.contains("ROLE_OPERATOR")) {
            return userMapper.toOperatorResponse(user);
        } else {
            return userMapper.toUserResponse(user);
        }
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id: " + id));
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }

    private List<User> findUsersByRolesOrThrow(Set<Role> roles) {
        List<User> matchingUsers = userRepository.findByRolesIn(roles);
        if (matchingUsers.isEmpty()) {
            throw new ResourceNotFoundException("No users found with any of the following roles: "
                    + roles.stream().map(Role::getName).collect(Collectors.toSet()));
        }
        return matchingUsers;
    }

    private Role findRoleOrThrow(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(this::findRoleOrThrow)
                .collect(Collectors.toSet());
    }
}
