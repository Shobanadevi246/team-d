package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer quantityOrdered;

    private String supplierName;

    private String supplierContact;

    private Double estimatedCost;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private String notes;

    private boolean raisedByAdmin = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by_staff_id")
    private Staff raisedByStaff;

    private LocalDate expectedDeliveryDate;

    private LocalDate deliveredDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public PurchaseOrder() {}

    // Constructor with all fields
    public PurchaseOrder(Long id, String orderNumber, Medicine medicine, Integer quantityOrdered, String supplierName, String supplierContact, Double estimatedCost, OrderStatus status, String notes, boolean raisedByAdmin, Staff raisedByStaff, LocalDate expectedDeliveryDate, LocalDate deliveredDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.medicine = medicine;
        this.quantityOrdered = quantityOrdered;
        this.supplierName = supplierName;
        this.supplierContact = supplierContact;
        this.estimatedCost = estimatedCost;
        this.status = status;
        this.notes = notes;
        this.raisedByAdmin = raisedByAdmin;
        this.raisedByStaff = raisedByStaff;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.deliveredDate = deliveredDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public Integer getQuantityOrdered() { return quantityOrdered; }
    public void setQuantityOrdered(Integer quantityOrdered) { this.quantityOrdered = quantityOrdered; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSupplierContact() { return supplierContact; }
    public void setSupplierContact(String supplierContact) { this.supplierContact = supplierContact; }

    public Double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(Double estimatedCost) { this.estimatedCost = estimatedCost; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isRaisedByAdmin() { return raisedByAdmin; }
    public void setRaisedByAdmin(boolean raisedByAdmin) { this.raisedByAdmin = raisedByAdmin; }

    public Staff getRaisedByStaff() { return raisedByStaff; }
    public void setRaisedByStaff(Staff raisedByStaff) { this.raisedByStaff = raisedByStaff; }

    public LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public LocalDate getDeliveredDate() { return deliveredDate; }
    public void setDeliveredDate(LocalDate deliveredDate) { this.deliveredDate = deliveredDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum OrderStatus {
        PENDING, APPROVED, ORDERED, DELIVERED, CANCELLED
    }
}
