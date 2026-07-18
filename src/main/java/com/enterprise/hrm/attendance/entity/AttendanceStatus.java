package com.enterprise.hrm.attendance.entity;

/**
 * Attendance status for a given day.
 */
public enum AttendanceStatus {
    PRESENT,    // Employee checked in on time
    LATE,       // Employee checked in after the standard start time (e.g., 9:30 AM)
    HALF_DAY,   // Employee worked less than half a day
    ABSENT,     // Employee did not check in
    ON_LEAVE,   // Employee is on approved leave (synced from leave module)
    HOLIDAY     // Official company holiday
}
