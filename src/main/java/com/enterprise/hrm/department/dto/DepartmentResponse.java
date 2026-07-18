package com.enterprise.hrm.department.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Department response DTO — returned to clients.
 * Never expose the entity directly — use DTOs to control
 * what data is sent over the wire.
 */
@Data
@Builder
public class DepartmentResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private String managerName;
    private boolean active;
    private long employeeCount;      // Computed field — not in entity
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
