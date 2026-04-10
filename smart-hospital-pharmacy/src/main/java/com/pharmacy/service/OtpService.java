package com.pharmacy.service;

import com.pharmacy.entity.OtpToken;
import com.pharmacy.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;

    @Transactional
    public String generateAndSaveOtp(String email) {
        // Invalidate old OTPs
        otpTokenRepository.invalidateAllByEmail(email);

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpToken token = new OtpToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);
        otpTokenRepository.save(token);

        return otp;
    }

    @Transactional
    public boolean validateOtp(String email, String otp) {
        String normalizedEmail = email == null ? null : email.trim();
        String normalizedOtp = otp == null ? null : otp.trim();
        Optional<OtpToken> tokenOpt = otpTokenRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(normalizedEmail);

        if (tokenOpt.isEmpty()) {
            log.warn("OTP validation failed: no active token found for email={}", normalizedEmail);
            return false;
        }

        OtpToken token = tokenOpt.get();
        if (token.isExpired()) {
            log.warn("OTP validation failed: token expired for email={}, submittedOtp={}, storedOtp={}, expiryTime={}, now={}",
                    normalizedEmail, normalizedOtp, token.getOtp(), token.getExpiryTime(), LocalDateTime.now());
            return false;
        }
        if (!token.getOtp().equals(normalizedOtp)) {
            log.warn("OTP validation failed: OTP mismatch for email={}, submittedOtp={}, storedOtp={}",
                    normalizedEmail, normalizedOtp, token.getOtp());
            return false;
        }

        token.setUsed(true);
        otpTokenRepository.save(token);
        log.info("OTP validated successfully for email={}", normalizedEmail);
        return true;
    }
}
