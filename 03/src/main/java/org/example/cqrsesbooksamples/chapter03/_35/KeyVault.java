package org.example.cqrsesbooksamples.chapter03._35;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * データキーを管理するキー管理システムの簡易実装。
 * キーを削除すると、そのキーで暗号化されたデータは二度と復号できない（Cryptographic Erase）。
 */
public final class KeyVault {
    private static final Map<String, SecretKey> KEYS = new ConcurrentHashMap<>();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private KeyVault() {
    }

    public static void createKey(String keyReference) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            KEYS.put(keyReference, generator.generateKey());
        } catch (Exception e) {
            throw new IllegalStateException("鍵の生成に失敗しました", e);
        }
    }

    public static void deleteKey(String keyReference) {
        KEYS.remove(keyReference);
    }

    public static String encrypt(String keyReference, String plainText) {
        SecretKey key = requireKey(keyReference);
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("暗号化に失敗しました", e);
        }
    }

    public static String decrypt(String keyReference, String encoded) {
        SecretKey key = requireKey(keyReference);
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(combined, IV_LENGTH_BYTES, combined.length - IV_LENGTH_BYTES);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("復号に失敗しました", e);
        }
    }

    private static SecretKey requireKey(String keyReference) {
        SecretKey key = KEYS.get(keyReference);
        if (key == null) {
            throw new IllegalStateException("鍵が存在しません: " + keyReference);
        }
        return key;
    }
}
