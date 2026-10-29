package org.example.cqrsesbooksamples.chapter10._11;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_views")
public class DocumentView {
    @Id
    private String documentId;
    private int helpfulCount;

    protected DocumentView() {
    }

    public DocumentView(String documentId, int helpfulCount) {
        this.documentId = documentId;
        this.helpfulCount = helpfulCount;
    }

    public String getDocumentId() {
        return documentId;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }
}
