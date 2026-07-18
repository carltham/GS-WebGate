package com.noprobit.tools.encoding;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.Arrays;

public class AdvancedEncodingEngine {

    public static class DetectionResult {
        public final Charset charset;
        public final boolean hasBOM;
        public final int bomLength;

        public DetectionResult(Charset charset, boolean hasBOM, int bomLength) {
            this.charset = charset;
            this.hasBOM = hasBOM;
            this.bomLength = bomLength;
        }
    }

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final byte[] UTF16BE_BOM = {(byte) 0xFE, (byte) 0xFF};
    private static final byte[] UTF16LE_BOM = {(byte) 0xFF, (byte) 0xFE};

    public static DetectionResult detectEncodingAndBOM(Path filePath) throws IOException {
        byte[] buffer = new byte[3];
        try (InputStream is = Files.newInputStream(filePath)) {
            int bytesRead = is.read(buffer);
            if (bytesRead >= 3 && Arrays.equals(buffer, UTF8_BOM)) {
                return new DetectionResult(StandardCharsets.UTF_8, true, 3);
            }
            if (bytesRead >= 2) {
                if (buffer[0] == UTF16BE_BOM[0] && buffer[1] == UTF16BE_BOM[1]) {
                    return new DetectionResult(StandardCharsets.UTF_16BE, true, 2);
                }
                if (buffer[0] == UTF16LE_BOM[0] && buffer[1] == UTF16LE_BOM[1]) {
                    return new DetectionResult(StandardCharsets.UTF_16LE, true, 2);
                }
            }
        }
        return new DetectionResult(StandardCharsets.UTF_8, false, 0);
    }

    public static void safeTranslateFile(Path source, Charset sourceFallback,
                                         Path target, Charset targetEncoding) throws IOException {

        DetectionResult detection = detectEncodingAndBOM(source);
        Charset actualSourceCharset = detection.hasBOM ? detection.charset : sourceFallback;

        CharsetDecoder decoder = actualSourceCharset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        CharsetEncoder encoder = targetEncoding.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try (FileChannel inChannel = FileChannel.open(source, StandardOpenOption.READ);
             FileChannel outChannel = FileChannel.open(target, StandardOpenOption.CREATE,
                     StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

            inChannel.position(detection.bomLength);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(65536);
            CharBuffer charBuffer = CharBuffer.allocate(65536);

            while (inChannel.read(byteBuffer) != -1) {
                byteBuffer.flip();

                CoderResult decodeResult = decoder.decode(byteBuffer, charBuffer, false);
                if (decodeResult.isError()) {
                    throw new CharacterCodingException();
                }

                charBuffer.flip();
                ByteBuffer targetBytes = encoder.encode(charBuffer);
                outChannel.write(targetBytes);

                byteBuffer.compact();
                charBuffer.clear();
            }

            byteBuffer.flip();
            decoder.decode(byteBuffer, charBuffer, true);
            charBuffer.flip();
            outChannel.write(encoder.encode(charBuffer));
        }
    }

    public static String readFileWithEncodingDetection(Path filePath) throws IOException {
        DetectionResult detection = detectEncodingAndBOM(filePath);
        byte[] allBytes = Files.readAllBytes(filePath);

        CharsetDecoder decoder = detection.charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);

        ByteBuffer byteBuffer = ByteBuffer.wrap(allBytes, detection.bomLength,
                allBytes.length - detection.bomLength);
        CharBuffer charBuffer = decoder.decode(byteBuffer);
        return charBuffer.toString();
    }
}
