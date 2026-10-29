// 検証補助: 3.40
package org.example.cqrsesbooksamples.chapter03._35.serializer;

public interface CryptoEngine {
    EncryptedValue encrypt(String subjectId, String context, String plainText);

    /** 正式に鍵を破棄した場合だけnullを返す。取得障害や未知の鍵は例外にする。 */
    String decrypt(String subjectId, String context, EncryptedValue encrypted);

    void destroyKey(String subjectId);

    record EncryptedValue(int version, String keyReference, String iv, String ciphertext) {
    }
}
