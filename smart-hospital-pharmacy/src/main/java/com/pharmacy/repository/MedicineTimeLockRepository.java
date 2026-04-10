package com.pharmacy.repository;

import com.pharmacy.entity.MedicineTimeLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineTimeLockRepository extends JpaRepository<MedicineTimeLock, Long> {
    List<MedicineTimeLock> findByActiveTrue();
    Optional<MedicineTimeLock> findByMedicineIdAndActiveTrue(Long medicineId);
}
