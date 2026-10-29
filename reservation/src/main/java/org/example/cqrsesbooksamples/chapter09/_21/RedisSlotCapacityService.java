// 紙面照合: 9.18

package org.example.cqrsesbooksamples.chapter09._21;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.example.cqrsesbooksamples.chapter09._01.event.ReservationSlotCreatedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ProcessingGroup("redis-slot-capacity")
@ConditionalOnProperty(
    prefix = "reservation.capacity.redis",
    name = "enabled",
    havingValue = "true")
public class RedisSlotCapacityService {

    private static final String KEY_PREFIX = "slot:remaining:";

    private final RedisTemplate<String, Long> redisTemplate;

    public RedisSlotCapacityService(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 枠作成イベントを購読して空きカウンタを初期化する
    @EventHandler
    public void on(ReservationSlotCreatedEvent event) {
        redisTemplate.opsForValue().set(key(event.slotId()), (long) event.capacity());
    }

    public boolean tryClaimSlot(String slotId) {
        var key = key(slotId);
        var initialized = redisTemplate.hasKey(key);
        if (initialized == null) {
            throw new IllegalStateException("Redisから予約枠の初期化状態を取得できません");
        }
        if (!initialized) {
            // 未初期化の枠は満席扱いにせず、構成不備として止める
            throw new SlotCapacityNotInitializedException(slotId);
        }

        Long remaining = redisTemplate.opsForValue()
            .decrement(key);
        if (remaining == null) {
            throw new IllegalStateException("Redisから残席数を取得できません");
        }
        if (remaining >= 0) {
            return true;
        }
        // 空きがなければ戻す
        redisTemplate.opsForValue()
            .increment(key);
        return false;
    }

    private String key(String slotId) {
        return KEY_PREFIX + slotId;
    }
}
