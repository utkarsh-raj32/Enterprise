package com.enterprise.hrm.leave.dto;

import lombok.Data;

/**
 * Request body for approving or rejecting a leave request.
 */
@Data
public class LeaveActionRequest {

    /** Optional note from the approver (reason for rejection etc.) */
    private String approverNote;
}
