package org.example.cqrsesbooksamples.chapter04._37;

public record TransferId(String value) {
    @Override
    public String toString() {
        return value;
    }
}
