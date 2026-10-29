// 検証補助: 1.1
package org.example.cqrsesbooksamples.chapter01._01;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * イベントを「型・発生時刻・データ」を持つ不変のデータとして表現できることを検証する。
 */
class EventDataRepresentationTest {
    record EventData(String type, Instant timestamp, Map<String, Object> data) {}

    private final ObjectMapper objectMapper = new ObjectMapper()
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    @Test
    void 書籍の3件のイベントをJSONとして再現できる() throws Exception {
        var stocked = new EventData(
            "ProductStocked",
            Instant.parse("2024-12-15T09:30:00Z"),
            Map.of(
                "stockId", "STK-001",
                "warehouseId", "WH-TOKYO",
                "items", List.of(Map.of(
                    "productId", "ABC123", "quantity", 5, "batchNumber", "LOT-2024-45"))));
        var shipped = new EventData(
            "ProductShipped",
            Instant.parse("2024-12-20T14:15:00Z"),
            Map.of(
                "shipmentId", "SHP-002",
                "destinationId", "STORE-05",
                "items", List.of(Map.of(
                    "productId", "ABC123", "quantity", 3, "trackingCode", "TRK-789456"))));
        var restocked = new EventData(
            "ProductStocked",
            Instant.parse("2024-12-28T11:45:00Z"),
            Map.of(
                "stockId", "STK-003",
                "warehouseId", "WH-OSAKA",
                "items", List.of(Map.of(
                    "productId", "ABC123", "quantity", 8, "batchNumber", "LOT-2024-52"))));

        var events = List.of(stocked, shipped, restocked);
        var expectedJson = List.of(
            """
                {
                  "type": "ProductStocked",
                  "timestamp": "2024-12-15T09:30:00Z",
                  "data": {
                    "stockId": "STK-001",
                    "warehouseId": "WH-TOKYO",
                    "items": [
                      {"productId": "ABC123", "quantity": 5, "batchNumber": "LOT-2024-45"}
                    ]
                  }
                }
                """,
            """
                {
                  "type": "ProductShipped",
                  "timestamp": "2024-12-20T14:15:00Z",
                  "data": {
                    "shipmentId": "SHP-002",
                    "destinationId": "STORE-05",
                    "items": [
                      {"productId": "ABC123", "quantity": 3, "trackingCode": "TRK-789456"}
                    ]
                  }
                }
                """,
            """
                {
                  "type": "ProductStocked",
                  "timestamp": "2024-12-28T11:45:00Z",
                  "data": {
                    "stockId": "STK-003",
                    "warehouseId": "WH-OSAKA",
                    "items": [
                      {"productId": "ABC123", "quantity": 8, "batchNumber": "LOT-2024-52"}
                    ]
                  }
                }
                """);

        assertThat(events).hasSameSizeAs(expectedJson);
        for (int i = 0; i < events.size(); i++) {
            var actual = objectMapper.readTree(objectMapper.writeValueAsString(events.get(i)));
            assertThat(actual).isEqualTo(objectMapper.readTree(expectedJson.get(i)));
        }
    }
}
