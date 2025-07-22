package com.cmc.controller;

import com.cmc.dto.*;
import com.cmc.entity.User;
import com.cmc.service.JwtService;
import com.cmc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "🔐 Authentication", description = "Quản lý đăng nhập, đăng ký và xác thực người dùng")
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @PostMapping("/register")
    @Operation(
        summary = "🔐 Đăng ký tài khoản mới",
        description = "Tạo tài khoản người dùng mới với role STANDARD. Username và email phải là duy nhất."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Đăng ký thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "success": true,
                        "message": "User registered successfully",
                        "data": {
                            "token": "eyJhbGciOiJIUzI1NiJ9...",
                            "username": "user123",
                            "email": "user@example.com",
                            "fullName": "Nguyễn Văn A",
                            "role": "STANDARD",
                            "isVip": false,
                            "isAdmin": false
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Lỗi đăng ký - Username hoặc email đã tồn tại",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "error": "Username already exists"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Parameter(description = "Thông tin đăng ký", required = true)
            @RequestBody RegisterRequest request) {
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
    @Operation(
        summary = "🔑 Đăng nhập hệ thống",
        description = "Xác thực người dùng và trả về JWT token để sử dụng cho các API khác."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Đăng nhập thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "success": true,
                        "message": "Login successful",
                        "data": {
                            "token": "eyJhbGciOiJIUzI1NiJ9...",
                            "username": "admin",
                            "email": "admin@example.com",
                            "fullName": "Administrator",
                            "role": "ADMIN",
                            "isVip": true,
                            "isAdmin": true
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Đăng nhập thất bại - Sai username hoặc password",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "error": "Invalid username or password"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Lỗi hệ thống",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "error": "Login failed: Internal server error"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Parameter(description = "Thông tin đăng nhập", required = true)
            @RequestBody LoginRequest request) {
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
    @Operation(
        summary = "✅ Xác thực JWT Token",
        description = "Kiểm tra tính hợp lệ của JWT token từ Authorization header."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "Token hợp lệ hoặc không hợp lệ",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Token hợp lệ",
                    value = """
                    {
                        "success": true,
                        "data": true
                    }
                    """
                ),
                @ExampleObject(
                    name = "Token không hợp lệ",
                    value = """
                    {
                        "success": true,
                        "data": false
                    }
                    """
                )
            }
        )
    )
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @Parameter(description = "Authorization header với Bearer token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader) {
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
