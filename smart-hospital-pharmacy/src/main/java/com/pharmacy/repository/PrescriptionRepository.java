package com.pharmacy.repository;

import com.pharmacy.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findAllByOrderByCreatedAtDesc();
    List<Prescription> findByPatientNameContainingIgnoreCase(String name);
    List<Prescription> findByDispensedByStaffIdOrderByCreatedAtDesc(Long staffId);
    List<Prescription> findByMedicineIdOrderByCreatedAtDesc(Long medicineId);

    @Query("SELECT p.medicineName, COUNT(p) as cnt FROM Prescription p GROUP BY p.medicineName ORDER BY cnt DESC")
    List<Object[]> findMostPrescribedMedicines();

    long countByStatus(Prescription.PrescriptionStatus status);
}
