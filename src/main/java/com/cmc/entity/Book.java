package com.cmc.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    private String author;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "original_filename")
    private String originalFilename;
    
    @Column(name = "encrypted_filename")
    private String encryptedFilename;
    
    @Column(name = "key_filename")
    private String keyFilename;
    
    @Column(name = "metadata_filename")
    private String metadataFilename;
    
    @Column(name = "images_folder")
    private String imagesFolder;
    
    @Column(name = "total_pages")
    private Integer totalPages;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
