package com.pharmacy.repository;

import com.pharmacy.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    Optional<Medicine> findBySkuId(String skuId);
    boolean existsBySkuId(String skuId);

    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity < 10")
    List<Medicine> findLowStockMedicines();

    @Query("SELECT m FROM Medicine m WHERE m.expiryDate <= :expiryThreshold AND m.expiryDate >= :today")
    List<Medicine> findExpiringMedicines(@Param("expiryThreshold") LocalDate expiryThreshold,
                                          @Param("today") LocalDate today);

    @Query("SELECT m FROM Medicine m WHERE m.expiryDate < :today")
    List<Medicine> findExpiredMedicines(@Param("today") LocalDate today);

    List<Medicine> findByProductNameContainingIgnoreCase(String name);
}
