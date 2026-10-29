package org.example.cqrsesbooksamples.chapter04._34;

public record AccountId(String value) {
    @Override
    public String toString() {
        return value;
    }
}
