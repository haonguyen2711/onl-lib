package com.cmc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Thông tin đăng ký tài khoản mới")
public class RegisterRequest {
    
    @Schema(description = "Tên đăng nhập (duy nhất)", example = "user123", required = true)
    private String username;
    
    @Schema(description = "Email (duy nhất)", example = "user@example.com", required = true)
    private String email;
    
    @Schema(description = "Mật khẩu (tối thiểu 6 ký tự)", example = "password123", required = true)
    private String password;
    
    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String fullName;
}
