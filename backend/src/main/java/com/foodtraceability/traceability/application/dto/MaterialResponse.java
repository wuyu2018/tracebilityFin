package com.foodtraceability.traceability.application.dto;

import java.time.LocalDateTime;

public record MaterialResponse(Long id, String name, boolean isActive,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {}
