package com.pharmacy.service;

import com.pharmacy.entity.Medicine;
import com.pharmacy.entity.Staff;
import com.pharmacy.entity.StockTransaction;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockTransactionRepository;
import com.pharmacy.util.SkuGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final SkuGenerator skuGenerator;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return id == null ? Optional.empty() : medicineRepository.findById(Objects.requireNonNull(id));
    }

    public List<Medicine> getLowStockMedicines() {
        return medicineRepository.findLowStockMedicines();
    }

    public List<Medicine> getExpiringMedicines() {
        LocalDate threshold = LocalDate.now().plusDays(10);
        return medicineRepository.findExpiringMedicines(threshold, LocalDate.now());
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findExpiredMedicines(LocalDate.now());
    }

    public int getLowStockCount() {
        return medicineRepository.findLowStockMedicines().size();
    }

    public int getExpiringCount() {
        return getExpiringMedicines().size();
    }

    @Transactional
    public Medicine addMedicine(Medicine medicine, MultipartFile imageFile, Staff staff, boolean isAdmin) throws IOException {
        // Auto-generate SKU if not provided
        if (medicine.getSkuId() == null || medicine.getSkuId().isBlank()) {
            medicine.setSkuId(skuGenerator.generateSku(medicine.getProductName()));
        }

        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String imagePath = saveImage(imageFile);
            medicine.setImagePath(imagePath);
        }

        medicine.setAdminAdded(isAdmin);
        if (!isAdmin) {
            medicine.setAddedByStaff(staff);
        }

        Medicine saved = medicineRepository.save(medicine);

        // Record stock-in transaction
        if (medicine.getStockQuantity() > 0) {
            StockTransaction tx = new StockTransaction();
            tx.setMedicine(saved);
            tx.setTransactionType(StockTransaction.TransactionType.STOCK_IN);
            tx.setQuantity(medicine.getStockQuantity());
            tx.setExpiryDateUpdated(medicine.getExpiryDate());
            tx.setPerformedByAdmin(isAdmin);
            tx.setPerformedByStaff(isAdmin ? null : staff);
            tx.setNotes("Initial stock");
            stockTransactionRepository.save(tx);
        }

        return saved;
    }

    @Transactional
    public void stockIn(Long medicineId, int quantity, LocalDate expiryDate,
                        boolean isAdmin, Staff staff, String notes) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        medicine.setStockQuantity(medicine.getStockQuantity() + quantity);
        if (expiryDate != null) {
            medicine.setExpiryDate(expiryDate);
        }
        medicineRepository.save(medicine);

        StockTransaction tx = new StockTransaction();
        tx.setMedicine(medicine);
        tx.setTransactionType(StockTransaction.TransactionType.STOCK_IN);
        tx.setQuantity(quantity);
        tx.setExpiryDateUpdated(expiryDate);
        tx.setPerformedByAdmin(isAdmin);
        tx.setPerformedByStaff(isAdmin ? null : staff);
        tx.setNotes(notes);
        stockTransactionRepository.save(tx);
    }

    @Transactional
    public void stockOut(Long medicineId, int quantity, boolean isAdmin, Staff staff, String notes) {
        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        if (medicine.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + medicine.getStockQuantity());
        }

        medicine.setStockQuantity(medicine.getStockQuantity() - quantity);
        medicineRepository.save(medicine);

        StockTransaction tx = new StockTransaction();
        tx.setMedicine(medicine);
        tx.setTransactionType(StockTransaction.TransactionType.STOCK_OUT);
        tx.setQuantity(quantity);
        tx.setPerformedByAdmin(isAdmin);
        tx.setPerformedByStaff(isAdmin ? null : staff);
        tx.setNotes(notes);
        stockTransactionRepository.save(tx);
    }

    public List<StockTransaction> getTransactionsByMedicine(Long medicineId) {
        return medicineId == null ? List.of() : stockTransactionRepository.findByMedicineIdOrderByDate(Objects.requireNonNull(medicineId));
    }

    public List<StockTransaction> getAllTransactions() {
        return stockTransactionRepository.findAll();
    }

    public List<StockTransaction> getTransactionsByStaff(Long staffId) {
        return staffId == null ? List.of() : stockTransactionRepository.findByPerformedByStaffId(Objects.requireNonNull(staffId));
    }

    public List<StockTransaction> getAdminTransactions() {
        return stockTransactionRepository.findByPerformedByAdmin(true);
    }

    public List<Object[]> getFastSellingMedicines() {
        return stockTransactionRepository.findFastSellingMedicines();
    }

    public List<Object[]> getSlowSellingMedicines() {
        return stockTransactionRepository.findSlowSellingMedicines();
    }

    public List<Medicine> searchMedicines(String query) {
        return medicineRepository.findByProductNameContainingIgnoreCase(query);
    }

    private String saveImage(MultipartFile file) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + ext;
        Path filePath = Paths.get(uploadDir, fileName);
        Files.write(filePath, file.getBytes());
        return fileName;
    }
}
