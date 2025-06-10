package com.bokszczanin.course.controller;

import com.bokszczanin.course.dto.LoginRequest;
import com.bokszczanin.course.dto.RegisterRequest;
import com.bokszczanin.course.model.Role;
import com.bokszczanin.course.model.UserData;
import com.bokszczanin.course.repository.UserRepository;
import com.bokszczanin.course.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest req = new RegisterRequest("john", "john@example.com", "secret", Role.STUDENT);

        when(passwordEncoder.encode("secret")).thenReturn("hashed_secret");

        ResponseEntity<?> response = authController.register(req);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Registered", response.getBody());

        ArgumentCaptor<UserData> captor = ArgumentCaptor.forClass(UserData.class);
        verify(userRepository).save(captor.capture());
        UserData savedUser = captor.getValue();

        assertEquals("john", savedUser.getUsername());
        assertEquals("john@example.com", savedUser.getEmail());
        assertEquals("hashed_secret", savedUser.getPassword());
        assertEquals(Role.STUDENT, savedUser.getRole());
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest req = new LoginRequest("john", "secret");
        UserData user = UserData.builder()
                .id(UUID.randomUUID())
                .username("john")
                .password("hashed_secret")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed_secret")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("mocked.jwt.token");

        ResponseEntity<?> response = authController.login(req);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(Map.of("token", "mocked.jwt.token"), response.getBody());
    }

    @Test
    void shouldFailLoginWithInvalidPassword() {
        LoginRequest req = new LoginRequest("john", "wrongpass");
        UserData user = UserData.builder()
                .id(UUID.randomUUID())
                .username("john")
                .password("hashed_secret")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed_secret")).thenReturn(false);

        ResponseEntity<?> response = authController.login(req);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid credentials", response.getBody());
    }
}
