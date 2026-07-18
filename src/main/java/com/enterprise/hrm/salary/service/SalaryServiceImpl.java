package com.enterprise.hrm.salary.service;

import com.enterprise.hrm.attendance.repository.AttendanceRepository;
import com.enterprise.hrm.employee.entity.Employee;
import com.enterprise.hrm.employee.repository.EmployeeRepository;
import com.enterprise.hrm.exception.BusinessException;
import com.enterprise.hrm.exception.DuplicateResourceException;
import com.enterprise.hrm.exception.ResourceNotFoundException;
import com.enterprise.hrm.salary.dto.*;
import com.enterprise.hrm.salary.entity.Payslip;
import com.enterprise.hrm.salary.entity.SalaryStructure;
import com.enterprise.hrm.salary.repository.PayslipRepository;
import com.enterprise.hrm.salary.repository.SalaryStructureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Salary service implementation.
 * Handles salary structure management and payslip generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalaryServiceImpl implements SalaryService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final PayslipRepository payslipRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public SalaryStructureResponse createSalaryStructure(SalaryStructureRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        // Deactivate current structure when creating new one
        salaryStructureRepository.findCurrentSalaryStructure(employee.getId())
                .ifPresent(current -> {
                    current.setActive(false);
                    salaryStructureRepository.save(current);
                });

        SalaryStructure structure = SalaryStructure.builder()
                .employee(employee)
                .basicSalary(request.getBasicSalary())
                .hra(request.getHra())
                .allowances(request.getAllowances())
                .deductions(request.getDeductions())
                .effectiveDate(request.getEffectiveDate())
                .active(true)
                .build();
        // @PrePersist calculates netSalary
        return mapStructureToResponse(salaryStructureRepository.save(structure));
    }

    @Override
    public SalaryStructureResponse getCurrentSalaryStructure(Long employeeId) {
        return salaryStructureRepository.findCurrentSalaryStructure(employeeId)
                .map(this::mapStructureToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "employeeId", employeeId));
    }

    @Override
    public List<SalaryStructureResponse> getSalaryHistory(Long employeeId) {
        return salaryStructureRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId)
                .stream()
                .map(this::mapStructureToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SalaryStructureResponse updateSalaryStructure(Long id, SalaryStructureRequest request) {
        SalaryStructure structure = salaryStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryStructure", "id", id));
        structure.setBasicSalary(request.getBasicSalary());
        structure.setHra(request.getHra());
        structure.setAllowances(request.getAllowances());
        structure.setDeductions(request.getDeductions());
        structure.setEffectiveDate(request.getEffectiveDate());
        structure.calculateNetSalary();
        return mapStructureToResponse(salaryStructureRepository.save(structure));
    }

    @Override
    @Transactional
    public PayslipResponse generatePayslip(PayslipGenerateRequest request) {
        // Check if payslip already generated
        if (payslipRepository.existsByEmployeeIdAndMonthAndYear(
                request.getEmployeeId(), request.getMonth(), request.getYear())) {
            throw new DuplicateResourceException(
                    "Payslip already generated for employee " + request.getEmployeeId() +
                    " for " + request.getMonth() + "/" + request.getYear());
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        SalaryStructure structure = salaryStructureRepository
                .findCurrentSalaryStructure(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException(
                        "No active salary structure found for employee", "NO_SALARY_STRUCTURE"));

        // Get attendance data for the month
        List<?> monthAttendance = attendanceRepository.findMonthlyAttendance(
                request.getEmployeeId(), request.getYear(), request.getMonth());

        int workingDays = YearMonth.of(request.getYear(), request.getMonth()).lengthOfMonth();
        int presentDays = monthAttendance.size();

        Payslip payslip = Payslip.builder()
                .employee(employee)
                .salaryStructure(structure)
                .month(request.getMonth())
                .year(request.getYear())
                .basicSalary(structure.getBasicSalary())
                .hra(structure.getHra())
                .allowances(structure.getAllowances())
                .deductions(structure.getDeductions())
                .netSalary(structure.getNetSalary())
                .workingDays(workingDays)
                .presentDays(presentDays)
                .build();

        return mapPayslipToResponse(payslipRepository.save(payslip));
    }

    @Override
    public List<PayslipResponse> getEmployeePayslips(Long employeeId) {
        return payslipRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId)
                .stream()
                .map(this::mapPayslipToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PayslipResponse getPayslipById(Long id) {
        return mapPayslipToResponse(
                payslipRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Payslip", "id", id))
        );
    }

    // ─── MAPPERS ───────────────────────────────────────────────────

    private SalaryStructureResponse mapStructureToResponse(SalaryStructure s) {
        return SalaryStructureResponse.builder()
                .id(s.getId())
                .employeeId(s.getEmployee().getId())
                .employeeName(s.getEmployee().getFirstName() + " " + s.getEmployee().getLastName())
                .basicSalary(s.getBasicSalary())
                .hra(s.getHra())
                .allowances(s.getAllowances())
                .deductions(s.getDeductions())
                .netSalary(s.getNetSalary())
                .effectiveDate(s.getEffectiveDate())
                .active(s.isActive())
                .build();
    }

    private PayslipResponse mapPayslipToResponse(Payslip p) {
        return PayslipResponse.builder()
                .id(p.getId())
                .employeeId(p.getEmployee().getId())
                .employeeName(p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName())
                .employeeCode(p.getEmployee().getEmpCode())
                .departmentName(p.getEmployee().getDepartment() != null
                        ? p.getEmployee().getDepartment().getName() : null)
                .designation(p.getEmployee().getDesignation())
                .month(p.getMonth())
                .year(p.getYear())
                .basicSalary(p.getBasicSalary())
                .hra(p.getHra())
                .allowances(p.getAllowances())
                .deductions(p.getDeductions())
                .netSalary(p.getNetSalary())
                .workingDays(p.getWorkingDays())
                .presentDays(p.getPresentDays())
                .generatedAt(p.getGeneratedAt())
                .pdfUrl(p.getPdfUrl())
                .build();
    }
}
