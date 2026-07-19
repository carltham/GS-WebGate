package com.noprobit.analyzers.encoding;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class EncodingSwitcher {

    private final Encoder encoder;
    private final Map<String, String> supportedEncodings = new HashMap<>();

    public EncodingSwitcher() {
        this(new CharsetEncodingStrategy());
    }

    public EncodingSwitcher(Encoder encoder) {
        this.encoder = encoder;
        initializeSupportedEncodings();
    }

    private void initializeSupportedEncodings() {
        supportedEncodings.put("UTF-8", "UTF-8");
        supportedEncodings.put("ISO-8859-1", "ISO-8859-1");
        supportedEncodings.put("Windows-1252", "Windows-1252");
        supportedEncodings.put("US-ASCII", "US-ASCII");
        supportedEncodings.put("UTF-16", "UTF-16");
        supportedEncodings.put("UTF-16BE", "UTF-16BE");
        supportedEncodings.put("UTF-16LE", "UTF-16LE");
        supportedEncodings.put("Shift-JIS", "Shift-JIS");
        supportedEncodings.put("EUC-JP", "EUC-JP");
        supportedEncodings.put("GB2312", "GB2312");
        supportedEncodings.put("GBK", "GBK");
        supportedEncodings.put("Big5", "Big5");
    }

    public EncodingResult convertText(String text, String sourceEncoding, String targetEncoding) {
        if (!isEncodingSupported(sourceEncoding)) {
            EncodingResult result = new EncodingResult(text, sourceEncoding, targetEncoding);
            return result.withError("Source encoding not supported: " + sourceEncoding);
        }

        if (!isEncodingSupported(targetEncoding)) {
            EncodingResult result = new EncodingResult(text, sourceEncoding, targetEncoding);
            return result.withError("Target encoding not supported: " + targetEncoding);
        }

        return encoder.encode(text, sourceEncoding, targetEncoding);
    }

    public EncodingResult convertFile(String sourceFilePath, String targetFilePath,
                                      String sourceEncoding, String targetEncoding) {
        try {
            Path source = Paths.get(sourceFilePath);
            String fileContent = new String(Files.readAllBytes(source), sourceEncoding);

            EncodingResult result = convertText(fileContent, sourceEncoding, targetEncoding);

            if (result.success) {
                Path target = Paths.get(targetFilePath);
                Files.write(target, result.convertedText.getBytes(targetEncoding),
                           StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            return result;

        } catch (Exception e) {
            EncodingResult result = new EncodingResult("", sourceEncoding, targetEncoding);
            return result.withError("File conversion failed: " + e.getMessage());
        }
    }

    public EncodingResult convertFileInPlace(String filePath, String sourceEncoding, String targetEncoding) {
        return convertFile(filePath, filePath, sourceEncoding, targetEncoding);
    }

    public boolean isEncodingSupported(String encoding) {
        if (encoding == null || encoding.isEmpty()) {
            return false;
        }
        return supportedEncodings.containsKey(encoding) ||
               supportedEncodings.containsValue(encoding);
    }

    public Map<String, String> getSupportedEncodings() {
        return new HashMap<>(supportedEncodings);
    }

    public void addSupportedEncoding(String name, String charset) {
        supportedEncodings.put(name, charset);
    }

    public String getCharsetName(String encodingName) {
        return supportedEncodings.getOrDefault(encodingName, encodingName);
    }
}
