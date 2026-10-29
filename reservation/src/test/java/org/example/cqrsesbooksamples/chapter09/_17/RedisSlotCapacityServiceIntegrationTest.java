// 検証補助: 9.18
package org.example.cqrsesbooksamples.chapter09._17;

import org.example.cqrsesbooksamples.chapter09._01.event.ReservationSlotCreatedEvent;
import org.example.cqrsesbooksamples.chapter09._21.RedisSlotCapacityService;
import org.example.cqrsesbooksamples.chapter09._21.SlotCapacityNotInitializedException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 実Redis（Testcontainers）上でリスト9.18の定員判定を検証する。
 * Dockerが利用できない環境ではスキップされる。
 */
@Testcontainers(disabledWithoutDocker = true)
class RedisSlotCapacityServiceIntegrationTest {

    @Container
    private static final GenericContainer<?> REDIS =
        new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static RedisTemplate<String, Long> template;
    private static RedisSlotCapacityService service;

    @BeforeAll
    static void setUp() {
        connectionFactory = new LettuceConnectionFactory(
            REDIS.getHost(), REDIS.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();

        template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
        template.afterPropertiesSet();

        service = new RedisSlotCapacityService(template);
    }

    @AfterAll
    static void tearDown() {
        connectionFactory.destroy();
    }

    @Test
    void 実Redisで初期化から満席までの受付と超過拒否とカウンタ復元が成立する() {
        var slotId = "slot-int-1";
        service.on(new ReservationSlotCreatedEvent(
            slotId, LocalDate.of(2026, 9, 1),
            LocalTime.of(10, 0), LocalTime.of(11, 0), 2));

        assertThat(service.tryClaimSlot(slotId)).isTrue();
        assertThat(service.tryClaimSlot(slotId)).isTrue();

        // 定員2を使い切ったあとは拒否され、カウンタは0へ復元される
        assertThat(service.tryClaimSlot(slotId)).isFalse();
        assertThat(service.tryClaimSlot(slotId)).isFalse();
        assertThat(template.opsForValue().get("slot:remaining:" + slotId))
            .isZero();
    }

    @Test
    void 実Redisでも未初期化の枠は構成不備として拒否される() {
        assertThatThrownBy(() -> service.tryClaimSlot("slot-uninitialized"))
            .isInstanceOf(SlotCapacityNotInitializedException.class);
    }
}
