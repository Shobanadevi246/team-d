package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "storage_logs")
public class StorageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(nullable = false)
    private String storageZone; // COLD_STORAGE, ROOM_TEMP, CONTROLLED_SUBSTANCE, FLAMMABLE

    private Double temperatureCelsius;

    private Double humidityPercent;

    private String loggedBy;

    @Enumerated(EnumType.STRING)
    private StorageStatus status;

    private String notes;

    private LocalDateTime loggedAt;

    // Default constructor
    public StorageLog() {}

    // Constructor with all fields
    public StorageLog(Long id, Medicine medicine, String storageZone, Double temperatureCelsius, Double humidityPercent, String loggedBy, StorageStatus status, String notes, LocalDateTime loggedAt) {
        this.id = id;
        this.medicine = medicine;
        this.storageZone = storageZone;
        this.temperatureCelsius = temperatureCelsius;
        this.humidityPercent = humidityPercent;
        this.loggedBy = loggedBy;
        this.status = status;
        this.notes = notes;
        this.loggedAt = loggedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getStorageZone() { return storageZone; }
    public void setStorageZone(String storageZone) { this.storageZone = storageZone; }

    public Double getTemperatureCelsius() { return temperatureCelsius; }
    public void setTemperatureCelsius(Double temperatureCelsius) { this.temperatureCelsius = temperatureCelsius; }

    public Double getHumidityPercent() { return humidityPercent; }
    public void setHumidityPercent(Double humidityPercent) { this.humidityPercent = humidityPercent; }

    public String getLoggedBy() { return loggedBy; }
    public void setLoggedBy(String loggedBy) { this.loggedBy = loggedBy; }

    public StorageStatus getStatus() { return status; }
    public void setStatus(StorageStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getLoggedAt() { return loggedAt; }
    public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

    @PrePersist
    protected void onCreate() {
        loggedAt = LocalDateTime.now();
    }

    public enum StorageStatus {
        OPTIMAL, WARNING, CRITICAL, BREACH
    }
}
