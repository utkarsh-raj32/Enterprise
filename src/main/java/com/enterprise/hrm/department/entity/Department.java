package com.enterprise.hrm.department.entity;

import com.enterprise.hrm.common.BaseEntity;
import com.enterprise.hrm.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * DEPARTMENT ENTITY
 * ============================================================
 *
 * RELATIONSHIP: Department (1) ──── (Many) Employee
 *
 * @OneToMany(mappedBy = "department"):
 *   Declares the "one" side of the One-to-Many relationship.
 *   'mappedBy = "department"' means:
 *     "The foreign key is in the Employee table, in the 'department' field"
 *   This side does NOT own the relationship (no FK here).
 *
 * WHY mappedBy?
 *   In bidirectional relationships, one side must be the "owner"
 *   (has the FK column). mappedBy tells JPA this side is the "inverse".
 *   Without mappedBy, JPA would create a join table — wrong behavior.
 *
 * cascade = CascadeType.ALL:
 *   Operations on Department cascade to employees:
 *   PERSIST, MERGE, REMOVE, REFRESH, DETACH
 *   WARNING: CascadeType.REMOVE means deleting a Department
 *   deletes ALL employees! Use orphanRemoval carefully.
 *
 *   For this system, we use CascadeType.PERSIST and MERGE only —
 *   employees shouldn't be deleted when dept is deleted (use soft delete).
 *
 * fetch = FetchType.LAZY (DEFAULT for @OneToMany):
 *   Employees are NOT loaded when a Department is fetched.
 *   Only loaded when department.getEmployees() is called within transaction.
 *   WHY LAZY? A department could have 1000+ employees — loading
 *   all of them on every department fetch would kill performance.
 *
 * orphanRemoval = false:
 *   If an employee is removed from the collection, do NOT delete it from DB.
 *   (The employee just gets a different department or null department)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Short unique code for the department.
     * E.g., "ENG", "HR", "FIN", "OPS"
     */
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(length = 500)
    private String description;

    /**
     * The manager's name (simplified). In a full system,
     * this would be @ManyToOne → Employee (the manager).
     */
    @Column(length = 150)
    private String managerName;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * BIDIRECTIONAL @OneToMany
     *
     * mappedBy = "department" — the 'department' field in Employee owns the FK
     * cascade = PERSIST, MERGE — saving dept also saves/updates employees
     * fetch = LAZY — don't load employees unless explicitly requested
     *
     * @Builder.Default — Lombok @Builder requires default for collections
     */
    @OneToMany(
        mappedBy   = "department",
        cascade    = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch      = FetchType.LAZY
    )
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();
}
