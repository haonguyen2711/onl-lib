package com.cmc.repository;

import com.cmc.entity.User;
import com.cmc.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
    List<User> findByIsActiveTrue();
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.vipExpiresAt IS NOT NULL AND u.vipExpiresAt < :now")
    List<User> findExpiredVipUsers(@Param("now") LocalDateTime now);
}
