package com.enterprise.hrm.salary.service;

import com.enterprise.hrm.salary.dto.*;
import java.util.List;

public interface SalaryService {
    SalaryStructureResponse createSalaryStructure(SalaryStructureRequest request);
    SalaryStructureResponse getCurrentSalaryStructure(Long employeeId);
    List<SalaryStructureResponse> getSalaryHistory(Long employeeId);
    SalaryStructureResponse updateSalaryStructure(Long id, SalaryStructureRequest request);
    PayslipResponse generatePayslip(PayslipGenerateRequest request);
    List<PayslipResponse> getEmployeePayslips(Long employeeId);
    PayslipResponse getPayslipById(Long id);
}
