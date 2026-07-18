package com.enterprise.hrm.employee.repository;

import com.enterprise.hrm.employee.entity.Employee;
import com.enterprise.hrm.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * EMPLOYEE REPOSITORY — Advanced Queries
 * ============================================================
 *
 * Pagination with Spring Data JPA:
 *   Methods that accept Pageable parameter automatically return
 *   a Page<T> with pagination metadata.
 *
 *   Usage in service:
 *     Pageable pageable = PageRequest.of(0, 10, Sort.by("lastName").ascending());
 *     Page<Employee> page = repo.findAll(pageable);
 *
 * @Query with JPQL:
 *   JPQL (Java Persistence Query Language) operates on entity objects
 *   and fields, not DB tables and columns. Hibernate translates JPQL to SQL.
 *
 *   Benefits over native SQL:
 *   • Database-agnostic (works with MySQL, PostgreSQL, Oracle, etc.)
 *   • Compile-time validation (Spring validates on startup)
 *   • Entity-aware (uses Java field names, not column names)
 *
 * nativeQuery = true:
 *   Executes raw SQL. Use when JPQL can't express complex queries
 *   (e.g., database-specific functions, complex GROUP BY, window functions).
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmpCode(String empCode);

    boolean existsByEmail(String email);

    boolean existsByEmpCode(String empCode);

    /**
     * Find employees by department — returns Page for pagination.
     * The 'Pageable' parameter tells Spring Data to apply
     * LIMIT, OFFSET, and ORDER BY automatically.
     */
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    /**
     * Filter by status with pagination.
     * Generated JPQL: SELECT e FROM Employee e WHERE e.status = :status
     */
    Page<Employee> findByStatus(EmployeeStatus status, Pageable pageable);

    /**
     * SEARCH EMPLOYEES — Custom JPQL with multiple optional filters.
     *
     * LOWER() — case-insensitive search
     * LIKE '%:query%' — partial match
     * :#{#query == null ? '%' : ('%' || lower(#query) || '%')}
     *   — if query is null, match everything (%)
     *
     * This is a dynamic query approach. For complex dynamic queries,
     * consider Spring Data Specifications (Criteria API) or QueryDSL.
     *
     * @Param("query") links the method parameter to :query in JPQL.
     */
    @Query("""
            SELECT e FROM Employee e
            JOIN FETCH e.department d
            WHERE (:query IS NULL OR
                   LOWER(e.firstName)   LIKE LOWER(CONCAT('%', :query, '%')) OR
                   LOWER(e.lastName)    LIKE LOWER(CONCAT('%', :query, '%')) OR
                   LOWER(e.email)       LIKE LOWER(CONCAT('%', :query, '%')) OR
                   LOWER(e.designation) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            AND (:departmentId IS NULL OR d.id = :departmentId)
            AND (:status IS NULL OR e.status = :status)
            """)
    Page<Employee> searchEmployees(
            @Param("query") String query,
            @Param("departmentId") Long departmentId,
            @Param("status") EmployeeStatus status,
            Pageable pageable
    );

    /**
     * Find all employees in a department (for department detail view).
     * JOIN FETCH prevents N+1 — loads department with employees in one query.
     */
    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.department.id = :deptId")
    List<Employee> findByDepartmentIdWithDepartment(@Param("deptId") Long deptId);

    /**
     * Count employees by department — for department list view statistics.
     */
    long countByDepartmentId(Long departmentId);

    /**
     * Count active employees.
     */
    long countByStatus(EmployeeStatus status);
}
