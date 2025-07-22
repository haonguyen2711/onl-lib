package com.cmc.service;

import com.cmc.entity.User;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PDFProcessingService {
    
    @Value("${watermark.font-size}")
    private int watermarkFontSize;
    
    @Value("${watermark.opacity}")
    private float watermarkOpacity;
    
    @Value("${storage.books-path}")
    private String booksPath;
    
    /**
     * Convert PDF thành danh sách ảnh với watermark
     */
    public List<String> convertPDFToImages(byte[] pdfData, String bookId, User user) throws IOException {
        List<String> imageFiles = new ArrayList<>();
        
        // Tạo thư mục cho ảnh
        Path imagesDir = Paths.get(booksPath, bookId + "_images");
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
        }
        
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            
            for (int page = 0; page < pageCount; page++) {
                // Render trang thành ảnh
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
                
                // Thêm watermark
                BufferedImage watermarkedImage = addWatermark(image, user);
                
                // Lưu ảnh
                String fileName = String.format("page_%03d.jpg", page + 1);
                Path imagePath = imagesDir.resolve(fileName);
                ImageIO.write(watermarkedImage, "JPEG", imagePath.toFile());
                
                imageFiles.add(fileName);
            }
        }
        
        return imageFiles;
    }
    
    /**
     * Thêm watermark cá nhân lên ảnh
     */
    private BufferedImage addWatermark(BufferedImage originalImage, User user) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Tạo ảnh mới với watermark
        BufferedImage watermarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = watermarkedImage.createGraphics();
        
        // Vẽ ảnh gốc
        g2d.drawImage(originalImage, 0, 0, null);
        
        // Cấu hình cho watermark
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Tạo nội dung watermark
        String watermarkText = createWatermarkText(user);
        
        // Cấu hình font và màu
        Font font = new Font("Arial", Font.BOLD, watermarkFontSize);
        g2d.setFont(font);
        
        // Tạo màu với độ trong suốt
        Color watermarkColor = new Color(128, 128, 128, (int)(255 * watermarkOpacity));
        g2d.setColor(watermarkColor);
        
        // Vẽ watermark ở góc dưới bên trái
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(watermarkText);
        int textHeight = fontMetrics.getHeight();
        
        int x = 10; // Margin từ bên trái
        int y = height - 10; // Margin từ dưới lên
        
        // Vẽ background cho text (tùy chọn)
        g2d.setColor(new Color(255, 255, 255, (int)(255 * watermarkOpacity * 0.8)));
        g2d.fillRect(x - 5, y - textHeight + 5, textWidth + 10, textHeight);
        
        // Vẽ text
        g2d.setColor(watermarkColor);
        g2d.drawString(watermarkText, x, y);
        
        // Vẽ thêm watermark nhỏ ở các vị trí khác (tùy chọn)
        addAdditionalWatermarks(g2d, user, width, height);
        
        g2d.dispose();
        return watermarkedImage;
    }
    
    /**
     * Tạo nội dung watermark
     */
    private String createWatermarkText(User user) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        return String.format("%s | %s | %s", 
                user.getEmail(), 
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                now.format(formatter));
    }
    
    /**
     * Thêm watermark nhỏ ở các vị trí khác
     */
    private void addAdditionalWatermarks(Graphics2D g2d, User user, int width, int height) {
        Font smallFont = new Font("Arial", Font.PLAIN, watermarkFontSize - 2);
        g2d.setFont(smallFont);
        
        Color lightColor = new Color(128, 128, 128, (int)(255 * watermarkOpacity * 0.3));
        g2d.setColor(lightColor);
        
        String shortText = user.getEmail();
        FontMetrics fm = g2d.getFontMetrics();
        
        // Watermark ở góc trên phải
        int x = width - fm.stringWidth(shortText) - 10;
        int y = 20;
        g2d.drawString(shortText, x, y);
        
        // Watermark ở giữa trang (mờ hơn)
        Color veryLightColor = new Color(128, 128, 128, (int)(255 * watermarkOpacity * 0.1));
        g2d.setColor(veryLightColor);
        
        Font centerFont = new Font("Arial", Font.BOLD, watermarkFontSize + 10);
        g2d.setFont(centerFont);
        fm = g2d.getFontMetrics();
        
        String centerText = user.getUsername().toUpperCase();
        x = (width - fm.stringWidth(centerText)) / 2;
        y = height / 2;
        
        // Xoay text 45 độ
        g2d.rotate(Math.toRadians(-45), x, y);
        g2d.drawString(centerText, x, y);
        g2d.rotate(Math.toRadians(45), x, y); // Xoay lại
    }
    
    /**
     * Lấy tổng số trang của PDF
     */
    public int getPDFPageCount(byte[] pdfData) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            return document.getNumberOfPages();
        }
    }
    
    /**
     * Validate file PDF
     */
    public boolean isValidPDF(byte[] data) {
        try (PDDocument document = Loader.loadPDF(data)) {
            return document.getNumberOfPages() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
