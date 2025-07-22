package com.cmc.service;

import com.cmc.entity.User;
import com.cmc.entity.UserRole;
import com.cmc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
    
    /**
     * Đăng ký người dùng mới
     */
    public User registerUser(String username, String email, String password, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(UserRole.STANDARD);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Tìm user theo username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Tìm user theo email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Lấy tất cả users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Lấy users theo role
     */
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Cập nhật role của user (chỉ admin)
     */
    public User updateUserRole(Long userId, UserRole newRole, LocalDateTime vipExpiresAt) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(newRole);
        if (newRole == UserRole.VIP) {
            user.setVipExpiresAt(vipExpiresAt);
        } else {
            user.setVipExpiresAt(null);
        }
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Kích hoạt/vô hiệu hóa user
     */
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Cập nhật thông tin cá nhân
     */
    public User updateProfile(Long userId, String fullName, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra email trùng
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }
        
        user.setFullName(fullName);
        user.setEmail(email);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Đổi mật khẩu
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }
    
    /**
     * Xử lý VIP users hết hạn
     */
    @Transactional
    public void processExpiredVipUsers() {
        List<User> expiredUsers = userRepository.findExpiredVipUsers(LocalDateTime.now());
        
        for (User user : expiredUsers) {
            user.setRole(UserRole.STANDARD);
            user.setVipExpiresAt(null);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
    
    /**
     * Kiểm tra quyền admin
     */
    public boolean isAdmin(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }
    
    /**
     * Kiểm tra quyền VIP
     */
    public boolean isVip(User user) {
        return user != null && user.isVip();
    }
}
