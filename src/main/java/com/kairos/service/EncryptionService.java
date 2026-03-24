package com.kairos.service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-CBC encryption service for Kairos entry content.
 *
 * <p>Each encryption operation generates a fresh 16-byte IV which is
 * prepended to the Base64-encoded ciphertext. Decryption extracts
 * the IV from the first 24 Base64 characters before deciphering.
 *
 * <p><strong>Key storage note:</strong> The secret key is currently
 * hard-coded as a constant for development simplicity. In a production
 * build this should be derived from a user-supplied password (PBKDF2)
 * and never stored in source code.
 *
 * @author Kairos
 * @version 1.0.0
 */
public class EncryptionService {

    // ─────────────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────────────

    /** Cipher algorithm, mode, and padding specification. */
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /** Key algorithm identifier for {@link SecretKeySpec}. */
    private static final String KEY_ALGORITHM = "AES";

    /** IV length in bytes (AES block size). */
    private static final int IV_LENGTH = 16;

    /**
     * 256-bit (32-byte) hard-coded secret key.
     *
     * <p><em>Replace with PBKDF2-derived key in production.</em>
     * The key must be exactly 32 bytes for AES-256.
     */
    private static final String SECRET_KEY = "Kairos$SecretKey!2024#Productivity";  // truncated to 32 bytes below

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────────────────────

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Initialises the service by deriving a {@link SecretKey} from the
     * hard-coded constant.
     */
    public EncryptionService() {
        // Use first 32 bytes of UTF-8 encoded key (pad or truncate as needed)
        byte[] keyBytes = new byte[32];
        byte[] rawKey   = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(rawKey, 0, keyBytes, 0, Math.min(rawKey.length, 32));
        this.secretKey    = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        this.secureRandom = new SecureRandom();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Encrypts {@code plainText} using AES-256-CBC with a randomly generated IV.
     *
     * <p>The returned string is formatted as:
     * <pre>Base64(IV) + ":" + Base64(ciphertext)</pre>
     *
     * @param plainText the text to encrypt; {@code null} or blank returns an empty string
     * @return Base64-encoded {@code "IV:ciphertext"} string
     * @throws RuntimeException wrapping any cryptographic failure
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return "";
        try {
            // Generate a fresh IV for every encryption call
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            String ivBase64         = Base64.getEncoder().encodeToString(iv);
            String ciphertextBase64 = Base64.getEncoder().encodeToString(encrypted);

            return ivBase64 + ":" + ciphertextBase64;

        } catch (Exception e) {
            throw new RuntimeException("[EncryptionService] Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts a string produced by {@link #encrypt(String)}.
     *
     * @param encryptedText the {@code "IV:ciphertext"} Base64 string
     * @return original plain text
     * @throws RuntimeException wrapping any cryptographic or format failure
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) return "";
        try {
            String[] parts = encryptedText.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted format — expected 'IV:ciphertext'.");
            }

            byte[] iv         = Base64.getDecoder().decode(parts[0]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[1]);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decrypted = cipher.doFinal(ciphertext);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("[EncryptionService] Decryption failed: " + e.getMessage(), e);
        }
    }
}
