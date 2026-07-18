package com.enterprise.hrm.salary.repository;

import com.enterprise.hrm.salary.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    List<Payslip> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);

    Optional<Payslip> findByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);

    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, int month, int year);
}
