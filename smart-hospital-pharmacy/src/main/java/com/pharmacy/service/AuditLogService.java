package com.pharmacy.service;

import com.pharmacy.entity.AuditLog;
import com.pharmacy.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String performedBy, String role,
                    String details, String entityType, Long entityId, String ip) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setRole(role);
        log.setDetails(details);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setIpAddress(ip);
        auditLogRepository.save(log);
    }

    public void log(String action, String performedBy, String role, String details) {
        log(action, performedBy, role, details, null, null, null);
    }

    public List<AuditLog> getAll() {
        return auditLogRepository.findTop100ByOrderByPerformedAtDesc();
    }

    public List<AuditLog> getByUser(String user) {
        return auditLogRepository.findByPerformedByOrderByPerformedAtDesc(user);
    }

    public List<AuditLog> getByRole(String role) {
        return auditLogRepository.findByRoleOrderByPerformedAtDesc(role);
    }
}
