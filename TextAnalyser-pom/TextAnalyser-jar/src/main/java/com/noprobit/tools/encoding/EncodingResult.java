package com.noprobit.tools.encoding;

public class EncodingResult {
    public String originalText;
    public String sourceEncoding;
    public String targetEncoding;
    public String convertedText;
    public boolean success;
    public String errorMessage;
    public long conversionTimeMs;

    public EncodingResult(String originalText, String sourceEncoding, String targetEncoding) {
        this.originalText = originalText;
        this.sourceEncoding = sourceEncoding;
        this.targetEncoding = targetEncoding;
        this.success = false;
    }

    public EncodingResult withSuccess(String convertedText, long timeMs) {
        this.convertedText = convertedText;
        this.success = true;
        this.conversionTimeMs = timeMs;
        return this;
    }

    public EncodingResult withError(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
        this.convertedText = originalText;
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "EncodingResult{%s -> %s, success=%s, time=%dms}",
                sourceEncoding, targetEncoding, success, conversionTimeMs);
    }
}
