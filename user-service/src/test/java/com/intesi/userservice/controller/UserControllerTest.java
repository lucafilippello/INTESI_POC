package com.intesi.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intesi.userservice.config.DataInitializer;
import com.intesi.userservice.dto.UserCreateRequestDto;
import com.intesi.userservice.dto.UserUpdateRequestDto;
import com.intesi.userservice.events.handling.ActivityLogEventProducer;
import com.intesi.userservice.mapper.UserMapper;
import com.intesi.userservice.model.Role;
import com.intesi.userservice.model.RoleName;
import com.intesi.userservice.model.User;
import com.intesi.userservice.repository.RoleRepository;
import com.intesi.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//@WebMvcTest(UserController.class)
//@Import({TestSecurityConfig.class, GlobalExceptionHandler.class, UserService.class, UserRepository.class, RoleRepository.class})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
    RabbitAutoConfiguration.class
})
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DataInitializer rolesInitializer;
    @Autowired private UserMapper userMapper;

    
    @MockBean private ActivityLogEventProducer eventProducer;

    
    private String mockUsername = "tony.stark";
    private String mockEmail = "tony.stark@example.com";
    private String mockTaxCode = "STRTNY80A01";
    private String mockName = "Tony";
    private String mockSurname = "Stark";
    
    private Long createdUserId;
    
    

    @BeforeEach
    void setUp() {
    	
    	//Roles db initialization
    	rolesInitializer.run();

    	//Empty users table and create a user for testing
    	userRepository.deleteAll();
    	
    	User user = userMapper.toEntity(buildCreateRequest());
    	user.setCreatedAt(LocalDateTime.now());
    	user = userRepository.save(user);
    	Role role = roleRepository.findByName(RoleName.DEVELOPER).get();
    	user.getRoles().add(role);
    	user = userRepository.save(user);
    	createdUserId = user.getId();
    }

    // --- GET /v1/users ---

    @Test
    void listUsers_asAdmin_returnsAllFields() throws Exception {
        mockMvc.perform(get("/v1/users/all")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("read_user"),
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taxCode").value(mockTaxCode))
                .andExpect(jsonPath("$[0].roles").isArray());
    }
    
    @Test
    void listUsers_asOperator_hidesTaxCode() throws Exception {
        mockMvc.perform(get("/v1/users/all")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("read_user"),
                                new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taxCode").doesNotExist())
                .andExpect(jsonPath("$[0].roles").isArray());
    }
    
    @Test
    void listUsers_asUser_hidesTaxCodeAndRoles() throws Exception {
        mockMvc.perform(get("/v1/users/all")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("read_user"),
                                new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].taxCode").doesNotExist())
                .andExpect(jsonPath("$[0].roles").doesNotExist());
    }
    

    @Test
    void listUsers_withoutReadPermission_returns403() throws Exception {
        mockMvc.perform(get("/v1/users/all")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }
	
    @Test
    void listUsers_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/v1/users/all"))
                .andExpect(status().isUnauthorized());
    }
    
    // --- GET /v1/users/{id} ---
    
    @Test
    void getUser_existingId_returns200() throws Exception {
        mockMvc.perform(get("/v1/users/"+createdUserId)
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("read_user"),
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdUserId))
                .andExpect(jsonPath("$.email").value(mockEmail));
    }
    
    @Test
    void getUser_notFound_returns404() throws Exception {
        mockMvc.perform(get("/v1/users/1000")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("read_user"),
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found for id: 1000"));
    }

    // --- POST /v1/users ---
    
    @Test
    void createUser_withCreatePermission_returns201() throws Exception {
    	userRepository.deleteAll();

        mockMvc.perform(post("/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("create_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void createUser_withoutCreatePermission_returns403() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("read_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_duplicateEmail_returns409() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("create_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isConflict());
    }

    // --- PATCH /v1/users/{id} ---
    
    @Test
    void updateUser_withUpdatePermission_returns200() throws Exception {
        UserUpdateRequestDto request = new UserUpdateRequestDto();
        request.setUsername("mario.r");
        request.setName("Mario");
        request.setSurname("Rossi");

        mockMvc.perform(patch("/v1/users/{id}",createdUserId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("update_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateUser_withoutUpdatePermission_returns403() throws Exception {
        UserUpdateRequestDto validRequest = new UserUpdateRequestDto();
        validRequest.setUsername("tony.s");
        validRequest.setName("Tony");
        validRequest.setSurname("Smazzo");

        mockMvc.perform(patch("/v1/users/{id}",createdUserId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("read_user")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    // --- DELETE /v1/users/{id} ---

    @Test
    void deleteUser_withDeletePermission_returns204() throws Exception {
        mockMvc.perform(delete("/v1/users/{id}",createdUserId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("delete_user"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_withoutDeletePermission_returns403() throws Exception {
        mockMvc.perform(delete("/v1/users/{id}",createdUserId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("read_user"))))
                .andExpect(status().isForbidden());
    }

    //POST /v1/users/{id}/roles/{roleName}
    
    @Test
    void addRole_withUpdatePermission_returns200() throws Exception {
        mockMvc.perform(post("/v1/users/{id}/roles/OWNER",createdUserId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("update_user"))))
                .andExpect(status().isOk());
    }

    @Test
    void addRole_invalidRoleName_returns400() throws Exception {
        mockMvc.perform(post("/v1/users/{id}/roles/INVALID_ROLE",createdUserId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("update_user"))))
                .andExpect(status().isBadRequest());
    }

    //DELETE /v1/users/{id}/roles/{roleName}
    
    @Test
    void removeRole_withUpdatePermission_returns200() throws Exception {
        //when(userService.removeRole(eq(1L), eq(RoleName.DEVELOPER), any())).thenReturn(adminView);

        mockMvc.perform(delete("/v1/users/{id}/roles/DEVELOPER",createdUserId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("update_user"))))
                .andExpect(status().isOk());
    }
    
    //helper methods
    private UserCreateRequestDto buildCreateRequest() {
        UserCreateRequestDto req = new UserCreateRequestDto();
        req.setUsername(mockUsername);
        req.setEmail(mockEmail);
        req.setTaxCode(mockTaxCode);
        req.setName(mockName);
        req.setSurname(mockSurname);
        req.setRoles(Set.of(RoleName.DEVELOPER));
        return req;
    }
}
