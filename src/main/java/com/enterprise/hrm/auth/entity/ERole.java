package com.enterprise.hrm.auth.entity;

/**
 * ============================================================
 * ROLE ENUM — Defines available roles in the system
 * ============================================================
 *
 * Spring Security expects role names with "ROLE_" prefix
 * when using hasRole() in security config. So if role = ADMIN,
 * Spring checks for authority "ROLE_ADMIN".
 *
 * Our User.getAuthorities() returns GrantedAuthority with
 * "ROLE_" + role.name() format.
 *
 * Roles and their permissions:
 *   ADMIN    — Full access to everything (god mode)
 *   HR       — Manage employees, approve/reject leaves
 *   EMPLOYEE — View own data, apply leaves, check in/out
 */
public enum ERole {
    ADMIN,
    HR,
    EMPLOYEE
}
