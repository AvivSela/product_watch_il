package com.avivse.retailfileservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public class ValidUrlValidator implements ConstraintValidator<ValidUrl, String> {

    private static final Set<String> ALLOWED_PROTOCOLS = Set.of("https", "http");
    private static final Set<String> BLOCKED_HOSTS = Set.of(
        "localhost", "127.0.0.1", "0.0.0.0", "::1"
    );

    @Override
    public boolean isValid(String urlString, ConstraintValidatorContext context) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty validation
        }

        try {
            URL url = new URL(urlString.trim());

            // Check protocol
            if (!ALLOWED_PROTOCOLS.contains(url.getProtocol().toLowerCase())) {
                addViolation(context, "Protocol must be HTTP or HTTPS");
                return false;
            }

            // Check for blocked hosts (security)
            String host = url.getHost();
            if (host != null && BLOCKED_HOSTS.contains(host.toLowerCase())) {
                addViolation(context, "Cannot use localhost or loopback addresses");
                return false;
            }

            // Check for private IP ranges (basic check)
            if (host != null && isPrivateIP(host)) {
                addViolation(context, "Cannot use private IP addresses");
                return false;
            }

            return true;
        } catch (MalformedURLException e) {
            addViolation(context, "Invalid URL format");
            return false;
        }
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    private boolean isPrivateIP(String host) {
        return host.startsWith("192.168.") ||
               host.startsWith("10.") ||
               host.startsWith("172.16.") ||
               host.startsWith("172.17.") ||
               host.startsWith("172.18.") ||
               host.startsWith("172.19.") ||
               host.startsWith("172.2") ||
               host.startsWith("172.30.") ||
               host.startsWith("172.31.");
    }
}