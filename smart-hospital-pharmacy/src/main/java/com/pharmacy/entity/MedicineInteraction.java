package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine_interactions")
public class MedicineInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String medicine1;

    @Column(nullable = false)
    private String medicine2;

    @Column(nullable = false, length = 3000)
    private String interactionResult;

    @Enumerated(EnumType.STRING)
    private SeverityLevel severity;

    private String checkedBy; // admin or staff email

    private LocalDateTime checkedAt;

    // Default constructor
    public MedicineInteraction() {}

    // Constructor with all fields
    public MedicineInteraction(Long id, String medicine1, String medicine2, String interactionResult, SeverityLevel severity, String checkedBy, LocalDateTime checkedAt) {
        this.id = id;
        this.medicine1 = medicine1;
        this.medicine2 = medicine2;
        this.interactionResult = interactionResult;
        this.severity = severity;
        this.checkedBy = checkedBy;
        this.checkedAt = checkedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMedicine1() { return medicine1; }
    public void setMedicine1(String medicine1) { this.medicine1 = medicine1; }

    public String getMedicine2() { return medicine2; }
    public void setMedicine2(String medicine2) { this.medicine2 = medicine2; }

    public String getInteractionResult() { return interactionResult; }
    public void setInteractionResult(String interactionResult) { this.interactionResult = interactionResult; }

    public SeverityLevel getSeverity() { return severity; }
    public void setSeverity(SeverityLevel severity) { this.severity = severity; }

    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }

    @PrePersist
    protected void onCreate() {
        checkedAt = LocalDateTime.now();
    }

    public enum SeverityLevel {
        SAFE, MINOR, MODERATE, MAJOR, CONTRAINDICATED, UNKNOWN
    }
}
