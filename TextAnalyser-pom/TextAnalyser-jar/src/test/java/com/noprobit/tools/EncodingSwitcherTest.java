package com.noprobit.tools;

import com.noprobit.tools.encoding.EncodingSwitcher;
import com.noprobit.tools.encoding.EncodingResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

public class EncodingSwitcherTest {

    private static final String UTF_8 = "UTF-8";
    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String TEST_ENCODED_FILE = ".test-encoded.txt";
    private static final String SUCCESS_MESSAGE = "Conversion should succeed";
    private static final String TEST_SOURCE_FILE = ".test-source.txt";
    private static final String TEST_INPLACE_FILE = ".test-inplace.txt";

    private EncodingSwitcher switcher;

    @BeforeEach
    public void setup() {
        switcher = new EncodingSwitcher();
    }

    @AfterEach
    public void cleanup() throws Exception {
        Files.deleteIfExists(Paths.get(TEST_ENCODED_FILE));
        Files.deleteIfExists(Paths.get(TEST_SOURCE_FILE));
        Files.deleteIfExists(Paths.get(TEST_INPLACE_FILE));
    }

    @Test
    public void testTextConversionUTF8ToISO88591() {
        String text = "Hello World";
        EncodingResult result = switcher.convertText(text, UTF_8, ISO_8859_1);

        assertTrue(result.success, SUCCESS_MESSAGE);
        assertTrue(result.convertedText.equals("Hello World"), "Text should match");
    }

    @Test
    public void testTextConversionISOToUTF8() {
        String text = "Hello World";
        EncodingResult result = switcher.convertText(text, ISO_8859_1, UTF_8);

        assertTrue(result.success, SUCCESS_MESSAGE);
        assertTrue(result.convertedText != null, "Converted text should not be null");
        assertTrue(result.convertedText.contains("Hello"), "Converted text should contain original content");
    }

    @Test
    public void testSameEncodingNoConversion() {
        String text = "No conversion needed";
        EncodingResult result = switcher.convertText(text, UTF_8, UTF_8);

        assertTrue(result.success, SUCCESS_MESSAGE);
        assertTrue(result.conversionTimeMs == 0, "Time should be zero for same encoding");
    }

    @Test
    public void testUnsupportedSourceEncoding() {
        String text = "Test";
        EncodingResult result = switcher.convertText(text, "UNKNOWN-ENCODING", UTF_8);

        assertTrue(!result.success, "Conversion should fail with unsupported source encoding");
        assertTrue(result.errorMessage != null, "Error message should be present");
    }

    @Test
    public void testUnsupportedTargetEncoding() {
        String text = "Test";
        EncodingResult result = switcher.convertText(text, UTF_8, "UNKNOWN-ENCODING");

        assertTrue(!result.success, "Conversion should fail with unsupported target encoding");
        assertTrue(result.errorMessage != null, "Error message should be present");
    }

    @Test
    public void testFileConversion() throws Exception {
        String content = "Test content for encoding conversion";
        Files.write(Paths.get(TEST_SOURCE_FILE), content.getBytes(StandardCharsets.UTF_8));

        EncodingResult result = switcher.convertFile(TEST_SOURCE_FILE, TEST_ENCODED_FILE, UTF_8, ISO_8859_1);

        assertTrue(result.success, "File conversion should succeed");
        assertTrue(Files.exists(Paths.get(TEST_ENCODED_FILE)), "Output file should exist");

        String convertedContent = new String(Files.readAllBytes(Paths.get(TEST_ENCODED_FILE)), StandardCharsets.ISO_8859_1);
        assertTrue(convertedContent.equals(content), "Content should match");
    }

    @Test
    public void testFileConversionInPlace() throws Exception {
        String content = "Test content for in-place conversion";
        Files.write(Paths.get(TEST_INPLACE_FILE), content.getBytes(StandardCharsets.UTF_8));

        EncodingResult result = switcher.convertFileInPlace(TEST_INPLACE_FILE, UTF_8, ISO_8859_1);

        assertTrue(result.success, "In-place file conversion should succeed");

        String convertedContent = new String(Files.readAllBytes(Paths.get(TEST_INPLACE_FILE)), StandardCharsets.ISO_8859_1);
        assertTrue(convertedContent.equals(content), "Content should match");
    }

    @Test
    public void testSupportedEncodingsContainsCommon() {
        assertTrue(switcher.isEncodingSupported(UTF_8), "UTF-8 should be supported");
        assertTrue(switcher.isEncodingSupported(ISO_8859_1), "ISO-8859-1 should be supported");
        assertTrue(switcher.isEncodingSupported("Windows-1252"), "Windows-1252 should be supported");
    }

    @Test
    public void testAddCustomEncoding() {
        switcher.addSupportedEncoding("Custom-Encoding", UTF_8);
        assertTrue(switcher.isEncodingSupported("Custom-Encoding"), "Custom encoding should be supported");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
