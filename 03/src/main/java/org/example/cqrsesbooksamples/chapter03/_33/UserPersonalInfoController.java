package org.example.cqrsesbooksamples.chapter03._33;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserPersonalInfoController {
    private final PersonalInformationStore personalInfoStore;
    private final UserProjectionRepository userProjectionRepository;

    public UserPersonalInfoController(PersonalInformationStore personalInfoStore,
                                      UserProjectionRepository userProjectionRepository) {
        this.personalInfoStore = personalInfoStore;
        this.userProjectionRepository = userProjectionRepository;
    }

    @PatchMapping("/{userId}/personal-info")
    public ResponseEntity<Void> updatePersonalInfo(
            @PathVariable String userId,
            @RequestBody PersonalInfoUpdateRequest request) {

        var projection = userProjectionRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        var currentInfo = personalInfoStore.retrieve(projection.getPersonalInfoId())
            .orElseThrow(() -> new PersonalInfoNotFoundException(projection.getPersonalInfoId()));

        var updatedInfo = new PersonalInformation(
            request.email() != null ? request.email() : currentInfo.email(),
            request.phoneNumber() != null ? request.phoneNumber() : currentInfo.phoneNumber(),
            request.address() != null ? request.address() : currentInfo.address()
        );

        personalInfoStore.update(projection.getPersonalInfoId(), updatedInfo);

        return ResponseEntity.noContent().build();
    }
}
