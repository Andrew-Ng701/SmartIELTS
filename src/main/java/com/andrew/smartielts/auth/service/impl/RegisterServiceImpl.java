package com.andrew.smartielts.auth.service.impl;

import com.andrew.smartielts.auth.domain.dto.AuthResponseDTO;
import com.andrew.smartielts.auth.domain.dto.UserDTO;
import com.andrew.smartielts.auth.domain.pojo.User;
import com.andrew.smartielts.auth.mapper.UserMapper;
import com.andrew.smartielts.auth.service.RegisterService;
import com.andrew.smartielts.security.properties.JwtProperties;
import com.andrew.smartielts.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterServiceImpl implements RegisterService{

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponseDTO register(UserDTO dto) {

        if (userMapper.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("USER");

        userMapper.save(user);

        // ✅ 直接生成 token
        String token = JwtUtil.createToken(
                user.getId(),
                user.getRole(),
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl()
        );

        return new AuthResponseDTO(token, user.getId(), user.getRole());
    }
}
