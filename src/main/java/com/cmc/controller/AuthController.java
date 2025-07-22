package com.cmc.controller;

import com.cmc.dto.*;
import com.cmc.entity.User;
import com.cmc.service.JwtService;
import com.cmc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName()
            );
            
            String token = jwtService.generateToken(user);
            
            AuthResponse authResponse = new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole(),
                    user.isVip(),
                    user.isAdmin()
            );
            
            return ResponseEntity.ok(ApiResponse.success("User registered successfully", authResponse));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            
            AuthResponse authResponse = new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole(),
                    user.isVip(),
                    user.isAdmin()
            );
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
            
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Login failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                boolean isValid = jwtService.isTokenValid(token);
                return ResponseEntity.ok(ApiResponse.success(isValid));
            }
            return ResponseEntity.badRequest().body(ApiResponse.success(false));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }
    }
}
