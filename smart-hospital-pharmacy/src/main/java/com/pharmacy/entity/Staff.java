package com.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String staffName;

    @Column(nullable = false, unique = true)
    private String staffId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String shopNumber;

    @Column(nullable = false)
    private String shopBlockName;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean locked = false;

    private int failedLoginAttempts = 0;

    private LocalDateTime lockedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Default constructor
    public Staff() {}

    // Constructor with all fields
    public Staff(Long id, String staffName, String staffId, String email, String shopNumber, String shopBlockName, String password, boolean enabled, boolean locked, int failedLoginAttempts, LocalDateTime lockedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.staffName = staffName;
        this.staffId = staffId;
        this.email = email;
        this.shopNumber = shopNumber;
        this.shopBlockName = shopBlockName;
        this.password = password;
        this.enabled = enabled;
        this.locked = locked;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedAt = lockedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getShopNumber() { return shopNumber; }
    public void setShopNumber(String shopNumber) { this.shopNumber = shopNumber; }

    public String getShopBlockName() { return shopBlockName; }
    public void setShopBlockName(String shopBlockName) { this.shopBlockName = shopBlockName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Medicine> getMedicines() { return medicines; }
    public void setMedicines(List<Medicine> medicines) { this.medicines = medicines; }

    public List<StockTransaction> getStockTransactions() { return stockTransactions; }
    public void setStockTransactions(List<StockTransaction> stockTransactions) { this.stockTransactions = stockTransactions; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "addedByStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Medicine> medicines;

    @OneToMany(mappedBy = "performedByStaff", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockTransaction> stockTransactions;
}
