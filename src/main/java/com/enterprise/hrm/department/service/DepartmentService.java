package com.enterprise.hrm.department.service;

import com.enterprise.hrm.common.PageResponse;
import com.enterprise.hrm.department.dto.DepartmentRequest;
import com.enterprise.hrm.department.dto.DepartmentResponse;
import com.enterprise.hrm.employee.dto.EmployeeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Department service interface — defines the contract for all department operations.
 */
public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest request);
    DepartmentResponse getDepartmentById(Long id);
    DepartmentResponse getDepartmentByCode(String code);
    List<DepartmentResponse> getAllDepartments();
    List<DepartmentResponse> getActiveDepartments();
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);
    void deleteDepartment(Long id);
    PageResponse<EmployeeResponse> getDepartmentEmployees(Long id, Pageable pageable);
}
