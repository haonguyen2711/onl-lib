package com.cmc.dto;

import com.cmc.entity.UserRole;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private boolean isVip;
    private boolean isAdmin;
}
