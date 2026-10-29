package org.example.cqrsesbooksamples.chapter03._33;

import java.time.Instant;

public record EventExportDTO(
    Instant timestamp,
    String eventType,
    Object payload
) {}
