package org.example.cqrsesbooksamples.chapter10._03;

public record OrderId(String value) {
    @Override
    public String toString() {
        return value;
    }
}
