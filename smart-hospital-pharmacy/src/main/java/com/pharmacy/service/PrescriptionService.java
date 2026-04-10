package com.pharmacy.service;

import com.pharmacy.entity.Prescription;
import com.pharmacy.entity.Staff;
import com.pharmacy.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicineService medicineService;

    public List<Prescription> getAll() {
        return prescriptionRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<Prescription> getById(Long id) {
        return id == null ? Optional.empty() : prescriptionRepository.findById(Objects.requireNonNull(id));
    }

    public List<Prescription> searchByPatient(String name) {
        return prescriptionRepository.findByPatientNameContainingIgnoreCase(name);
    }

    public List<Prescription> getByStaff(Long staffId) {
        return staffId == null ? List.of() : prescriptionRepository.findByDispensedByStaffIdOrderByCreatedAtDesc(Objects.requireNonNull(staffId));
    }

    public List<Object[]> getMostPrescribed() {
        return prescriptionRepository.findMostPrescribedMedicines();
    }

    @Transactional
    public Prescription createPrescription(Prescription prescription, boolean isAdmin, Staff staff) {
        prescription.setPrescriptionNumber("RX-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        prescription.setDispensedByAdmin(isAdmin);
        if (!isAdmin) prescription.setDispensedByStaff(staff);

        // Deduct from stock
        if (prescription.getMedicine() != null) {
            medicineService.stockOut(
                prescription.getMedicine().getId(),
                prescription.getQuantityDispensed(),
                isAdmin, staff,
                "Prescribed to: " + prescription.getPatientName()
            );
            prescription.setMedicineName(prescription.getMedicine().getProductName());
        }
        return prescriptionRepository.save(prescription);
    }

    public long getTotalCount() {
        return prescriptionRepository.count();
    }

    public long getCountByStatus(Prescription.PrescriptionStatus status) {
        return prescriptionRepository.countByStatus(status);
    }
}
