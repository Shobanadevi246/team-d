package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_transactions")
public class StockTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer quantity;

    private LocalDate expiryDateUpdated;

    @Column(nullable = false)
    private boolean performedByAdmin = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff performedByStaff;

    private String notes;

    private LocalDateTime transactionDate;

    // Default constructor
    public StockTransaction() {}

    // Constructor with all fields
    public StockTransaction(Long id, Medicine medicine, TransactionType transactionType, Integer quantity, LocalDate expiryDateUpdated, boolean performedByAdmin, Staff performedByStaff, String notes, LocalDateTime transactionDate) {
        this.id = id;
        this.medicine = medicine;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.expiryDateUpdated = expiryDateUpdated;
        this.performedByAdmin = performedByAdmin;
        this.performedByStaff = performedByStaff;
        this.notes = notes;
        this.transactionDate = transactionDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public LocalDate getExpiryDateUpdated() { return expiryDateUpdated; }
    public void setExpiryDateUpdated(LocalDate expiryDateUpdated) { this.expiryDateUpdated = expiryDateUpdated; }

    public boolean isPerformedByAdmin() { return performedByAdmin; }
    public void setPerformedByAdmin(boolean performedByAdmin) { this.performedByAdmin = performedByAdmin; }

    public Staff getPerformedByStaff() { return performedByStaff; }
    public void setPerformedByStaff(Staff performedByStaff) { this.performedByStaff = performedByStaff; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();
    }

    public enum TransactionType {
        STOCK_IN, STOCK_OUT
    }
}
