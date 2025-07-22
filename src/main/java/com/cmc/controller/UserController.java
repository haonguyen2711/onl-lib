package com.cmc.controller;

import com.cmc.dto.ApiResponse;
import com.cmc.entity.User;
import com.cmc.entity.UserRole;
import com.cmc.service.UserService;
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
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(currentUser));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(
            @AuthenticationPrincipal User currentUser,
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
