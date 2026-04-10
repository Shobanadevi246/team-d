package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String prescriptionNumber;

    @Column(nullable = false)
    private String patientName;

    private String patientAge;

    private String patientGender;

    private String doctorName;

    private String doctorId;

    private String wardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private String medicineName;

    @Column(nullable = false)
    private Integer quantityDispensed;

    private String dosage;

    private String frequency;

    private Integer durationDays;

    private String instructions;

    private LocalDate prescriptionDate;

    @Enumerated(EnumType.STRING)
    private PrescriptionStatus status = PrescriptionStatus.DISPENSED;

    private boolean dispensedByAdmin = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensed_by_staff_id")
    private Staff dispensedByStaff;

    private LocalDateTime createdAt;

    // Default constructor
    public Prescription() {}

    // Constructor with all fields
    public Prescription(Long id, String prescriptionNumber, String patientName, String patientAge, String patientGender, String doctorName, String doctorId, String wardNumber, Medicine medicine, String medicineName, Integer quantityDispensed, String dosage, String frequency, Integer durationDays, String instructions, LocalDate prescriptionDate, PrescriptionStatus status, boolean dispensedByAdmin, Staff dispensedByStaff, LocalDateTime createdAt) {
        this.id = id;
        this.prescriptionNumber = prescriptionNumber;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.doctorName = doctorName;
        this.doctorId = doctorId;
        this.wardNumber = wardNumber;
        this.medicine = medicine;
        this.medicineName = medicineName;
        this.quantityDispensed = quantityDispensed;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.instructions = instructions;
        this.prescriptionDate = prescriptionDate;
        this.status = status;
        this.dispensedByAdmin = dispensedByAdmin;
        this.dispensedByStaff = dispensedByStaff;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrescriptionNumber() { return prescriptionNumber; }
    public void setPrescriptionNumber(String prescriptionNumber) { this.prescriptionNumber = prescriptionNumber; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientAge() { return patientAge; }
    public void setPatientAge(String patientAge) { this.patientAge = patientAge; }

    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getWardNumber() { return wardNumber; }
    public void setWardNumber(String wardNumber) { this.wardNumber = wardNumber; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getQuantityDispensed() { return quantityDispensed; }
    public void setQuantityDispensed(Integer quantityDispensed) { this.quantityDispensed = quantityDispensed; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }

    public boolean isDispensedByAdmin() { return dispensedByAdmin; }
    public void setDispensedByAdmin(boolean dispensedByAdmin) { this.dispensedByAdmin = dispensedByAdmin; }

    public Staff getDispensedByStaff() { return dispensedByStaff; }
    public void setDispensedByStaff(Staff dispensedByStaff) { this.dispensedByStaff = dispensedByStaff; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (prescriptionDate == null) prescriptionDate = LocalDate.now();
    }

    public enum PrescriptionStatus {
        DISPENSED, PARTIAL, PENDING, CANCELLED
    }
}
