package com.qms.util;

import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {

    // Formats a sequential number into a padded token string using the service's prefix.
    // e.g., prefix="C", count=42 → "C-042"
    public String generate(String prefix, long sequenceNumber) {
        return prefix + "-" + String.format("%03d", sequenceNumber);
    }
}
