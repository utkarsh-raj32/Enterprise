package com.enterprise.hrm.employee.entity;

/**
 * Employee employment status enum.
 * Stored as STRING in database for readability.
 */
public enum EmployeeStatus {
    ACTIVE,      // Currently employed
    INACTIVE,    // Temporarily inactive / on leave of absence
    TERMINATED   // No longer employed
}
