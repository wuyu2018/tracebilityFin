package com.foodtraceability.traceability.domain.vo;

import java.util.Objects;

public class InspectionResult {
    private final boolean qualified;
    private final String detail;

    private InspectionResult(boolean qualified, String detail) {
        this.qualified = qualified;
        this.detail = detail;
    }

    public static InspectionResult pass() {
        return new InspectionResult(true, null);
    }

    public static InspectionResult fail(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("不合格原因不能为空");
        }
        return new InspectionResult(false, reason);
    }

    public boolean isQualified() { return qualified; }
    public String detail() { return detail; }
    public String displayStatus() { return qualified ? "合格" : "不合格"; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InspectionResult that = (InspectionResult) o;
        return qualified == that.qualified && Objects.equals(detail, that.detail);
    }

    @Override
    public int hashCode() { return Objects.hash(qualified, detail); }
}
