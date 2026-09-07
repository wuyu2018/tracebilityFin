package com.foodtraceability.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class AgentKeyManager {

    private static final Logger log = LoggerFactory.getLogger(AgentKeyManager.class);

    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_CIPHER = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_ALGORITHM = "AES";
    private static final String AES_CIPHER = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final com.foodtraceability.repository.AgentIdentityRepository agentIdentityRepo;

    public AgentKeyManager(com.foodtraceability.repository.AgentIdentityRepository agentIdentityRepo) throws NoSuchAlgorithmException {
        this.agentIdentityRepo = agentIdentityRepo;
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public PublicKey resolveAgentPublicKey(String agentId) {
        var identity = agentIdentityRepo.findByAgentId(agentId);
        if (identity.isPresent() && identity.get().getPublicKey() != null) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(identity.get().getPublicKey());
                return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(keyBytes));
            } catch (Exception e) {
                log.warn("Failed to parse public key for agent {}. Falling back to local key.", agentId, e);
            }
        }
        log.debug("No stored public key for agent {}. Using local public key.", agentId);
        return this.publicKey;
    }

    public String sign(String data) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(sig.sign());
    }

    public boolean verify(String data, String signatureBase64, PublicKey publicKey) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes("UTF-8"));
        return sig.verify(Base64.getDecoder().decode(signatureBase64));
    }

    public String generateAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(AES_KEY_SIZE);
        return Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
    }

    public String encryptAesKey(String aesKey, PublicKey recipientKey) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, recipientKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(aesKey.getBytes("UTF-8")));
    }

    public String decryptAesKey(String encryptedKeyBase64) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedKeyBase64)), "UTF-8");
    }

    public String encryptData(String plaintext, String aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(aesKey), AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
        byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return Base64.getEncoder().encodeToString(combined);
    }

    public String decryptData(String encryptedBase64, String aesKey) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedBase64);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(combined, GCM_IV_LENGTH, ciphertext, 0, ciphertext.length);
        Cipher cipher = Cipher.getInstance(AES_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(Base64.getDecoder().decode(aesKey), AES_ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return new String(cipher.doFinal(ciphertext), "UTF-8");
    }

    public String encryptAesKeyForAgent(String aesKey, String agentId) throws Exception {
        return encryptAesKey(aesKey, resolveAgentPublicKey(agentId));
    }
}
