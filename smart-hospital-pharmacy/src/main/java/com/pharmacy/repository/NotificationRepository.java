package com.pharmacy.repository;

import com.pharmacy.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReadByAdminFalseOrderByCreatedAtDesc();
    List<Notification> findTop20ByOrderByCreatedAtDesc();
    long countByReadByAdminFalse();
    List<Notification> findByTargetStaffIdAndReadByStaffFalse(Long staffId);
    long countByTargetStaffIdAndReadByStaffFalse(Long staffId);
}
