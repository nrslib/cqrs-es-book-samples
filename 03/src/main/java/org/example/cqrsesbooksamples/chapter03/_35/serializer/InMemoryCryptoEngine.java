// 検証補助: 3.40
package org.example.cqrsesbooksamples.chapter03._35.serializer;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** 学習用。鍵と破棄記録はメモリ内だけに保持する。永続的な鍵管理の代替ではない。 */
public final class InMemoryCryptoEngine implements CryptoEngine {
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, String> subjectKeys = new HashMap<>();
    private final Map<String, SecretKey> keys = new HashMap<>();
    private final Map<String, String> destroyedKeys = new HashMap<>();

    public synchronized void createKey(String subjectId) {
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("主体IDが必要です");
        }
        if (subjectKeys.containsKey(subjectId)) {
            throw new IllegalStateException("この主体の鍵は作成済みです。破棄後の再作成もできません");
        }
        try {
            var generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            SecretKey key = generator.generateKey();
            String reference = UUID.randomUUID().toString();
            keys.put(reference, key);
            subjectKeys.put(subjectId, reference);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("鍵の生成に失敗しました", e);
        }
    }

    @Override
    public synchronized EncryptedValue encrypt(String subjectId, String context, String plainText) {
        String reference = requireReference(subjectId);
        SecretKey key = requireKey(reference);
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        try {
            Cipher cipher = cipher(Cipher.ENCRYPT_MODE, key, iv, reference, context);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return new EncryptedValue(1, reference, Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(encrypted));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("暗号化に失敗しました", e);
        }
    }

    @Override
    public synchronized String decrypt(String subjectId, String context, EncryptedValue encrypted) {
        if (encrypted == null || encrypted.version() != 1 || encrypted.keyReference() == null
                || encrypted.iv() == null || encrypted.ciphertext() == null) {
            throw new IllegalArgumentException("暗号文の形式が不正です");
        }
        byte[] iv = Base64.getDecoder().decode(encrypted.iv());
        byte[] ciphertext = Base64.getDecoder().decode(encrypted.ciphertext());
        if (iv.length != IV_BYTES || ciphertext.length < TAG_BITS / 8) {
            throw new IllegalArgumentException("暗号文の長さが不正です");
        }
        String reference = requireReference(subjectId);
        if (!reference.equals(encrypted.keyReference())) {
            throw new IllegalStateException("暗号文の鍵と主体が一致しません");
        }
        // 鍵が見つからないだけでは消去済みと判断しない。
        if (subjectId.equals(destroyedKeys.get(reference))) {
            return null;
        }
        SecretKey key = requireKey(reference);
        try {
            Cipher cipher = cipher(Cipher.DECRYPT_MODE, key, iv, reference, context);
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("復号または暗号文の認証に失敗しました", e);
        }
    }

    @Override
    public synchronized void destroyKey(String subjectId) {
        String reference = requireReference(subjectId);
        keys.remove(reference);
        destroyedKeys.put(reference, subjectId);
    }

    private String requireReference(String subjectId) {
        String reference = subjectKeys.get(subjectId);
        if (reference == null) {
            throw new IllegalStateException("主体の鍵が登録されていません");
        }
        return reference;
    }

    private SecretKey requireKey(String reference) {
        SecretKey key = keys.get(reference);
        if (key == null) {
            throw new IllegalStateException("鍵を取得できません");
        }
        return key;
    }

    private Cipher cipher(int mode, SecretKey key, byte[] iv, String reference, String context)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, key, new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD((reference + "\n" + context).getBytes(StandardCharsets.UTF_8));
        return cipher;
    }
}
