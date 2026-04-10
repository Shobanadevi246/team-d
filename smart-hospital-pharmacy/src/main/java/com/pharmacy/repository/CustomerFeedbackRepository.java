package com.pharmacy.repository;

import com.pharmacy.entity.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    List<CustomerFeedback> findByMedicineId(Long medicineId);

    @Query("SELECT f.medicineName, AVG(f.rating) as avgRating, COUNT(f) as count FROM CustomerFeedback f GROUP BY f.medicineName ORDER BY avgRating DESC")
    List<Object[]> findMedicineRatingStats();

    List<CustomerFeedback> findBySubmittedByStaffId(Long staffId);
    List<CustomerFeedback> findByCustomerType(CustomerFeedback.CustomerType type);
}
