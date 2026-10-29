package org.example.cqrsesbooksamples.chapter03._33;

import java.time.Instant;
import java.util.List;

public record UserDataExport(
    String userId,
    PersonalInformation personalInformation,
    List<Object> events,
    Instant exportedAt
) {}
