package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean readByAdmin = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff targetStaff; // null = all / admin only

    private boolean readByStaff = false;

    private String actionUrl;

    private LocalDateTime createdAt;

    // Default constructor
    public Notification() {}

    // Constructor with all fields
    public Notification(Long id, String title, String message, NotificationType type, boolean readByAdmin, Staff targetStaff, boolean readByStaff, String actionUrl, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.readByAdmin = readByAdmin;
        this.targetStaff = targetStaff;
        this.readByStaff = readByStaff;
        this.actionUrl = actionUrl;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public boolean isReadByAdmin() { return readByAdmin; }
    public void setReadByAdmin(boolean readByAdmin) { this.readByAdmin = readByAdmin; }

    public Staff getTargetStaff() { return targetStaff; }
    public void setTargetStaff(Staff targetStaff) { this.targetStaff = targetStaff; }

    public boolean isReadByStaff() { return readByStaff; }
    public void setReadByStaff(boolean readByStaff) { this.readByStaff = readByStaff; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        LOW_STOCK, EXPIRY_WARNING, STOCK_IN, STOCK_OUT,
        NEW_STAFF, ACCOUNT_LOCKED, PURCHASE_ORDER, SYSTEM
    }
}
