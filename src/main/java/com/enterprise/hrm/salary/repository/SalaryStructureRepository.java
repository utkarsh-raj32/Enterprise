package com.enterprise.hrm.salary.repository;

import com.enterprise.hrm.salary.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

    List<SalaryStructure> findByEmployeeIdOrderByEffectiveDateDesc(Long employeeId);

    /** Get the most recent active salary structure for an employee */
    @Query("""
            SELECT ss FROM SalaryStructure ss
            WHERE ss.employee.id = :employeeId
            AND ss.active = true
            ORDER BY ss.effectiveDate DESC
            """)
    Optional<SalaryStructure> findCurrentSalaryStructure(@Param("employeeId") Long employeeId);

    boolean existsByEmployeeIdAndActiveTrue(Long employeeId);
}
