package com.pharmacy.service;

import com.pharmacy.entity.CustomerFeedback;
import com.pharmacy.entity.Staff;
import com.pharmacy.repository.CustomerFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final CustomerFeedbackRepository feedbackRepository;

    public List<CustomerFeedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }

    public Optional<CustomerFeedback> getFeedbackById(Long id) {
        return id == null ? Optional.empty() : feedbackRepository.findById(Objects.requireNonNull(id));
    }

    @Transactional
    public CustomerFeedback saveFeedback(CustomerFeedback feedback, boolean isAdmin, Staff staff) {
        feedback.setSubmittedByAdmin(isAdmin);
        if (!isAdmin) {
            feedback.setSubmittedByStaff(staff);
        }
        return feedbackRepository.save(feedback);
    }

    public List<CustomerFeedback> getFeedbackByMedicine(Long medicineId) {
        return medicineId == null ? List.of() : feedbackRepository.findByMedicineId(Objects.requireNonNull(medicineId));
    }

    public List<Object[]> getMedicineRatingStats() {
        return feedbackRepository.findMedicineRatingStats();
    }

    public List<CustomerFeedback> getFeedbackByStaff(Long staffId) {
        return staffId == null ? List.of() : feedbackRepository.findBySubmittedByStaffId(Objects.requireNonNull(staffId));
    }
}
