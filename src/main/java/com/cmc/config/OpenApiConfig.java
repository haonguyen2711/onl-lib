package com.cmc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .addSecurityItem(securityRequirement())
                .schemaRequirement("bearerAuth", securityScheme());
    }
    
    private Info apiInfo() {
        return new Info()
                .title("📚🔐 Online Library Encryption System API")
                .description("""
                        # Online Library Encryption System
                        
                        Hệ thống thư viện số an toàn với mã hóa AES-256-GCM và RSA-2048.
                        
                        ## 🎯 Tính Năng Chính
                        
                        ### 🔐 Bảo Mật Cao Cấp
                        - **AES-256-GCM**: Mã hóa file PDF gốc
                        - **RSA-2048**: Bảo vệ AES key
                        - **JWT Authentication**: Xác thực người dùng
                        
                        ### 💧 Watermark Cá Nhân
                        - Watermark trên mỗi trang: `email | tên | thời gian`
                        - Ngăn chặn chia sẻ trái phép
                        
                        ### 👥 Phân Quyền 3 Cấp
                        - **ADMIN**: Upload sách, quản lý users
                        - **VIP**: Tải PDF gốc (không watermark)
                        - **STANDARD**: Xem ảnh có watermark
                        
                        ## 🚀 Cách Sử Dụng API
                        
                        1. **Đăng nhập**: POST `/auth/login` để lấy JWT token
                        2. **Thêm Authorization Header**: `Bearer YOUR_JWT_TOKEN`
                        3. **Sử dụng endpoints**: Theo quyền của user
                        
                        ## 🔑 Test Accounts
                        
                        | Username | Password | Role | Permissions |
                        |----------|----------|------|-------------|
                        | admin | admin123 | ADMIN | Full access |
                        | vipuser | user123 | VIP | Download PDFs |
                        | standarduser | user123 | STANDARD | View images only |
                        """)
                .version("1.0.0")
                .contact(apiContact())
                .license(apiLicense());
    }
    
    private Contact apiContact() {
        return new Contact()
                .name("Online Library Team")
                .email("support@onllib.com")
                .url("https://github.com/haonguyen2711/onl-lib");
    }
    
    private License apiLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }
    
    private List<Server> apiServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080" + contextPath)
                        .description("🔧 Development Server"),
                new Server()
                        .url("http://staging.onllib.local" + contextPath)
                        .description("🧪 Staging Server"),
                new Server()
                        .url("https://api.onllib.com" + contextPath)
                        .description("🚀 Production Server")
        );
    }
    
    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }
    
    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("🔑 JWT Authentication Token")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
    }
}
