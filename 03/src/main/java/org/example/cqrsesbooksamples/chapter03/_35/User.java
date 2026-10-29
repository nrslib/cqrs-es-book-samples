// 紙面照合: 3.39
package org.example.cqrsesbooksamples.chapter03._35;


public record User(String userId, String keyReference, boolean deleted) {
    public static UserRegisteredEvent create(String userId, String keyReference, PersonalData personalData) {
        var encryptionResult = encryptPersonalData(keyReference, personalData);

        return new UserRegisteredEvent(
            userId,
            keyReference,
            encryptionResult.encryptedData()
        );
    }

    public static User applyEvent(UserRegisteredEvent event) {
        return new User(event.userId(), event.keyReference(), false);
    }

    private static EncryptionResult encryptPersonalData(String keyReference, PersonalData personalData) {
        var plain = personalData.name() + "," + personalData.email();
        return new EncryptionResult(KeyVault.encrypt(keyReference, plain));
    }
}
