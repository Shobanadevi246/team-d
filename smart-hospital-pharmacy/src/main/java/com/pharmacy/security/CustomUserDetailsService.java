package com.pharmacy.security;

import com.pharmacy.entity.Staff;
import com.pharmacy.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Check admin credentials
        if (username.equals(adminUsername) || username.equals(adminEmail)) {
            return User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .build();
        }

        // Check staff by email
        Optional<Staff> staffOpt = staffRepository.findByEmail(username);
        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();
            if (staff.isLocked()) {
                throw new UsernameNotFoundException("Account is locked. Contact administrator.");
            }
            return User.builder()
                    .username(staff.getEmail())
                    .password(staff.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_STAFF")))
                    .disabled(!staff.isEnabled())
                    .accountLocked(staff.isLocked())
                    .build();
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }

    public boolean isAdmin(String username) {
        return username.equals(adminUsername) || username.equals(adminEmail);
    }
}
