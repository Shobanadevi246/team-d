package com.pharmacy.service;

import com.pharmacy.entity.Staff;
import com.pharmacy.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int MAX_FAILED_ATTEMPTS = 3;

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Optional<Staff> getStaffById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return staffRepository.findById(id);
    }

    public Optional<Staff> getStaffByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    @Transactional
    public Staff createStaff(Staff staff) {
        if (staffRepository.existsByEmail(staff.getEmail())) {
            throw new RuntimeException("Email already exists: " + staff.getEmail());
        }
        if (staffRepository.existsByStaffId(staff.getStaffId())) {
            throw new RuntimeException("Staff ID already exists: " + staff.getStaffId());
        }
        String rawPassword = staff.getPassword();
        staff.setPassword(passwordEncoder.encode(rawPassword));
        staff.setEnabled(true);
        staff.setLocked(false);
        staff.setFailedLoginAttempts(0);
        Staff saved = staffRepository.save(staff);
        try {
            emailService.sendWelcomeEmail(staff.getEmail(), staff.getStaffName(), rawPassword);
        } catch (Exception e) {
            log.warn("Could not send welcome email: {}", e.getMessage());
        }
        return saved;
    }

    @Transactional
    public void recordFailedLogin(String email) {
        staffRepository.findByEmail(email).ifPresent(staff -> {
            int attempts = staff.getFailedLoginAttempts() + 1;
            staff.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                staff.setLocked(true);
                staff.setLockedAt(LocalDateTime.now());
                try {
                    emailService.sendAccountLockedEmail(staff.getEmail(), staff.getStaffName());
                } catch (Exception e) {
                    log.warn("Could not send lock notification: {}", e.getMessage());
                }
            }
            staffRepository.save(staff);
        });
    }

    @Transactional
    public void resetFailedAttempts(String email) {
        staffRepository.findByEmail(email).ifPresent(staff -> {
            staff.setFailedLoginAttempts(0);
            staffRepository.save(staff);
        });
    }

    @Transactional
    public void unlockStaff(Long staffId) {
        if (staffId == null) {
            return;
        }
        staffRepository.findById(Objects.requireNonNull(staffId)).ifPresent(staff -> {
            staff.setLocked(false);
            staff.setFailedLoginAttempts(0);
            staff.setLockedAt(null);
            staffRepository.save(staff);
        });
    }

    @Transactional
    public void lockStaff(Long staffId) {
        if (staffId == null) {
            return;
        }
        staffRepository.findById(Objects.requireNonNull(staffId)).ifPresent(staff -> {
            staff.setLocked(true);
            staff.setLockedAt(LocalDateTime.now());
            staffRepository.save(staff);
        });
    }

    public List<Staff> getLockedStaff() {
        return staffRepository.findLockedStaff();
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        staffRepository.findByEmail(email).ifPresent(staff -> {
            staff.setPassword(passwordEncoder.encode(newPassword));
            staff.setFailedLoginAttempts(0);
            staff.setLocked(false);
            staffRepository.save(staff);
        });
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public boolean isPasswordStrong(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");
    }

    public boolean existsByEmail(String email) {
        return staffRepository.existsByEmail(email);
    }
}
