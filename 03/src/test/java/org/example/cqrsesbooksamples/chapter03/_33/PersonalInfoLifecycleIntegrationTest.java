package org.example.cqrsesbooksamples.chapter03._33;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:chapter03_33;DB_CLOSE_DELAY=-1")
class PersonalInfoLifecycleIntegrationTest {
    @Autowired
    private UsersController usersController;

    @Autowired
    private UserPersonalInfoController personalInfoController;

    @Autowired
    private UserDeletionController deletionController;

    @Autowired
    private UserDataExportController exportController;

    @Autowired
    private UserApplicationService queryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 参照IDパターンで登録から訂正と削除まで実行できる() {
        // 登録: 個人情報は外部ストアへ、イベントには参照IDのみ
        String userId = usersController.registerUser(new RegistrationRequest(
            "taro@example.com", "090-0000-0000", "Tokyo", "taro")).join();

        await().untilAsserted(() ->
            assertThat(queryService.getUserDetail(userId)).isPresent());

        // 個人情報はデータベース上では暗号化されている
        String storedEmail = jdbcTemplate.queryForObject(
            "SELECT email FROM personal_information LIMIT 1", String.class);
        assertThat(storedEmail).isNotEqualTo("taro@example.com");

        // クエリ側では復号された個人情報が返る
        var detail = queryService.getUserDetail(userId).orElseThrow();
        assertThat(detail.email()).isEqualTo("taro@example.com");

        // 訂正の権利: 外部ストアの更新だけで完了する
        personalInfoController.updatePersonalInfo(userId,
            new PersonalInfoUpdateRequest(null, "080-1111-1111", null));
        detail = queryService.getUserDetail(userId).orElseThrow();
        assertThat(detail.phoneNumber()).isEqualTo("080-1111-1111");
        assertThat(detail.email()).isEqualTo("taro@example.com");

        // データポータビリティ: イベント履歴と個人情報をエクスポートできる
        var export = exportController.exportUserData(userId).getBody();
        assertThat(export).isNotNull();
        assertThat(export.events()).isNotEmpty();
        // エクスポートされたイベントから個人情報の参照IDが除外されている
        assertThat(export.events().toString()).doesNotContain("personalInfoId");
        assertThat(export.personalInformation().email()).isEqualTo("taro@example.com");

        // 忘れられる権利: 個人情報を削除してもイベントと参照IDは残る
        deletionController.deleteUser(userId).join();
        var afterDelete = queryService.getUserDetail(userId).orElseThrow();
        assertThat(afterDelete.email()).isNull();
        assertThat(afterDelete.phoneNumber()).isNull();
        assertThat(afterDelete.displayName()).isEqualTo("taro");
    }
}
