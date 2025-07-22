package com.cmc.repository;

import com.cmc.entity.DownloadLog;
import com.cmc.entity.User;
import com.cmc.entity.Book;
import com.cmc.entity.DownloadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {
    
    List<DownloadLog> findByUserOrderByDownloadTimeDesc(User user);
    Page<DownloadLog> findByUserOrderByDownloadTimeDesc(User user, Pageable pageable);
    
    List<DownloadLog> findByBookOrderByDownloadTimeDesc(Book book);
    
    @Query("SELECT d FROM DownloadLog d WHERE d.downloadTime BETWEEN :startDate AND :endDate " +
           "ORDER BY d.downloadTime DESC")
    List<DownloadLog> findByDownloadTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(d) FROM DownloadLog d WHERE d.downloadType = :downloadType")
    long countByDownloadType(@Param("downloadType") DownloadType downloadType);
    
    @Query("SELECT COUNT(d) FROM DownloadLog d WHERE d.user = :user AND d.book = :book " +
           "AND d.downloadType = :downloadType")
    long countUserDownloads(@Param("user") User user, @Param("book") Book book, 
                           @Param("downloadType") DownloadType downloadType);
}
