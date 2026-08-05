package com.noprobit.analyzers.encoding;

public interface Encoder {
    EncodingResult encode(String text, String sourceEncoding, String targetEncoding);
}
