package com.foodtraceability.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class RsaKeyConfig {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyConfig.class);

    @Value("${blockchain.rsa.private-key-path:}")
    private String privateKeyPath;

    @Value("${blockchain.rsa.public-key-path:}")
    private String publicKeyPath;

    @Bean
    public KeyPair blockchainKeyPair() {
        try {
            if (privateKeyPath != null && !privateKeyPath.isBlank()
                    && publicKeyPath != null && !publicKeyPath.isBlank()
                    && Files.exists(Path.of(privateKeyPath))
                    && Files.exists(Path.of(publicKeyPath))) {
                return loadFromFiles();
            }
            log.warn("RSA key files not found, generating new key pair");
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair pair = gen.generateKeyPair();
            log.info("Generated new RSA key pair");
            return pair;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize RSA key pair", e);
        }
    }

    private KeyPair loadFromFiles() throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        byte[] privBytes = parsePem(Files.readString(Path.of(privateKeyPath)));
        PrivateKey priv = factory.generatePrivate(new PKCS8EncodedKeySpec(privBytes));

        byte[] pubBytes = parsePem(Files.readString(Path.of(publicKeyPath)));
        PublicKey pub = factory.generatePublic(new X509EncodedKeySpec(pubBytes));

        log.info("Loaded RSA key pair from: {} / {}", privateKeyPath, publicKeyPath);
        return new KeyPair(pub, priv);
    }

    private byte[] parsePem(String pem) {
        String cleaned = pem
                .replaceAll("-----BEGIN [A-Z ]+-----", "")
                .replaceAll("-----END [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(cleaned);
    }
}
