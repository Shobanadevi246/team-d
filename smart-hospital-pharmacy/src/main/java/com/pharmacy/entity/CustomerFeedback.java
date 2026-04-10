package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_feedback")
public class CustomerFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType customerType;

    @Column(nullable = false)
    private String medicineName;

    @Column(nullable = false)
    private String medicineBrand;

    @Column(nullable = false, length = 1000)
    private String review;

    @Column(nullable = false)
    private int rating = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    private boolean submittedByAdmin = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff submittedByStaff;

    private LocalDateTime createdAt;

    // Default constructor
    public CustomerFeedback() {}

    // Constructor with all fields
    public CustomerFeedback(Long id, String customerName, CustomerType customerType, String medicineName, String medicineBrand, String review, int rating, Medicine medicine, boolean submittedByAdmin, Staff submittedByStaff, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.customerType = customerType;
        this.medicineName = medicineName;
        this.medicineBrand = medicineBrand;
        this.review = review;
        this.rating = rating;
        this.medicine = medicine;
        this.submittedByAdmin = submittedByAdmin;
        this.submittedByStaff = submittedByStaff;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getMedicineBrand() { return medicineBrand; }
    public void setMedicineBrand(String medicineBrand) { this.medicineBrand = medicineBrand; }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public boolean isSubmittedByAdmin() { return submittedByAdmin; }
    public void setSubmittedByAdmin(boolean submittedByAdmin) { this.submittedByAdmin = submittedByAdmin; }

    public Staff getSubmittedByStaff() { return submittedByStaff; }
    public void setSubmittedByStaff(Staff submittedByStaff) { this.submittedByStaff = submittedByStaff; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum CustomerType {
        PATIENT, DOCTOR, CUSTOMER, NURSE
    }
}
