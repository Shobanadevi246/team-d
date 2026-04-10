package com.pharmacy.controller;

import com.pharmacy.entity.Staff;
import com.pharmacy.service.EmailService;
import com.pharmacy.service.OtpService;
import com.pharmacy.service.StaffService;
import com.pharmacy.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StaffService staffService;
    private final OtpService otpService;
    private final EmailService emailService;
    private static final String RESET_EMAIL_SESSION_KEY = "resetEmail";
    private static final String OTP_VERIFIED_SESSION_KEY = "otpVerified";

    @Value("${admin.username}")
    private String adminUsername;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "locked", required = false) String locked,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid credentials. Please try again.");
        if (logout != null) model.addAttribute("message", "You have been logged out successfully.");
        if (locked != null) model.addAttribute("error", "Your account is locked. Contact administrator.");
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam(defaultValue = "staff") String loginType,
                               HttpServletResponse response,
                               RedirectAttributes ra) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(auth);

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            String token = jwtUtil.generateToken(username, isAdmin ? "ADMIN" : "STAFF");
            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(86400);
            cookie.setPath("/");
            response.addCookie(cookie);

            if (!isAdmin) {
                staffService.resetFailedAttempts(username);
            }

            return isAdmin ? "redirect:/admin/dashboard" : "redirect:/staff/dashboard";

        } catch (BadCredentialsException e) {
            // Record failed login for staff
            Optional<Staff> staffOpt = staffService.getStaffByEmail(username);
            if (staffOpt.isPresent()) {
                staffService.recordFailedLogin(username);
                Staff staff = staffOpt.get();
                if (staff.isLocked() || staff.getFailedLoginAttempts() >= 3) {
                    return "redirect:/login?locked=true";
                }
            }
            return "redirect:/login?error=true";
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        if (auth == null) return "redirect:/login";
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin/dashboard" : "redirect:/staff/dashboard";
    }

    // Forgot password flow
    @GetMapping("/auth/forgot-password")
    public String forgotPasswordPage(HttpSession session) {
        session.removeAttribute(RESET_EMAIL_SESSION_KEY);
        session.removeAttribute(OTP_VERIFIED_SESSION_KEY);
        return "auth/forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String sendOtp(@RequestParam String email, Model model, RedirectAttributes ra, HttpSession session) {
        if (!staffService.existsByEmail(email)) {
            model.addAttribute("error", "No account found with this email address.");
            return "auth/forgot-password";
        }
        try {
            String otp = otpService.generateAndSaveOtp(email);
            Staff staff = staffService.getStaffByEmail(email).orElseThrow();
            emailService.sendOtpEmail(email, otp, staff.getStaffName());
            session.setAttribute(RESET_EMAIL_SESSION_KEY, email);
            session.removeAttribute(OTP_VERIFIED_SESSION_KEY);
            ra.addFlashAttribute("email", email);
            ra.addFlashAttribute("success", "OTP sent to your email. Valid for 10 minutes.");
            return "redirect:/auth/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to send OTP: " + e.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/auth/verify-otp")
    public String verifyOtpPage(Model model, HttpSession session) {
        Object email = session.getAttribute(RESET_EMAIL_SESSION_KEY);
        if (email == null) {
            return "redirect:/auth/forgot-password";
        }
        model.addAttribute("email", email.toString());
        return "auth/verify-otp";
    }

    @PostMapping("/auth/verify-otp")
    public String verifyOtp(@RequestParam(required = false) String email,
                            @RequestParam String otp,
                            Model model,
                            RedirectAttributes ra,
                            HttpSession session) {
        String sessionEmail = (String) session.getAttribute(RESET_EMAIL_SESSION_KEY);
        String effectiveEmail = (email != null && !email.isBlank()) ? email : sessionEmail;

        if (effectiveEmail == null || effectiveEmail.isBlank()) {
            model.addAttribute("error", "Your OTP session expired. Please request a new OTP.");
            return "auth/forgot-password";
        }

        if (otpService.validateOtp(effectiveEmail, otp)) {
            session.setAttribute(RESET_EMAIL_SESSION_KEY, effectiveEmail);
            session.setAttribute(OTP_VERIFIED_SESSION_KEY, true);
            ra.addFlashAttribute("email", effectiveEmail);
            return "redirect:/auth/reset-password";
        } else {
            model.addAttribute("error", "Invalid or expired OTP. Please try again.");
            model.addAttribute("email", effectiveEmail);
            return "auth/verify-otp";
        }
    }

    @GetMapping("/auth/reset-password")
    public String resetPasswordPage(Model model, HttpSession session) {
        Boolean otpVerified = (Boolean) session.getAttribute(OTP_VERIFIED_SESSION_KEY);
        String email = (String) session.getAttribute(RESET_EMAIL_SESSION_KEY);
        if (!Boolean.TRUE.equals(otpVerified) || email == null || email.isBlank()) {
            return "redirect:/auth/forgot-password";
        }
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String resetPassword(@RequestParam(required = false) String email,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Model model,
                                RedirectAttributes ra,
                                HttpSession session) {
        String sessionEmail = (String) session.getAttribute(RESET_EMAIL_SESSION_KEY);
        String effectiveEmail = (email != null && !email.isBlank()) ? email : sessionEmail;
        if (effectiveEmail == null || effectiveEmail.isBlank()) {
            return "redirect:/auth/forgot-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("email", effectiveEmail);
            return "auth/reset-password";
        }
        if (!staffService.isPasswordStrong(newPassword)) {
            model.addAttribute("error", "Password must contain at least 1 uppercase, 1 number and 1 special character.");
            model.addAttribute("email", effectiveEmail);
            return "auth/reset-password";
        }
        staffService.updatePassword(effectiveEmail, newPassword);
        session.removeAttribute(RESET_EMAIL_SESSION_KEY);
        session.removeAttribute(OTP_VERIFIED_SESSION_KEY);
        ra.addFlashAttribute("success", "Password reset successfully. You can now login.");
        return "redirect:/login";
    }
}
