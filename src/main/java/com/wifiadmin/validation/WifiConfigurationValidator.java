package com.wifiadmin.validation;

import com.wifiadmin.dto.WifiConfigurationDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class WifiConfigurationValidator {

    private final Validator validator;

    public WifiConfigurationValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(WifiConfigurationDto dto) {
        Set<ConstraintViolation<WifiConfigurationDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation failed");
            throw new IllegalArgumentException(msg);
        }

        validateBusinessRules(dto);
    }

    private void validateBusinessRules(WifiConfigurationDto dto) {
        String encryption = dto.getEncryptionType();
        String password = dto.getPassword();

        if (encryption == null || encryption.isEmpty()) {
            dto.setEncryptionType("OPEN");
            encryption = "OPEN";
        }

        // Validate encryption type allowed values
        Set<String> allowedEncryptions = Set.of(
                "OPEN", "WEP", "WPA_PSK", "WPA2_PSK", "WPA3_SAE", "WPA2_ENTERPRISE"
        );
        if (!allowedEncryptions.contains(encryption)) {
            throw new IllegalArgumentException("Invalid encryptionType: " + encryption);
        }

        // Validate wifiBand allowed values
        Set<String> allowedBands = Set.of("BAND_2_4_GHZ", "BAND_5_GHZ");
        if (!allowedBands.contains(dto.getWifiBand())) {
            throw new IllegalArgumentException("Invalid wifiBand: " + dto.getWifiBand());
        }

        // Password required for non-OPEN encryption
        if (!"OPEN".equals(encryption) && (password == null || password.isBlank())) {
            throw new IllegalArgumentException(
                    "Password is required when encryptionType is " + encryption);
        }

        // Password should be absent/empty for OPEN networks
        if ("OPEN".equals(encryption) && password != null && !password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password should not be set for OPEN networks");
        }

        // Password length validation
        if (password != null && !password.isBlank()) {
            if (password.length() < 8) {
                throw new IllegalArgumentException(
                        "Password must be at least 8 characters long");
            }
            if (password.length() > 64) {
                throw new IllegalArgumentException(
                        "Password must be at most 64 characters long");
            }
        }
    }
}
