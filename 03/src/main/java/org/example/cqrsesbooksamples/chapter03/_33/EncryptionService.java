package org.example.cqrsesbooksamples.chapter03._33;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.springframework.stereotype.Component;

/**
 * 個人情報をフィールド単位でAES/GCM暗号化するサービス。
 */
@Component
public class EncryptionService {
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public EncryptionService() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            this.key = generator.generateKey();
        } catch (Exception e) {
            throw new IllegalStateException("暗号鍵の生成に失敗しました", e);
        }
    }

    public EncryptedPersonalInformation encrypt(PersonalInformation info) {
        return new EncryptedPersonalInformation(
            encryptValue(info.email()),
            encryptValue(info.phoneNumber()),
            encryptValue(info.address()));
    }

    public PersonalInformation decrypt(EncryptedPersonalInformation encrypted) {
        return new PersonalInformation(
            decryptValue(encrypted.email()),
            decryptValue(encrypted.phoneNumber()),
            decryptValue(encrypted.address()));
    }

    private String encryptValue(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            random.nextBytes(iv);
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

    private String decryptValue(String encoded) {
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
}
