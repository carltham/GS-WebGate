package com.gswebgate.searcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Multi-layer security tests for SecurityValidator.
 * Verifies that searches CANNOT execute on internet-accessible servers.
 * 
 * Security Layers:
 * 1. LAYER 1: Localhost-only binding (127.0.0.1, ::1) - Private IPs REJECTED
 * 2. LAYER 2: Environment variable confirmation (SEARCHER_PRIVATE_MODE=true)
 * 3. LAYER 3: HTTP scheme only (not HTTPS which could indicate proxying)
 */
@DisplayName("Multi-Layer Security Validation - Localhost Only")
class SecurityValidatorTest {

    private SecurityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SecurityValidator();
        // Enable private environment check
        ReflectionTestUtils.setField(validator, "requirePrivateEnvironment", true);
        // Enable private mode
        ReflectionTestUtils.setField(validator, "privateModeEnabled", true);
    }

    // ============ LAYER 1 TESTS: LOCALHOST-ONLY BINDING ============
    
    /**
     * LAYER 1: Localhost (127.0.0.1) should be allowed.
     */
    @Test
    @DisplayName("LAYER 1: Localhost 127.0.0.1 allowed")
    void testLayer1_Localhost127Allowed() {
        validator.validatePrivateEnvironment("http://127.0.0.1:8080");
    }

    /**
     * LAYER 1: IPv6 localhost (::1) should be allowed.
     */
    @Test
    @DisplayName("LAYER 1: IPv6 localhost [::1] allowed")
    void testLayer1_Ipv6LocalhostAllowed() {
        validator.validatePrivateEnvironment("http://[::1]:8080");
    }

    /**
     * LAYER 1: "localhost" hostname should be allowed.
     */
    @Test
    @DisplayName("LAYER 1: 'localhost' hostname allowed")
    void testLayer1_LocalhostNameAllowed() {
        validator.validatePrivateEnvironment("http://localhost:8080");
    }

    /**
     * LAYER 1: Private IP 10.x.x.x REJECTED (can be port-forwarded).
     */
    @Test
    @DisplayName("LAYER 1: Private IP 10.x.x.x REJECTED")
    void testLayer1_PrivateIp10RangeRejected() {
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("http://10.0.0.1:8080"));
        assertTrue(ex.getMessage().contains("LAYER 1 FAILED"));
        assertTrue(ex.getMessage().contains("NOT localhost"));
    }

    /**
     * LAYER 1: Private IP 192.168.x.x REJECTED (can be port-forwarded).
     */
    @Test
    @DisplayName("LAYER 1: Private IP 192.168.x.x REJECTED")
    void testLayer1_PrivateIp192RangeRejected() {
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("http://192.168.1.1:8080"));
        assertTrue(ex.getMessage().contains("LAYER 1 FAILED"));
        assertTrue(ex.getMessage().contains("NOT localhost"));
    }

    /**
     * LAYER 1: Private IP 172.16-31.x.x REJECTED (can be port-forwarded).
     */
    @Test
    @DisplayName("LAYER 1: Private IP 172.16-31.x.x REJECTED")
    void testLayer1_PrivateIp172RangeRejected() {
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("http://172.20.0.1:8080"));
        assertTrue(ex.getMessage().contains("LAYER 1 FAILED"));
    }

    /**
     * LAYER 1: Public IP REJECTED.
     */
    @Test
    @DisplayName("LAYER 1: Public IP 8.8.8.8 REJECTED")
    void testLayer1_PublicIpRejected() {
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("http://8.8.8.8:8080"));
        assertTrue(ex.getMessage().contains("LAYER 1 FAILED"));
    }

    /**
     * LAYER 1: Public domain REJECTED.
     */
    @Test
    @DisplayName("LAYER 1: Public domain example.com REJECTED")
    void testLayer1_PublicDomainRejected() {
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("http://example.com:8080"));
        assertTrue(ex.getMessage().contains("LAYER 1 FAILED"));
    }

    // ============ LAYER 2 TESTS: ENVIRONMENT VARIABLE CONFIRMATION ============

    /**
     * LAYER 2: Environment variable MUST be set to "true".
     */
    @Test
    @DisplayName("LAYER 2: Private mode disabled without environment variable")
    void testLayer2_DisabledWithoutEnvVar() {
        ReflectionTestUtils.setField(validator, "privateModeEnabled", false);
        
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("http://localhost:8080"));
        assertTrue(ex.getMessage().contains("LAYER 2 FAILED"));
        assertTrue(ex.getMessage().contains("Private mode not enabled"));
    }

    /**
     * LAYER 2: When private mode is enabled, validation proceeds.
     */
    @Test
    @DisplayName("LAYER 2: Passes when searcher.private-mode-enabled=true")
    void testLayer2_PassesWhenEnabled() {
        ReflectionTestUtils.setField(validator, "privateModeEnabled", true);
        // Should not throw if layers 1 and 3 also pass
        validator.validatePrivateEnvironment("http://localhost:8080");
    }

    // ============ LAYER 3 TESTS: URL SCHEME VALIDATION ============

    /**
     * LAYER 3: HTTP scheme allowed (no encryption needed for localhost).
     */
    @Test
    @DisplayName("LAYER 3: HTTP scheme allowed")
    void testLayer3_HttpAllowed() {
        validator.validatePrivateEnvironment("http://localhost:8080");
    }

    /**
     * LAYER 3: HTTPS scheme REJECTED (suggests proxying/exposure).
     */
    @Test
    @DisplayName("LAYER 3: HTTPS scheme REJECTED")
    void testLayer3_HttpsRejected() {
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("https://localhost:8080"));
        String message = ex.getMessage();
        assertTrue(message != null && message.contains("LAYER 3"), 
            "Expected 'LAYER 3' in message but got: " + message);
    }

    // ============ COMBINED LAYER TESTS ============

    /**
     * All layers must pass: localhost + http + private mode enabled.
     */
    @Test
    @DisplayName("All layers pass: localhost, HTTP, private mode enabled")
    void testAllLayersPass() {
        ReflectionTestUtils.setField(validator, "privateModeEnabled", true);
        // Should not throw
        validator.validatePrivateEnvironment("http://localhost:8080");
    }

    /**
     * Fails if ANY layer fails (all layers must pass).
     */
    @Test
    @DisplayName("Fails if LAYER 1 fails (host not localhost)")
    void testFailsIfLayer1Fails() {
        ReflectionTestUtils.setField(validator, "privateModeEnabled", true);
        
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("http://10.0.0.1:8080"));
        assertTrue(ex.getMessage().contains("LAYER 1 FAILED"));
    }

    /**
     * Fails if LAYER 3 fails (HTTPS instead of HTTP).
     */
    @Test
    @DisplayName("Fails if LAYER 3 fails (HTTPS instead of HTTP)")
    void testFailsIfLayer3Fails() {
        ReflectionTestUtils.setField(validator, "privateModeEnabled", true);
        
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("https://localhost:8080"));
        assertTrue(ex.getMessage().contains("LAYER 3 FAILED"));
    }

    /**
     * When all checks disabled, all URLs pass.
     */
    @Test
    @DisplayName("When requirePrivateEnvironment=false, all URLs allowed")
    void testDisabledCheckAllowsAll() {
        ReflectionTestUtils.setField(validator, "requirePrivateEnvironment", false);
        
        validator.validatePrivateEnvironment("http://8.8.8.8:8080");
        validator.validatePrivateEnvironment("https://example.com");
        validator.validatePrivateEnvironment("http://aws-server.com");
    }

    /**
     * Invalid URL format throws SecurityException.
     */
    @Test
    @DisplayName("Invalid URL throws SecurityException")
    void testInvalidUrlThrows() {
        SecurityException ex = assertThrows(SecurityException.class,
            () -> validator.validatePrivateEnvironment("not-a-valid-url"));
        assertTrue(ex.getMessage().contains("FAILED"));
    }
}
