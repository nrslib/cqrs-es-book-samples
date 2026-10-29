// 紙面照合: 3.38
package org.example.cqrsesbooksamples.chapter03._33;

import java.util.Optional;

import org.springframework.stereotype.Service;


@Service
public class UserApplicationService {
    private final UserProjectionRepository userProjectionRepository;
    private final PersonalInformationStore personalInfoStore;

    public UserApplicationService(UserProjectionRepository userProjectionRepository,
                           PersonalInformationStore personalInfoStore) {
        this.userProjectionRepository = userProjectionRepository;
        this.personalInfoStore = personalInfoStore;
    }

    public Optional<UserDetailDTO> getUserDetail(String userId) {
        var projection = userProjectionRepository.findById(userId);

        return projection.map(p -> {
            var personalInfo = personalInfoStore.retrieve(p.getPersonalInfoId());

            return personalInfo.map(info -> new UserDetailDTO(
                p.getUserId(),
                p.getDisplayName(),
                info.email(),
                info.phoneNumber(),
                info.address(),
                p.getCreatedAt()
            )).orElse(new UserDetailDTO(
                p.getUserId(),
                p.getDisplayName(),
                null,  // 個人情報が削除されている場合
                null,
                null,
                p.getCreatedAt()
            ));
        });
    }
}
