package com.cmc.controller;

import com.cmc.dto.ApiResponse;
import com.cmc.entity.Book;
import com.cmc.entity.User;
import com.cmc.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    
    private final BookService bookService;
    
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Book>> uploadBook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            Book book = bookService.uploadBook(file, title, author, description, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Book uploaded successfully", book));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Upload failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<Book>>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Book> books = bookService.getBooks(pageable);
            return ResponseEntity.ok(ApiResponse.success(books));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to fetch books: " + e.getMessage()));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<Book>>> searchBooks(
            @RequestParam("keyword") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Book> books = bookService.searchBooks(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success(books));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Search failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/info")
    public ResponseEntity<ApiResponse<Book>> getBookInfo(@PathVariable Long id) {
        try {
            Optional<Book> book = bookService.getBook(id);
            if (book.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(book.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to fetch book info: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/page/{pageNumber}")
    public ResponseEntity<byte[]> getBookPage(
            @PathVariable Long id,
            @PathVariable int pageNumber,
            @AuthenticationPrincipal User currentUser) {
        
        try {
            byte[] imageData = bookService.getBookPageImage(id, pageNumber, currentUser);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(imageData.length);
            headers.setCacheControl("max-age=3600"); // Cache 1 hour
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageData);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('VIP')")
    public ResponseEntity<byte[]> downloadPDF(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {
        
        try {
            String userIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            byte[] pdfData = bookService.downloadPDF(id, currentUser, userIp, userAgent);
            
            // Lấy thông tin sách để đặt tên file
            Optional<Book> bookOpt = bookService.getBook(id);
            String filename = "book.pdf";
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                filename = book.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfData.length);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(ApiResponse.success("Book deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Delete failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBookStatistics(@PathVariable Long id) {
        try {
            Map<String, Object> stats = bookService.getBookStatistics(id);
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to fetch statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Lấy IP address của client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
