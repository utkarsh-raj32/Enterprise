package com.enterprise.hrm.bootstrap;

import com.enterprise.hrm.auth.entity.ERole;
import com.enterprise.hrm.auth.entity.Role;
import com.enterprise.hrm.auth.repository.RoleRepository;
import com.enterprise.hrm.department.entity.Department;
import com.enterprise.hrm.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * ============================================================
 * DATA SEEDER
 * ============================================================
 * Runs automatically on application startup to ensure essential
 * database records exist (like Roles) in a fresh database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Seeding initial database roles...");
        Arrays.stream(ERole.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                log.info("Created missing role: {}", roleName);
            }
        });
        log.info("Role seeding completed.");

        log.info("Seeding initial database departments...");
        if (departmentRepository.count() == 0) {
            Department dept = new Department();
            dept.setCode("ENG-001");
            dept.setName("Engineering");
            dept.setDescription("Software Engineering and Development");
            departmentRepository.save(dept);
            log.info("Created missing department: Engineering");
        }
        log.info("Department seeding completed.");
    }
}
