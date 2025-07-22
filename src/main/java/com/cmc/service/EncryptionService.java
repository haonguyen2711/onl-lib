package com.cmc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {
    
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    @Value("${encryption.rsa.public-key-file}")
    private String publicKeyFile;
    
    @Value("${encryption.rsa.private-key-file}")
    private String privateKeyFile;
    
    @Value("${encryption.rsa.key-size}")
    private int rsaKeySize;
    
    @Value("${encryption.aes.key-size}")
    private int aesKeySize;
    
    @Value("${storage.keys-path}")
    private String keysPath;
    
    private KeyPair rsaKeyPair;
    
    /**
     * Khởi tạo hoặc tải RSA key pair
     */
    public void initializeKeys() throws Exception {
        Path keysDir = Paths.get(keysPath);
        if (!Files.exists(keysDir)) {
            Files.createDirectories(keysDir);
        }
        
        Path publicKeyPath = Paths.get(publicKeyFile);
        Path privateKeyPath = Paths.get(privateKeyFile);
        
        if (Files.exists(publicKeyPath) && Files.exists(privateKeyPath)) {
            // Tải keys từ file
            loadRSAKeyPair();
        } else {
            // Tạo keys mới
            generateAndSaveRSAKeyPair();
        }
    }
    
    /**
     * Tạo AES key ngẫu nhiên
     */
    public SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(aesKeySize);
        return keyGenerator.generateKey();
    }
    
    /**
     * Mã hóa file PDF bằng AES-GCM
     */
    public EncryptionResult encryptPDF(byte[] pdfData, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        
        // Tạo IV ngẫu nhiên
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec);
        
        byte[] encryptedData = cipher.doFinal(pdfData);
        
        return new EncryptionResult(encryptedData, iv, cipher.getIV());
    }
    
    /**
     * Giải mã file PDF bằng AES-GCM
     */
    public byte[] decryptPDF(byte[] encryptedData, SecretKey aesKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmParameterSpec);
        
        return cipher.doFinal(encryptedData);
    }
    
    /**
     * Mã hóa AES key bằng RSA public key
     */
    public byte[] encryptAESKey(SecretKey aesKey) throws Exception {
        if (rsaKeyPair == null) {
            initializeKeys();
        }
        
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, rsaKeyPair.getPublic());
        
        return cipher.doFinal(aesKey.getEncoded());
    }
    
    /**
     * Giải mã AES key bằng RSA private key
     */
    public SecretKey decryptAESKey(byte[] encryptedAESKey) throws Exception {
        if (rsaKeyPair == null) {
            initializeKeys();
        }
        
        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
        
        byte[] decryptedKeyBytes = cipher.doFinal(encryptedAESKey);
        return new SecretKeySpec(decryptedKeyBytes, AES_ALGORITHM);
    }
    
    /**
     * Tạo và lưu RSA key pair mới
     */
    private void generateAndSaveRSAKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyPairGenerator.initialize(rsaKeySize);
        rsaKeyPair = keyPairGenerator.generateKeyPair();
        
        // Lưu public key
        byte[] publicKeyBytes = rsaKeyPair.getPublic().getEncoded();
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder(64, System.lineSeparator().getBytes()).encodeToString(publicKeyBytes) +
                "\n-----END PUBLIC KEY-----";
        Files.write(Paths.get(publicKeyFile), publicKeyPEM.getBytes());
        
        // Lưu private key
        byte[] privateKeyBytes = rsaKeyPair.getPrivate().getEncoded();
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, System.lineSeparator().getBytes()).encodeToString(privateKeyBytes) +
                "\n-----END PRIVATE KEY-----";
        Files.write(Paths.get(privateKeyFile), privateKeyPEM.getBytes());
    }
    
    /**
     * Tải RSA key pair từ file
     */
    private void loadRSAKeyPair() throws Exception {
        // Tải public key
        String publicKeyPEM = Files.readString(Paths.get(publicKeyFile));
        publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----", "")
                                 .replace("-----END PUBLIC KEY-----", "")
                                 .replaceAll("\\s", "");
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(publicKeySpec);
        
        // Tải private key
        String privateKeyPEM = Files.readString(Paths.get(privateKeyFile));
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "")
                                    .replace("-----END PRIVATE KEY-----", "")
                                    .replaceAll("\\s", "");
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(privateKeySpec);
        
        rsaKeyPair = new KeyPair(publicKey, privateKey);
    }
    
    /**
     * Kết quả mã hóa
     */
    public static class EncryptionResult {
        private final byte[] encryptedData;
        private final byte[] iv;
        private final byte[] authTag;
        
        public EncryptionResult(byte[] encryptedData, byte[] iv, byte[] authTag) {
            this.encryptedData = encryptedData;
            this.iv = iv;
            this.authTag = authTag;
        }
        
        public byte[] getEncryptedData() { return encryptedData; }
        public byte[] getIv() { return iv; }
        public byte[] getAuthTag() { return authTag; }
    }
}
