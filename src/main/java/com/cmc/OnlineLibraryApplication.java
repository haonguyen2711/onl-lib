package com.cmc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineLibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineLibraryApplication.class, args);
        System.out.println("🚀 Online Library Encryption System started successfully!");
        System.out.println("📚 Access at: http://localhost:8080/api");
    }
}