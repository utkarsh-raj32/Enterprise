package com.enterprise.hrm.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ============================================================
 * ROLE ENTITY
 * ============================================================
 *
 * @Entity:
 *   Marks this POJO as a JPA entity — Hibernate will manage it.
 *   Hibernate maps this class to a database table.
 *   ENTITY LIFECYCLE: TRANSIENT → MANAGED → DETACHED → REMOVED
 *
 * @Table(name = "roles"):
 *   Specifies the exact table name. Without this, JPA uses the
 *   class name (Role → ROLE). Explicit naming is enterprise best practice.
 *
 * @Id:
 *   Marks the primary key field.
 *   Every JPA entity MUST have exactly one @Id field.
 *
 * @GeneratedValue(strategy = GenerationType.IDENTITY):
 *   Delegates ID generation to the database (AUTO_INCREMENT in MySQL).
 *   Other strategies:
 *   • SEQUENCE — uses DB sequence objects (preferred for Oracle)
 *   • TABLE — uses a separate table (portable but slow)
 *   • AUTO — Hibernate picks strategy based on dialect
 *   • UUID — generates UUID strings (good for distributed systems)
 *
 * @Enumerated(EnumType.STRING):
 *   Stores the enum as its String name ("ADMIN") in the database.
 *   WHY STRING over ORDINAL?
 *   ORDINAL stores the position (0, 1, 2). If enum order changes,
 *   stored data becomes corrupted. STRING is self-documenting and safe.
 *
 * @Data (Lombok):
 *   Generates: getters, setters, equals(), hashCode(), toString().
 *   WARNING: Avoid @Data on JPA entities with circular relationships
 *   as toString() / hashCode() can cause infinite recursion.
 *   For entities, prefer @Getter @Setter @ToString(exclude=...) etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Enumerated(STRING) — stored as "ADMIN", "HR", "EMPLOYEE"
     * @Column(unique = true) — each role name must be unique in the table
     * length = 20 — matches the longest possible enum name
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private ERole name;
}
