package com.cmc.controller;

import com.cmc.dto.ApiResponse;
import com.cmc.entity.User;
import com.cmc.entity.UserRole;
import com.cmc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "👤 User Management", description = "Quản lý thông tin người dùng, profile và role")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    @Operation(
        summary = "👤 Xem thông tin cá nhân",
        description = "Lấy thông tin profile của người dùng hiện tại.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "Thông tin profile",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = User.class),
            examples = @ExampleObject(
                value = """
                {
                    "success": true,
                    "data": {
                        "id": 1,
                        "username": "user123",
                        "email": "user@example.com",
                        "fullName": "Nguyễn Văn A",
                        "role": "STANDARD",
                        "isVip": false,
                        "isAdmin": false,
                        "registrationDate": "2024-01-01T10:00:00"
                    }
                }
                """
            )
        )
    )
    public ResponseEntity<ApiResponse<User>> getProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(currentUser));
    }
    
    @PutMapping("/profile")
    @Operation(
        summary = "✏️ Cập nhật thông tin cá nhân",
        description = "Cập nhật thông tin profile (họ tên, email) của người dùng.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Cập nhật thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "success": true,
                        "message": "Profile updated successfully",
                        "data": {
                            "id": 1,
                            "username": "user123",
                            "email": "newemail@example.com",
                            "fullName": "Tên Mới",
                            "role": "STANDARD"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Lỗi cập nhật",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "error": "Update failed: Email already exists"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Thông tin cập nhật", required = true)
            @RequestBody Map<String, String> updates) {
        
        try {
            String fullName = updates.get("fullName");
            String email = updates.get("email");
            
            User updatedUser = userService.updateProfile(currentUser.getId(), fullName, email);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updatedUser));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Update failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, String> passwordData) {
        
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Password change failed: " + e.getMessage()));
        }
    }
    
    // Admin endpoints
    @GetMapping("/manage/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to fetch users: " + e.getMessage()));
        }
    }
    
    @PostMapping("/manage/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> roleData) {
        
        try {
            UserRole newRole = UserRole.valueOf((String) roleData.get("role"));
            LocalDateTime vipExpiresAt = null;
            
            if (newRole == UserRole.VIP && roleData.containsKey("vipExpiresAt")) {
                vipExpiresAt = LocalDateTime.parse((String) roleData.get("vipExpiresAt"));
            }
            
            User updatedUser = userService.updateUserRole(userId, newRole, vipExpiresAt);
            return ResponseEntity.ok(ApiResponse.success("User role updated successfully", updatedUser));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Role update failed: " + e.getMessage()));
        }
    }
    
    @PostMapping("/manage/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(@PathVariable Long userId) {
        try {
            User updatedUser = userService.toggleUserStatus(userId);
            String status = updatedUser.getIsActive() ? "activated" : "deactivated";
            return ResponseEntity.ok(ApiResponse.success("User " + status + " successfully", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status toggle failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/manage/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable String role) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            List<User> users = userService.getUsersByRole(userRole);
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to fetch users by role: " + e.getMessage()));
        }
    }
}
