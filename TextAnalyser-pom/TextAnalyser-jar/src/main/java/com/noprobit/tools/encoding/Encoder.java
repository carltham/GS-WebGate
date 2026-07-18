package com.noprobit.tools.encoding;

public interface Encoder {
    EncodingResult encode(String text, String sourceEncoding, String targetEncoding);
}
