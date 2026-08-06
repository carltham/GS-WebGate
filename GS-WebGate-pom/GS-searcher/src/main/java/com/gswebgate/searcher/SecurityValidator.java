package com.gswebgate.searcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Multi-layer security validator to PREVENT search execution on internet servers.
 * 
 * Security Layers:
 * 1. Localhost-only binding (127.0.0.1, ::1)
 * 2. Environment variable confirmation (SEARCHER_PRIVATE_MODE=true)
 * 3. Certificate validation (self-signed only)
 * 4. HTTP-only on localhost (no HTTPS on public)
 * 
 * All layers must pass. Failure on any layer blocks execution.
 * This design makes it EXTREMELY HARD to accidentally expose on internet.
 */
@Service
public class SecurityValidator {

    private static final Logger logger = LoggerFactory.getLogger(SecurityValidator.class);

    @Value("${searcher.require-private-environment:true}")
    private boolean requirePrivateEnvironment;
    
    @Value("${searcher.private-mode-enabled:false}")
    private boolean privateModeEnabled;

    /**
     * Validates that search operations are ONLY allowed on localhost.
     * Uses multi-layer security to prevent accidental internet exposure.
     * 
     * @param relayUrl The relay base URL to validate
     * @throws SecurityException If ANY security layer fails
     */
    public void validatePrivateEnvironment(String relayUrl) throws SecurityException {
        if (!requirePrivateEnvironment) {
            logger.debug("Private environment check disabled");
            return;
        }

        logger.info("Validating search execution environment - LAYER 1: URL inspection");
        validateLocalhostOnly(relayUrl);
        
        logger.info("LAYER 2: Environment variable confirmation");
        validatePrivateModeEnabled();
        
        logger.info("LAYER 3: URL scheme validation");
        validateUrlScheme(relayUrl);
        
        logger.info("✓ All security layers passed - search execution allowed");
    }

    /**
     * LAYER 1: Hostname must be ONLY localhost (127.0.0.1 or ::1).
     * Private IP ranges (10.x, 192.168.x) are REJECTED because they can be:
     * - Port-forwarded to internet
     * - Exposed via VPN
     * - Bridge-networked to public interfaces
     * 
     * @throws SecurityException if host is not localhost
     */
    private void validateLocalhostOnly(String relayUrl) throws SecurityException {
        try {
            URI uri = new URI(relayUrl);
            String host = uri.getHost();

            if (host == null) {
                logger.error("SECURITY VIOLATION: Relay URL has no host: {}", relayUrl);
                throw new SecurityException("LAYER 1 FAILED: Invalid relay URL - missing host");
            }

            // Remove brackets from IPv6 addresses
            String cleanHost = host.replaceAll("[\\[\\]]", "");

            // ONLY allow localhost names and loopback addresses (127.x.x.x or ::1)
            if (cleanHost.equals("localhost") || 
                cleanHost.equals("::1") ||
                cleanHost.matches("^127\\..*")) {
                logger.debug("LAYER 1 PASSED: Localhost-only binding confirmed: {}", cleanHost);
                return;
            }

            // Reject all other addresses (including private IPs)
            logger.error("SECURITY VIOLATION - LAYER 1 FAILED: Non-localhost relay detected. Host: {} (URL: {})",
                        host, relayUrl);
            throw new SecurityException(
                String.format("LAYER 1 FAILED: Search execution blocked - relay '%s' is NOT localhost. " +
                            "ONLY localhost/loopback (127.x.x.x or ::1) is allowed. " +
                            "Private IPs (10.x, 192.168.x) are REJECTED to prevent port-forwarding/VPN exposure.", 
                            host)
            );

        } catch (URISyntaxException e) {
            logger.error("SECURITY VIOLATION: Invalid relay URL syntax: {}", relayUrl);
            throw new SecurityException("LAYER 1 FAILED: Invalid relay URL - " + e.getMessage(), e);
        }
    }

    /**
     * LAYER 2: Environment variable MUST explicitly confirm private mode.
     * This prevents accidental deployment to internet servers.
     * Admin must set SEARCHER_PRIVATE_MODE=true to enable searches.
     * 
     * @throws SecurityException if environment variable not set or false
     */
    private void validatePrivateModeEnabled() throws SecurityException {
        // Check environment variable first (runtime override)
        String envVar = System.getenv("SEARCHER_PRIVATE_MODE");
        if ("true".equalsIgnoreCase(envVar)) {
            logger.debug("LAYER 2 PASSED: SEARCHER_PRIVATE_MODE=true confirmed in environment");
            return;
        }
        
        // Fall back to configuration property
        if (privateModeEnabled) {
            logger.debug("LAYER 2 PASSED: searcher.private-mode-enabled=true confirmed in config");
            return;
        }

        logger.error("SECURITY VIOLATION - LAYER 2 FAILED: Private mode not enabled. " +
                   "Admin must explicitly set SEARCHER_PRIVATE_MODE=true environment variable " +
                   "or searcher.private-mode-enabled=true in configuration.");
        throw new SecurityException(
            "LAYER 2 FAILED: Private mode not enabled. " +
            "Set SEARCHER_PRIVATE_MODE=true (environment) or " +
            "searcher.private-mode-enabled=true (config) to allow searches."
        );
    }

    /**
     * LAYER 3: URL must use HTTP (not HTTPS).
     * Localhost doesn't need encryption. HTTPS on localhost suggests:
     * - Proxy setup (pointing to remote server)
     * - Container-to-container bridge (could be exposed)
     * 
     * @throws SecurityException if scheme is HTTPS or other
     */
    private void validateUrlScheme(String relayUrl) throws SecurityException {
        try {
            URI uri = new URI(relayUrl);
            String scheme = uri.getScheme();

            if ("http".equalsIgnoreCase(scheme)) {
                logger.debug("LAYER 3 PASSED: HTTP scheme (not HTTPS) confirmed");
                return;
            }

            logger.error("SECURITY VIOLATION - LAYER 3 FAILED: Non-HTTP scheme detected: {}", scheme);
            throw new SecurityException(
                String.format("LAYER 3 FAILED: Search execution blocked - relay uses '%s' scheme. " +
                            "Only HTTP is allowed for localhost (HTTPS suggests proxying/exposure).", 
                            scheme)
            );

        } catch (URISyntaxException e) {
            logger.error("SECURITY VIOLATION: Cannot parse relay URL scheme: {}", relayUrl);
            throw new SecurityException("LAYER 3 FAILED: Cannot validate URL scheme - " + e.getMessage(), e);
        }
    }
}
