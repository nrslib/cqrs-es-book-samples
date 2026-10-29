// 紙面照合: 4.17
package org.example.cqrsesbooksamples.chapter04._01;

import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/documents")
public class DocumentQueryController {
    private final QueryGateway queryGateway;

    public DocumentQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/{documentId}")
    public DocumentView get(@PathVariable String documentId) {
        var query = new FindDocumentViewQuery(documentId);
        return queryGateway.query(query, DocumentView.class).join();
    }
}
