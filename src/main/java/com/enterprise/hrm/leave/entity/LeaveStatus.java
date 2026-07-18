package com.enterprise.hrm.leave.entity;

/**
 * Leave request status enum.
 *
 * State Machine:
 *   PENDING → APPROVED (by HR/ADMIN)
 *   PENDING → REJECTED (by HR/ADMIN)
 *   PENDING → CANCELLED (by the employee)
 *   APPROVED → CANCELLED (if employee cancels approved leave)
 */
public enum LeaveStatus {
    PENDING,    // Awaiting approval
    APPROVED,   // Approved by HR/ADMIN
    REJECTED,   // Rejected by HR/ADMIN
    CANCELLED   // Cancelled by the employee
}
