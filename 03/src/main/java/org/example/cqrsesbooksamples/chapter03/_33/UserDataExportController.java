package org.example.cqrsesbooksamples.chapter03._33;

import java.time.Instant;
import java.util.Map;

import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/users")
public class UserDataExportController {
    private final EventStore eventStore;
    private final PersonalInformationStore personalInfoStore;
    private final UserProjectionRepository userProjectionRepository;
    private final ObjectMapper objectMapper;

    public UserDataExportController(EventStore eventStore,
                                    PersonalInformationStore personalInfoStore,
                                    UserProjectionRepository userProjectionRepository,
                                    ObjectMapper objectMapper) {
        this.eventStore = eventStore;
        this.personalInfoStore = personalInfoStore;
        this.userProjectionRepository = userProjectionRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/{userId}/export")
    public ResponseEntity<UserDataExport> exportUserData(@PathVariable String userId) {
        var events = eventStore.readEvents(userId)
            .asStream()
            .map(this::sanitizeEvent)
            .toList();

        var personalInfoId = extractPersonalInfoId(userId);
        var personalInfo = personalInfoStore.retrieve(personalInfoId);

        var export = new UserDataExport(
            userId,
            personalInfo.orElse(null),
            events,
            Instant.now()
        );

        return ResponseEntity.ok(export);
    }

    private Object sanitizeEvent(DomainEventMessage<?> event) {
        // イベントから個人情報の参照IDを除外し、
        // ビジネス上必要な情報のみを含める
        Map<String, Object> payload = objectMapper.convertValue(
            event.getPayload(), new TypeReference<>() {});
        payload.remove("personalInfoId");
        return new EventExportDTO(
            event.getTimestamp(),
            event.getPayloadType().getSimpleName(),
            payload
        );
    }

    private String extractPersonalInfoId(String userId) {
        return userProjectionRepository.findById(userId)
            .map(UserProjectionEntity::getPersonalInfoId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
