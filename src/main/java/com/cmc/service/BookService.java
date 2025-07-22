package com.cmc.service;

import com.cmc.entity.Book;
import com.cmc.entity.DownloadLog;
import com.cmc.entity.DownloadType;
import com.cmc.entity.User;
import com.cmc.repository.BookRepository;
import com.cmc.repository.DownloadLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {
    
    private final BookRepository bookRepository;
    private final DownloadLogRepository downloadLogRepository;
    private final EncryptionService encryptionService;
    private final PDFProcessingService pdfProcessingService;
    private final ObjectMapper objectMapper;
    
    @Value("${storage.books-path}")
    private String booksPath;
    
    @Value("${storage.temp-path}")
    private String tempPath;
    
    /**
     * Upload và xử lý sách mới (chỉ admin)
     */
    public Book uploadBook(MultipartFile file, String title, String author, 
                          String description, User uploadedBy) throws Exception {
        
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (!Objects.requireNonNull(file.getContentType()).equals("application/pdf")) {
            throw new RuntimeException("Only PDF files are allowed");
        }
        
        byte[] pdfData = file.getBytes();
        
        // Validate PDF
        if (!pdfProcessingService.isValidPDF(pdfData)) {
            throw new RuntimeException("Invalid PDF file");
        }
        
        // Tạo directories nếu cần
        Path booksDir = Paths.get(booksPath);
        if (!Files.exists(booksDir)) {
            Files.createDirectories(booksDir);
        }
        
        // Tạo Book entity
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setDescription(description);
        book.setOriginalFilename(file.getOriginalFilename());
        book.setFileSize(file.getSize());
        book.setUploadedBy(uploadedBy);
        book.setIsActive(true);
        book.setCreatedAt(LocalDateTime.now());
        
        // Lưu book để có ID
        book = bookRepository.save(book);
        
        String bookId = "book" + book.getId();
        
        try {
            // 1. Tạo AES key ngẫu nhiên
            SecretKey aesKey = encryptionService.generateAESKey();
            
            // 2. Mã hóa PDF bằng AES
            EncryptionService.EncryptionResult encryptionResult = 
                    encryptionService.encryptPDF(pdfData, aesKey);
            
            // 3. Mã hóa AES key bằng RSA
            byte[] encryptedAESKey = encryptionService.encryptAESKey(aesKey);
            
            // 4. Lưu file mã hóa
            String encryptedFilename = bookId + ".pdf.enc";
            Path encryptedFilePath = booksDir.resolve(encryptedFilename);
            Files.write(encryptedFilePath, encryptionResult.getEncryptedData());
            
            // 5. Lưu key mã hóa
            String keyFilename = bookId + ".key.enc";
            Path keyFilePath = booksDir.resolve(keyFilename);
            Files.write(keyFilePath, encryptedAESKey);
            
            // 6. Lưu metadata
            String metadataFilename = bookId + "_meta.json";
            Path metadataFilePath = booksDir.resolve(metadataFilename);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("iv", Base64.getEncoder().encodeToString(encryptionResult.getIv()));
            metadata.put("authTag", Base64.getEncoder().encodeToString(encryptionResult.getAuthTag()));
            metadata.put("algorithm", "AES-256-GCM");
            metadata.put("uploadTime", LocalDateTime.now().toString());
            metadata.put("originalFilename", file.getOriginalFilename());
            metadata.put("fileSize", file.getSize());
            
            Files.write(metadataFilePath, objectMapper.writeValueAsBytes(metadata));
            
            // 7. Convert PDF thành ảnh với watermark
            List<String> imageFiles = pdfProcessingService.convertPDFToImages(pdfData, bookId, uploadedBy);
            
            // 8. Lấy số trang
            int totalPages = pdfProcessingService.getPDFPageCount(pdfData);
            
            // 9. Cập nhật thông tin book
            book.setEncryptedFilename(encryptedFilename);
            book.setKeyFilename(keyFilename);
            book.setMetadataFilename(metadataFilename);
            book.setImagesFolder(bookId + "_images");
            book.setTotalPages(totalPages);
            book.setUpdatedAt(LocalDateTime.now());
            
            return bookRepository.save(book);
            
        } catch (Exception e) {
            // Cleanup nếu có lỗi
            cleanup(bookId);
            throw new RuntimeException("Failed to process book: " + e.getMessage(), e);
        }
    }
    
    /**
     * Lấy danh sách sách
     */
    public Page<Book> getBooks(Pageable pageable) {
        return bookRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
    }
    
    /**
     * Tìm kiếm sách
     */
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        return bookRepository.searchBooks(keyword, pageable);
    }
    
    /**
     * Lấy thông tin sách
     */
    public Optional<Book> getBook(Long id) {
        return bookRepository.findByIdAndIsActiveTrue(id);
    }
    
    /**
     * Lấy ảnh trang sách (cho người dùng standard)
     */
    public byte[] getBookPageImage(Long bookId, int pageNumber, User user) throws IOException {
        Book book = bookRepository.findByIdAndIsActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        if (pageNumber < 1 || pageNumber > book.getTotalPages()) {
            throw new RuntimeException("Invalid page number");
        }
        
        // Log download
        logDownload(user, book, DownloadType.IMAGE_VIEW, null);
        
        String imageFilename = String.format("page_%03d.jpg", pageNumber);
        Path imagePath = Paths.get(booksPath, book.getImagesFolder(), imageFilename);
        
        if (!Files.exists(imagePath)) {
            throw new RuntimeException("Image not found");
        }
        
        return Files.readAllBytes(imagePath);
    }
    
    /**
     * Tải PDF gốc (chỉ VIP)
     */
    public byte[] downloadPDF(Long bookId, User user, String userIp, String userAgent) throws Exception {
        if (!user.isVip()) {
            throw new RuntimeException("Only VIP users can download PDF");
        }
        
        Book book = bookRepository.findByIdAndIsActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        // Đọc file mã hóa
        Path encryptedFilePath = Paths.get(booksPath, book.getEncryptedFilename());
        byte[] encryptedData = Files.readAllBytes(encryptedFilePath);
        
        // Đọc key mã hóa
        Path keyFilePath = Paths.get(booksPath, book.getKeyFilename());
        byte[] encryptedKey = Files.readAllBytes(keyFilePath);
        
        // Đọc metadata
        Path metadataFilePath = Paths.get(booksPath, book.getMetadataFilename());
        String metadataJson = Files.readString(metadataFilePath);
        Map<String, Object> metadata = objectMapper.readValue(metadataJson, Map.class);
        
        byte[] iv = Base64.getDecoder().decode((String) metadata.get("iv"));
        
        // Giải mã AES key
        SecretKey aesKey = encryptionService.decryptAESKey(encryptedKey);
        
        // Giải mã PDF
        byte[] pdfData = encryptionService.decryptPDF(encryptedData, aesKey, iv);
        
        // Log download
        logDownload(user, book, DownloadType.PDF_DOWNLOAD, userIp, userAgent);
        
        return pdfData;
    }
    
    /**
     * Xóa sách (chỉ admin)
     */
    public void deleteBook(Long bookId) throws IOException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        // Xóa files
        cleanup("book" + book.getId());
        
        // Đánh dấu inactive
        book.setIsActive(false);
        book.setUpdatedAt(LocalDateTime.now());
        bookRepository.save(book);
    }
    
    /**
     * Lấy thống kê downloads
     */
    public Map<String, Object> getBookStatistics(Long bookId) {
        Book book = bookRepository.findByIdAndIsActiveTrue(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        
        List<DownloadLog> logs = downloadLogRepository.findByBookOrderByDownloadTimeDesc(book);
        
        long pdfDownloads = logs.stream()
                .filter(log -> log.getDownloadType() == DownloadType.PDF_DOWNLOAD)
                .count();
        
        long imageViews = logs.stream()
                .filter(log -> log.getDownloadType() == DownloadType.IMAGE_VIEW)
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("bookId", bookId);
        stats.put("title", book.getTitle());
        stats.put("totalPages", book.getTotalPages());
        stats.put("pdfDownloads", pdfDownloads);
        stats.put("imageViews", imageViews);
        stats.put("totalInteractions", pdfDownloads + imageViews);
        
        return stats;
    }
    
    /**
     * Log download/view activity
     */
    private void logDownload(User user, Book book, DownloadType type, String userIp) {
        logDownload(user, book, type, userIp, null);
    }
    
    private void logDownload(User user, Book book, DownloadType type, String userIp, String userAgent) {
        DownloadLog log = new DownloadLog();
        log.setUser(user);
        log.setBook(book);
        log.setDownloadType(type);
        log.setUserIp(userIp);
        log.setUserAgent(userAgent);
        log.setDownloadTime(LocalDateTime.now());
        
        downloadLogRepository.save(log);
    }
    
    /**
     * Cleanup files khi có lỗi hoặc xóa
     */
    private void cleanup(String bookId) {
        try {
            Path booksDir = Paths.get(booksPath);
            
            // Xóa file mã hóa
            Files.deleteIfExists(booksDir.resolve(bookId + ".pdf.enc"));
            
            // Xóa key file
            Files.deleteIfExists(booksDir.resolve(bookId + ".key.enc"));
            
            // Xóa metadata
            Files.deleteIfExists(booksDir.resolve(bookId + "_meta.json"));
            
            // Xóa thư mục ảnh
            Path imagesDir = booksDir.resolve(bookId + "_images");
            if (Files.exists(imagesDir)) {
                Files.walk(imagesDir)
                     .sorted(Comparator.reverseOrder())
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             // Ignore
                         }
                     });
            }
        } catch (IOException e) {
            // Log error but don't throw
            System.err.println("Failed to cleanup files for " + bookId + ": " + e.getMessage());
        }
    }
}
