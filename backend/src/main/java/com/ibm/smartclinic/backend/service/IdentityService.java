package com.ibm.smartclinic.backend.service;

import com.ibm.smartclinic.backend.exception.ConflictException;
import com.ibm.smartclinic.backend.exception.ValidationException;
import com.ibm.smartclinic.backend.model.UserIdentity;
import com.ibm.smartclinic.backend.model.UserRole;
import com.ibm.smartclinic.backend.repository.UserIdentityRepository;
import java.util.Locale;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class IdentityService {

    private final UserIdentityRepository userIdentityRepository;
    private final PasswordEncoder passwordEncoder;

    public IdentityService(@NonNull UserIdentityRepository userIdentityRepository,
                           @NonNull PasswordEncoder passwordEncoder) {
        this.userIdentityRepository = userIdentityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String normalizeEmail(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    public UserIdentity registerIdentity(@NonNull String email, @NonNull String rawPassword, @NonNull UserRole role) {
        String normalized = validateEmail(email);
        validatePassword(rawPassword);
        Optional<UserIdentity> existing = userIdentityRepository.findByEmail(normalized);
        if (existing.isPresent()) {
                UserRole existingRole = existing.get().getRole();
            String code = "EMAIL_EXISTS";
            String message = existingRole == role
                    ? "Email is already registered for this role"
                    : "Email is already registered as " + existingRole.name();
            throw new ConflictException(message, code);
        }

        UserIdentity identity = new UserIdentity();
        identity.setEmail(normalized);
        identity.setRole(role);
        identity.setPasswordHash(passwordEncoder.encode(rawPassword));
        identity.setActive(true);
        return userIdentityRepository.save(identity);
    }

    public UserIdentity provisionIdentity(@NonNull String email,
                                          @NonNull UserRole role,
                                          @NonNull String rawPassword) {
        String normalized = validateEmail(email);
        validatePassword(rawPassword);
        return userIdentityRepository.findByEmail(normalized)
                .map(existing -> {
                    enforceRole(existing, role);
                    if (needsPasswordRefresh(existing.getPasswordHash(), rawPassword)) {
                        existing.setPasswordHash(passwordEncoder.encode(rawPassword));
                    }
                    if (!existing.isActive()) {
                        existing.setActive(true);
                    }
                    return existing;
                })
                .orElseGet(() -> registerIdentity(normalized, rawPassword, role));
    }

    public UserIdentity requireIdentity(@NonNull String email) {
        String normalized = validateEmail(email);
        return userIdentityRepository.findByEmail(normalized)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    public UserIdentity authenticate(@NonNull String email,
                                     @NonNull String rawPassword,
                                     @NonNull UserRole expectedRole) {
        String normalized = validateEmail(email);
        validatePassword(rawPassword);
        UserIdentity identity = userIdentityRepository.findByEmail(normalized)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        enforceRole(identity, expectedRole);
        if (!identity.isActive()) {
            throw new AccessDeniedException("Account is inactive");
        }
        if (!matchesPassword(rawPassword, identity.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        upgradePasswordIfNeeded(identity, rawPassword);
        return identity;
    }

    public void assertRole(@NonNull String email, @NonNull UserRole expectedRole) {
        enforceRole(requireIdentity(email), expectedRole);
    }

    private void enforceRole(UserIdentity identity, UserRole expectedRole) {
        if (identity.getRole() != expectedRole) {
            throw new AccessDeniedException("Email is registered as " + identity.getRole());
        }
    }

    private void upgradePasswordIfNeeded(UserIdentity identity, String rawPassword) {
        if (needsPasswordRefresh(identity.getPasswordHash(), rawPassword)) {
            identity.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
    }

    private boolean needsPasswordRefresh(String storedPassword, String rawPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return true;
        }
        return !isBcrypt(storedPassword) && storedPassword.equals(rawPassword);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (passwordEncoder.matches(rawPassword, storedPassword)) {
            return true;
        }
        return !isBcrypt(storedPassword) && storedPassword.equals(rawPassword);
    }

    private boolean isBcrypt(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    private String validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ValidationException("Email is required");
        }
        return normalizeEmail(email);
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new ValidationException("Password is required");
        }
    }
}
