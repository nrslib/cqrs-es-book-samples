package org.example.cqrsesbooksamples.chapter09._21;

import org.example.cqrsesbooksamples.chapter09._01.event.ReservationSlotCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class RedisSlotCapacityServiceTest {

    private static final String KEY = "slot:remaining:s-1";

    private final RedisTemplate<String, Long> redisTemplate = mock(RedisTemplate.class);
    private final ValueOperations<String, Long> valueOperations = mock(ValueOperations.class);
    private final RedisSlotCapacityService service = new RedisSlotCapacityService(redisTemplate);

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 枠作成で初期化して満席まで受け付け超過時はカウンタを復元する() {
        var initialized = new AtomicBoolean();
        var remaining = new AtomicLong();
        doAnswer(invocation -> {
            remaining.set(invocation.getArgument(1));
            initialized.set(true);
            return null;
        }).when(valueOperations).set(anyString(), anyLong());
        when(redisTemplate.hasKey(KEY)).thenAnswer(invocation -> initialized.get());
        when(valueOperations.decrement(KEY)).thenAnswer(invocation -> remaining.decrementAndGet());
        when(valueOperations.increment(KEY)).thenAnswer(invocation -> remaining.incrementAndGet());

        service.on(new ReservationSlotCreatedEvent(
            "s-1", LocalDate.now().plusDays(1),
            LocalTime.of(10, 0), LocalTime.of(11, 0), 2));

        assertThat(service.tryClaimSlot("s-1")).isTrue();
        assertThat(service.tryClaimSlot("s-1")).isTrue();
        assertThat(service.tryClaimSlot("s-1")).isFalse();
        assertThat(remaining).hasValue(0);
        verify(valueOperations).increment(KEY);
    }

    @Test
    void 未初期化の枠は暗黙にカウンタを作らずエラーにする() {
        when(redisTemplate.hasKey(KEY)).thenReturn(false);

        assertThatThrownBy(() -> service.tryClaimSlot("s-1"))
            .isInstanceOf(SlotCapacityNotInitializedException.class)
            .hasMessageContaining("s-1");
        verify(valueOperations, never()).decrement(KEY);
    }
}
