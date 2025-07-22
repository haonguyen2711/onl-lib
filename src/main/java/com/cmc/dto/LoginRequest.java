package com.cmc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {
    
    @Schema(description = "Tên đăng nhập", example = "admin", required = true)
    private String username;
    
    @Schema(description = "Mật khẩu", example = "admin123", required = true)
    private String password;
}
