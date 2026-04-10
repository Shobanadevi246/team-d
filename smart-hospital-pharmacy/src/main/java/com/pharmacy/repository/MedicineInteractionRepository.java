package com.pharmacy.repository;

import com.pharmacy.entity.MedicineInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicineInteractionRepository extends JpaRepository<MedicineInteraction, Long> {
    List<MedicineInteraction> findTop20ByOrderByCheckedAtDesc();
    List<MedicineInteraction> findByCheckedBy(String email);
}
