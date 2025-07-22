package com.cmc.dto;

import com.cmc.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Phản hồi sau khi đăng nhập thành công")
public class AuthResponse {
    
    @Schema(description = "JWT Token để xác thực API", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    
    @Schema(description = "Tên đăng nhập", example = "admin")
    private String username;
    
    @Schema(description = "Email", example = "admin@library.com")
    private String email;
    
    @Schema(description = "Họ và tên", example = "System Administrator")
    private String fullName;
    
    @Schema(description = "Vai trò người dùng", example = "ADMIN")
    private UserRole role;
    
    @Schema(description = "Có quyền VIP hay không", example = "false")
    private boolean isVip;
    
    @Schema(description = "Có quyền Admin hay không", example = "true")
    private boolean isAdmin;
}
