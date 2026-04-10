package com.pharmacy.repository;

import com.pharmacy.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop100ByOrderByPerformedAtDesc();
    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(String performedBy);
    List<AuditLog> findByRoleOrderByPerformedAtDesc(String role);
    List<AuditLog> findByEntityTypeOrderByPerformedAtDesc(String entityType);
}
