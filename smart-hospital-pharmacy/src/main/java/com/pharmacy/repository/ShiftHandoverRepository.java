package com.pharmacy.repository;

import com.pharmacy.entity.ShiftHandover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShiftHandoverRepository extends JpaRepository<ShiftHandover, Long> {
    List<ShiftHandover> findAllByOrderByCreatedAtDesc();
    List<ShiftHandover> findByOutgoingStaffIdOrderByCreatedAtDesc(Long staffId);
    List<ShiftHandover> findByIncomingStaffIdAndStatus(Long staffId, ShiftHandover.HandoverStatus status);
}
