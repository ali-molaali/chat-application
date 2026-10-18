package com.messagingapp.service.impl;

import com.messagingapp.entity.User;
import com.messagingapp.exception.UserAlreadyExistsException;
import com.messagingapp.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_savesUserWithHashedPassword() {
        when(userRepository.existsByUsername("ali")).thenReturn(false);
        when(userRepository.existsByEmail("ali@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register("ali", "ali@example.com", "secret123");

        assertEquals("ali", result.getUsername());
        assertEquals("ali@example.com", result.getEmail());
        assertEquals("hashed-password", result.getPasswordHash());
    }

    @Test
    void register_throwsWhenUsernameIsTaken() {
        when(userRepository.existsByUsername("ali")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register("ali", "ali@example.com", "secret123"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_throwsWhenEmailIsTaken() {
        when(userRepository.existsByUsername("ali")).thenReturn(false);
        when(userRepository.existsByEmail("ali@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register("ali", "ali@example.com", "secret123"));

        verify(userRepository, never()).save(any());
    }
}
