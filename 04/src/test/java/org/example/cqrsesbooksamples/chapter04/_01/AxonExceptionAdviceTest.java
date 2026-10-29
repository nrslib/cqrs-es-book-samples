package org.example.cqrsesbooksamples.chapter04._01;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.axonframework.modelling.command.AggregateNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AxonExceptionAdviceTest {
    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new MissingAggregateController())
        .setControllerAdvice(new AxonExceptionAdvice())
        .build();

    @Test
    void 集約不在例外をHTTP404へ変換する() throws Exception {
        mockMvc.perform(get("/missing-aggregate"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("Document not found"));
    }

    @RestController
    static class MissingAggregateController {
        @GetMapping("/missing-aggregate")
        void getMissingAggregate() {
            throw new AggregateNotFoundException("doc-404", "Document not found");
        }
    }
}
