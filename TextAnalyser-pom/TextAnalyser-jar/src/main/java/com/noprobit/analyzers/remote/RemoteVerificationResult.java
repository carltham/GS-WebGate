package com.noprobit.analyzers.analyzers.remote;

/**
 * Remote Verification Result
 * Result from WebGate remote verification via internet search
 */
public class RemoteVerificationResult {
    public boolean verified;
    public String reason;
    public String internetSource;

    public RemoteVerificationResult(boolean verified, String reason, String internetSource) {
        this.verified = verified;
        this.reason = reason;
        this.internetSource = internetSource;
    }

    @Override
    public String toString() {
        return String.format("RemoteVerification{verified=%b, reason='%s', source='%s'}",
            verified, reason, internetSource != null ? internetSource : "N/A");
    }
}
