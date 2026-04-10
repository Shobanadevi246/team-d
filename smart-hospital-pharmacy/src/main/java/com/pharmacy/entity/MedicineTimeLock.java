package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine_time_locks")
public class MedicineTimeLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private String lockReason; // e.g. "Controlled substance", "Narcotics"

    @Column(nullable = false)
    private LocalTime allowedFromTime;

    @Column(nullable = false)
    private LocalTime allowedToTime;

    private String allowedDays; // e.g. "MON,TUE,WED,THU,FRI"

    private boolean requiresAdminApproval = false;

    private boolean active = true;

    private String createdBy;

    private LocalDateTime createdAt;

    // Default constructor
    public MedicineTimeLock() {}

    // Constructor with all fields
    public MedicineTimeLock(Long id, Medicine medicine, String lockReason, LocalTime allowedFromTime, LocalTime allowedToTime, String allowedDays, boolean requiresAdminApproval, boolean active, String createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.medicine = medicine;
        this.lockReason = lockReason;
        this.allowedFromTime = allowedFromTime;
        this.allowedToTime = allowedToTime;
        this.allowedDays = allowedDays;
        this.requiresAdminApproval = requiresAdminApproval;
        this.active = active;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getLockReason() { return lockReason; }
    public void setLockReason(String lockReason) { this.lockReason = lockReason; }

    public LocalTime getAllowedFromTime() { return allowedFromTime; }
    public void setAllowedFromTime(LocalTime allowedFromTime) { this.allowedFromTime = allowedFromTime; }

    public LocalTime getAllowedToTime() { return allowedToTime; }
    public void setAllowedToTime(LocalTime allowedToTime) { this.allowedToTime = allowedToTime; }

    public String getAllowedDays() { return allowedDays; }
    public void setAllowedDays(String allowedDays) { this.allowedDays = allowedDays; }

    public boolean isRequiresAdminApproval() { return requiresAdminApproval; }
    public void setRequiresAdminApproval(boolean requiresAdminApproval) { this.requiresAdminApproval = requiresAdminApproval; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isCurrentlyAccessible() {
        LocalTime now = LocalTime.now();
        return now.isAfter(allowedFromTime) && now.isBefore(allowedToTime);
    }
}
