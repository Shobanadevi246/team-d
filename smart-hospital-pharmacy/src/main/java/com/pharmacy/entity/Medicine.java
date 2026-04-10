package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, unique = true)
    private String skuId;

    private String imagePath;

    @Column(nullable = false)
    private Integer stockQuantity = 0;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean adminAdded = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff addedByStaff;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Medicine() {}

    // Constructor with all fields
    public Medicine(Long id, String productName, String skuId, String imagePath, Integer stockQuantity, LocalDate expiryDate, boolean adminAdded, Staff addedByStaff, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productName = productName;
        this.skuId = skuId;
        this.imagePath = imagePath;
        this.stockQuantity = stockQuantity;
        this.expiryDate = expiryDate;
        this.adminAdded = adminAdded;
        this.addedByStaff = addedByStaff;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSkuId() { return skuId; }
    public void setSkuId(String skuId) { this.skuId = skuId; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public boolean isAdminAdded() { return adminAdded; }
    public void setAdminAdded(boolean adminAdded) { this.adminAdded = adminAdded; }

    public Staff getAddedByStaff() { return addedByStaff; }
    public void setAddedByStaff(Staff addedByStaff) { this.addedByStaff = addedByStaff; }

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

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockTransaction> stockTransactions;

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerFeedback> feedbacks;
}
