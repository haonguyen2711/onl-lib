package com.cmc.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DownloadLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(name = "download_time")
    private LocalDateTime downloadTime = LocalDateTime.now();
    
    @Column(name = "user_ip")
    private String userIp;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "download_type")
    private DownloadType downloadType;
}
