package com.enterprise.hrm.employee.service;

import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.employee.dto.EmployeeRequest;
import com.enterprise.hrm.employee.dto.EmployeeResponse;
import com.enterprise.hrm.employee.entity.EmployeeStatus;
import org.springframework.data.domain.Pageable;

/**
 * Employee service interface.
 */
public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    EmployeeResponse getEmployeeById(Long id);
    EmployeeResponse getEmployeeByCode(String empCode);
    PageResponse<EmployeeResponse> getAllEmployees(Pageable pageable);
    PageResponse<EmployeeResponse> searchEmployees(String query, Long departmentId,
                                                    EmployeeStatus status, Pageable pageable);
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);
    void deleteEmployee(Long id);
}
