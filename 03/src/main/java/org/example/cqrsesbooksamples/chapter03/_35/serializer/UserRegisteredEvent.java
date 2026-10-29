// 紙面照合: 3.40
package org.example.cqrsesbooksamples.chapter03._35.serializer;

public record UserRegisteredEvent(
        @DataSubjectId String userId,
        @PersonalData String name,
        @PersonalData String email) {
}
