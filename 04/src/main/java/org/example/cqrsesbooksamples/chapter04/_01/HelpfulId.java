// 紙面照合: 4.4
package org.example.cqrsesbooksamples.chapter04._01;


public record HelpfulId(String documentId, String userId) {
    @Override
    public String toString() {
        return documentId + "_" + userId;
    }
}
