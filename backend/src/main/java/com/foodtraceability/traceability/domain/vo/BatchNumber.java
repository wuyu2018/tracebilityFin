package com.foodtraceability.traceability.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public class BatchNumber {
    private static final Pattern FORMAT = Pattern.compile("B\\d{8}\\d{4}");

    private final String value;

    private BatchNumber(String value) {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("批次号格式无效: " + value + "，须为 B+yyyyMMdd+0001");
        }
        this.value = value;
    }

    public static BatchNumber of(String value) {
        return new BatchNumber(value);
    }

    public static BatchNumber generate(String dateStr, long seq) {
        return new BatchNumber("B" + dateStr + String.format("%04d", seq));
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchNumber that = (BatchNumber) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
