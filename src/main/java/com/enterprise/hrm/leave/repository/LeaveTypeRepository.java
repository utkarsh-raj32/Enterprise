package com.enterprise.hrm.leave.repository;

import com.enterprise.hrm.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    Optional<LeaveType> findByName(String name);
    boolean existsByName(String name);
    List<LeaveType> findByActiveTrue();
}
