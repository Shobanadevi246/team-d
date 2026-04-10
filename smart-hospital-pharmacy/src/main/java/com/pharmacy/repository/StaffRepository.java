package com.pharmacy.repository;

import com.pharmacy.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);
    Optional<Staff> findByStaffId(String staffId);
    boolean existsByEmail(String email);
    boolean existsByStaffId(String staffId);
    List<Staff> findByLocked(boolean locked);

    @Query("SELECT s FROM Staff s WHERE s.locked = true")
    List<Staff> findLockedStaff();
}
