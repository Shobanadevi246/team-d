package com.pharmacy.repository;

import com.pharmacy.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByMedicineId(Long medicineId);
    List<StockTransaction> findByPerformedByStaffId(Long staffId);
    List<StockTransaction> findByPerformedByAdmin(boolean performedByAdmin);

    @Query("SELECT t FROM StockTransaction t WHERE t.medicine.id = :medicineId ORDER BY t.transactionDate DESC")
    List<StockTransaction> findByMedicineIdOrderByDate(@Param("medicineId") Long medicineId);

    @Query("SELECT t.medicine.id, t.medicine.productName, SUM(t.quantity) as totalOut FROM StockTransaction t " +
           "WHERE t.transactionType = 'STOCK_OUT' GROUP BY t.medicine.id, t.medicine.productName ORDER BY totalOut DESC")
    List<Object[]> findFastSellingMedicines();

    @Query("SELECT t.medicine.id, t.medicine.productName, SUM(t.quantity) as totalOut FROM StockTransaction t " +
           "WHERE t.transactionType = 'STOCK_OUT' GROUP BY t.medicine.id, t.medicine.productName ORDER BY totalOut ASC")
    List<Object[]> findSlowSellingMedicines();
}
