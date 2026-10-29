// 紙面照合: 4.12
package org.example.cqrsesbooksamples.chapter04._01;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "document_views")
public class DocumentView {
    @Id
    private String documentId;
    private String title;
    private String content;
    private int helpfulCount;

    protected DocumentView() {}

    public DocumentView(String documentId, String title, String content, int helpfulCount) {
        this.documentId = documentId;
        this.title = title;
        this.content = content;
        this.helpfulCount = helpfulCount;
    }

    public String getDocumentId() { return documentId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public int getHelpfulCount() { return helpfulCount; }
}
