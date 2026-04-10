package com.pharmacy.repository;

import com.pharmacy.entity.StorageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StorageLogRepository extends JpaRepository<StorageLog, Long> {
    List<StorageLog> findTop50ByOrderByLoggedAtDesc();
    List<StorageLog> findByMedicineIdOrderByLoggedAtDesc(Long medicineId);
    List<StorageLog> findByStatus(StorageLog.StorageStatus status);
    List<StorageLog> findByStorageZone(String zone);
}
