package com.foodtraceability.traceability.domain.vo;

import java.util.Objects;

public class Quantity {
    private final double value;
    private final String unit;

    private Quantity(double value, String unit) {
        if (value < 0) {
            throw new IllegalArgumentException("数量不能为负数: " + value);
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("单位不能为空");
        }
        this.value = value;
        this.unit = unit;
    }

    public static Quantity of(double value, String unit) {
        return new Quantity(value, unit);
    }

    public double value() {
        return value;
    }

    public String unit() {
        return unit;
    }

    public boolean isZero() {
        return value == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return Double.compare(value, quantity.value) == 0 && Objects.equals(unit, quantity.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    @Override
    public String toString() {
        return value + " " + unit;
    }
}
