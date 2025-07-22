package com.cmc.controller;

import com.cmc.dto.ApiResponse;
import com.cmc.entity.Book;
import com.cmc.entity.User;
import com.cmc.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "📚 Book Management", description = "Quản lý sách - Upload, xem, tải PDF")
public class BookController {
    
    private final BookService bookService;
    
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "📤 Upload sách mới",
        description = "Chỉ Admin có thể upload sách. File PDF sẽ được mã hóa và lưu trữ an toàn.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Upload thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Book.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "success": true,
                        "message": "Book uploaded successfully",
                        "data": {
                            "id": 1,
                            "title": "Java Programming",
                            "author": "Oracle",
                            "description": "Complete Java guide",
                            "filePath": "encrypted_path",
                            "uploadedBy": "admin",
                            "uploadDate": "2024-01-01T10:00:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Lỗi upload",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "error": "Upload failed: Invalid file format"
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Không có quyền - Chỉ Admin",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "error": "Access denied"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<Book>> uploadBook(
            @Parameter(description = "File PDF để upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Tiêu đề sách", required = true, example = "Java Programming")
            @RequestParam("title") String title,
            @Parameter(description = "Tác giả", required = true, example = "Oracle")
            @RequestParam("author") String author,
            @Parameter(description = "Mô tả sách", required = false, example = "Complete guide to Java programming")
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
    @Operation(
        summary = "📋 Danh sách sách",
        description = "Xem danh sách tất cả sách có sẵn với phân trang. Không cần đăng nhập."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "Lấy danh sách thành công",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                {
                    "success": true,
                    "data": {
                        "content": [
                            {
                                "id": 1,
                                "title": "Java Programming",
                                "author": "Oracle",
                                "description": "Complete Java guide",
                                "uploadedBy": "admin",
                                "uploadDate": "2024-01-01T10:00:00"
                            }
                        ],
                        "pageable": {
                            "pageNumber": 0,
                            "pageSize": 10
                        },
                        "totalElements": 1,
                        "totalPages": 1
                    }
                }
                """
            )
        )
    )
    public ResponseEntity<ApiResponse<Page<Book>>> getBooks(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số sách mỗi trang", example = "10")
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
    @Operation(
        summary = "🔍 Tìm kiếm sách",
        description = "Tìm kiếm sách theo từ khóa trong tiêu đề, tác giả hoặc mô tả."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", 
        description = "Tìm kiếm thành công",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                {
                    "success": true,
                    "data": {
                        "content": [
                            {
                                "id": 1,
                                "title": "Java Programming",
                                "author": "Oracle",
                                "description": "Complete Java guide"
                            }
                        ],
                        "totalElements": 1
                    }
                }
                """
            )
        )
    )
    public ResponseEntity<ApiResponse<Page<Book>>> searchBooks(
            @Parameter(description = "Từ khóa tìm kiếm", required = true, example = "Java")
            @RequestParam("keyword") String keyword,
            @Parameter(description = "Số trang", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số sách mỗi trang", example = "10")
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
    @Operation(
        summary = "📖 Thông tin chi tiết sách",
        description = "Lấy thông tin chi tiết của một cuốn sách theo ID."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Lấy thông tin thành công",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Book.class),
                examples = @ExampleObject(
                    value = """
                    {
                        "success": true,
                        "data": {
                            "id": 1,
                            "title": "Java Programming",
                            "author": "Oracle",
                            "description": "Complete Java guide",
                            "uploadedBy": "admin",
                            "uploadDate": "2024-01-01T10:00:00"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy sách"
        )
    })
    public ResponseEntity<ApiResponse<Book>> getBookInfo(
            @Parameter(description = "ID của sách", required = true, example = "1")
            @PathVariable Long id) {
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
    @Operation(
        summary = "🖼️ Xem trang sách",
        description = "Xem một trang cụ thể của sách dưới dạng hình ảnh có watermark. Miễn phí cho tất cả người dùng.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Trả về hình ảnh trang sách có watermark",
            content = @Content(
                mediaType = "image/jpeg",
                schema = @Schema(type = "string", format = "binary")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Lỗi khi lấy trang sách"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Cần đăng nhập"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy sách hoặc trang"
        )
    })
    public ResponseEntity<byte[]> getBookPage(
            @Parameter(description = "ID của sách", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Số trang (bắt đầu từ 1)", required = true, example = "1")
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
    @Operation(
        summary = "⬇️ Tải PDF gốc",
        description = "Tải file PDF gốc không watermark. Chỉ dành cho người dùng VIP. Có ghi log tải về.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Tải PDF thành công",
            content = @Content(
                mediaType = "application/pdf",
                schema = @Schema(type = "string", format = "binary")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Không có quyền - Chỉ VIP"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Không tìm thấy sách"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Cần đăng nhập"
        )
    })
    public ResponseEntity<byte[]> downloadPDF(
            @Parameter(description = "ID của sách", required = true, example = "1")
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
