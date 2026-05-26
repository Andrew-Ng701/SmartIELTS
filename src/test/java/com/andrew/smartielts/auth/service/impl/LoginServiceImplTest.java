package com.andrew.smartielts.auth.service.impl;

import com.andrew.smartielts.auth.domain.dto.AuthResponseDTO;
import com.andrew.smartielts.auth.domain.dto.UserDTO;
import com.andrew.smartielts.auth.domain.pojo.User;
import com.andrew.smartielts.auth.mapper.AuthMapper;
import com.andrew.smartielts.security.properties.JwtProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void login_shouldUpdateLoginStatsAfterPasswordMatches() {
        LoginServiceImpl service = new LoginServiceImpl();
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        jwtProperties.setTtl(7200000L);
        jwtProperties.setRefreshInterval(900000L);
        ReflectionTestUtils.setField(service, "authMapper", authMapper);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(service, "jwtProperties", jwtProperties);

        User user = new User();
        user.setId(9L);
        user.setEmail("u@example.com");
        user.setPassword("encoded");
        user.setRole("USER");
        user.setIsDeleted(0);
        user.setTokenVersion(3L);
        when(authMapper.findAnyByEmail("u@example.com")).thenReturn(user);
        when(authMapper.findActiveByEmail("u@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        UserDTO dto = new UserDTO();
        dto.setEmail(" U@Example.com ");
        dto.setPassword("password123");

        AuthResponseDTO result = service.login(dto);

        assertNotNull(result.getToken());
        assertEquals(9L, result.getUserId());
        assertEquals("USER", result.getRole());
        verify(authMapper).updateLoginStatsById(9L);
    }
}
