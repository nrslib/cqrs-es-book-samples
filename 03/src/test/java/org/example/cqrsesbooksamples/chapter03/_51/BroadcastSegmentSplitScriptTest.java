// 検証補助: 3.19
package org.example.cqrsesbooksamples.chapter03._51;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class BroadcastSegmentSplitScriptTest {
    @TempDir Path temporaryDirectory;

    static Stream<Arguments> responses() {
        return Stream.of(
            Arguments.of(List.of("{\"status\":\"SUCCESS\"}"), 200, 1, true),
            Arguments.of(List.of("{\"status\":\"NOT_OWNED\"}", "{\"status\":\"SUCCESS\"}"), 200, 2, true),
            Arguments.of(List.of("{\"status\":\"NOT_OWNED\"}"), 200, 10, false),
            Arguments.of(List.of("{\"status\":\"FAILED\"}"), 200, 1, false),
            Arguments.of(List.of("{\"status\":\"UNKNOWN\"}"), 200, 1, false),
            Arguments.of(List.of("{}"), 200, 1, false),
            Arguments.of(List.of("broken json"), 200, 1, false),
            Arguments.of(List.of("{\"status\":\"SUCCESS\"}"), 500, 1, false)
        );
    }

    @ParameterizedTest
    @MethodSource("responses")
    void 実スクリプトは成功で停止し所有者不一致だけ再試行する(
            List<String> bodies, int httpStatus, int expectedRequests, boolean success) throws Exception {
        var requests = new AtomicInteger();
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                var index = requests.getAndIncrement();
                var body = bodies.get(Math.min(index, bodies.size() - 1)).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(httpStatus, body.length);
                exchange.getResponseBody().write(body);
            }
        });
        server.start();
        Process process = null;
        try {
            var output = temporaryDirectory.resolve("script.log");
            var builder = new ProcessBuilder("bash", "snippets/broadcast-segment-split.sh")
                .redirectErrorStream(true).redirectOutput(output.toFile());
            builder.environment().put("http_proxy", "http://127.0.0.1:" + server.getAddress().getPort());
            builder.environment().put("no_proxy", "");
            builder.environment().put("NO_PROXY", "");
            process = builder.start();
            assertThat(process.waitFor(30, TimeUnit.SECONDS)).isTrue();
            var log = Files.readString(output);
            assertThat(process.exitValue() == 0).as(log).isEqualTo(success);
            assertThat(requests.get()).as(log).isEqualTo(expectedRequests);
        } finally {
            if (process != null && process.isAlive()) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly().waitFor();
            }
            server.stop(0);
        }
    }
}
