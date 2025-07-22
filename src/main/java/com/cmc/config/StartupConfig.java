package com.cmc.config;

import com.cmc.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupConfig implements ApplicationRunner {
    
    private final EncryptionService encryptionService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("🔐 Initializing encryption keys...");
        
        try {
            encryptionService.initializeKeys();
            log.info("✅ Encryption keys initialized successfully");
        } catch (Exception e) {
            log.error("❌ Failed to initialize encryption keys: {}", e.getMessage());
            throw e;
        }
        
        log.info("🚀 Online Library Encryption System is ready!");
    }
}
