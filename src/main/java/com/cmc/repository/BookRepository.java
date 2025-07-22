package com.cmc.repository;

import com.cmc.entity.Book;
import com.cmc.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    List<Book> findByIsActiveTrueOrderByCreatedAtDesc();
    Page<Book> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    Optional<Book> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT b FROM Book b WHERE b.isActive = true AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);
    
    List<Book> findByUploadedByAndIsActiveTrue(User uploadedBy);
    
    @Query("SELECT COUNT(b) FROM Book b WHERE b.isActive = true")
    long countActiveBooks();
}
