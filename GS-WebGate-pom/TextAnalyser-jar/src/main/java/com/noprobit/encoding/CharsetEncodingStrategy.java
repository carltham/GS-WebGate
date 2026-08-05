package com.noprobit.analyzers.encoding;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class CharsetEncodingStrategy implements Encoder {

    @Override
    public EncodingResult encode(String text, String sourceEncoding, String targetEncoding) {
        long startTime = System.currentTimeMillis();
        EncodingResult result = new EncodingResult(text, sourceEncoding, targetEncoding);

        try {
            if (text == null || text.isEmpty()) {
                return result.withSuccess(text, 0);
            }

            Charset sourceCharset = Charset.forName(sourceEncoding);
            Charset targetCharset = Charset.forName(targetEncoding);

            if (sourceEncoding.equalsIgnoreCase(targetEncoding)) {
                return result.withSuccess(text, 0);
            }

            byte[] sourceBytes = text.getBytes(sourceCharset);

            CharsetDecoder decoder = sourceCharset.newDecoder();
            CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(sourceBytes));

            CharsetEncoder encoder = targetCharset.newEncoder();
            ByteBuffer encodedBytes = encoder.encode(charBuffer);

            String convertedText = new String(encodedBytes.array(), targetCharset);
            long timeMs = System.currentTimeMillis() - startTime;

            return result.withSuccess(convertedText, timeMs);

        } catch (Exception e) {
            return result.withError(e.getMessage());
        }
    }
}
