package com.cmc;

import com.cmc.entity.User;
import com.cmc.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OnlineLibraryApplicationTests {
    
    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
        assertTrue(true);
    }
    
    @Test
    void testUserCreation() {
        // Test user entity creation
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole(UserRole.STANDARD);
        user.setIsActive(true);
        
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals(UserRole.STANDARD, user.getRole());
        assertTrue(user.getIsActive());
        assertFalse(user.isVip());
        assertFalse(user.isAdmin());
    }
    
    @Test
    void testVipUser() {
        User vipUser = new User();
        vipUser.setRole(UserRole.VIP);
        vipUser.setVipExpiresAt(java.time.LocalDateTime.now().plusDays(30));
        
        assertTrue(vipUser.isVip());
        assertFalse(vipUser.isAdmin());
    }
    
    @Test
    void testAdminUser() {
        User adminUser = new User();
        adminUser.setRole(UserRole.ADMIN);
        
        assertFalse(adminUser.isVip());
        assertTrue(adminUser.isAdmin());
    }
    
    @Test
    void testUserRoles() {
        // Test UserRole enum
        assertEquals("Admin", UserRole.ADMIN.getDisplayName());
        assertEquals("VIP User", UserRole.VIP.getDisplayName());
        assertEquals("Standard User", UserRole.STANDARD.getDisplayName());
    }
}
