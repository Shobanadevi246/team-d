package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private String role; // ADMIN or STAFF

    @Column(length = 1000)
    private String details;

    private String entityType; // Medicine, Staff, StockTransaction, etc.

    private Long entityId;

    private String ipAddress;

    private LocalDateTime performedAt;

    // Default constructor
    public AuditLog() {}

    // Constructor with all fields
    public AuditLog(Long id, String action, String performedBy, String role, String details, String entityType, Long entityId, String ipAddress, LocalDateTime performedAt) {
        this.id = id;
        this.action = action;
        this.performedBy = performedBy;
        this.role = role;
        this.details = details;
        this.entityType = entityType;
        this.entityId = entityId;
        this.ipAddress = ipAddress;
        this.performedAt = performedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    @PrePersist
    protected void onCreate() {
        performedAt = LocalDateTime.now();
    }
}
