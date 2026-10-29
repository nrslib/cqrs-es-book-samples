package org.example.cqrsesbooksamples.chapter03._55;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

// 削除済み（旧 3.30）
/**
 * 管理操作専用エンドポイントのService定義（旧 3.30）の検証。
 * snippets/admin-service.yaml がKubernetesのService定義として解析でき、
 * 管理ポート8081だけを公開する構成になっていることを確かめる。
 * （クラスタへの適用は行わない。構造の妥当性までを検証する）
 */
class AdminServiceManifestTest {

    @Test
    @SuppressWarnings("unchecked")
    void 管理ポートだけを公開するService定義になっている() throws IOException {
        Map<String, Object> manifest = new Yaml().load(
                Files.readString(Path.of("snippets/admin-service.yaml")));

        assertThat(manifest.get("apiVersion")).isEqualTo("v1");
        assertThat(manifest.get("kind")).isEqualTo("Service");

        var metadata = (Map<String, Object>) manifest.get("metadata");
        assertThat(metadata.get("name")).isEqualTo("axon-app-admin");

        var spec = (Map<String, Object>) manifest.get("spec");
        assertThat(spec.get("type")).isEqualTo("NodePort");

        var selector = (Map<String, Object>) spec.get("selector");
        assertThat(selector).containsEntry("app", "axon-app");

        var ports = (List<Map<String, Object>>) spec.get("ports");
        assertThat(ports).hasSize(1);
        assertThat(ports.get(0))
                .containsEntry("port", 8081)
                .containsEntry("targetPort", 8081)
                .containsEntry("name", "admin");
    }
}
