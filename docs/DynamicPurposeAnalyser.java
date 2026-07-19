import java.util.*;

// ==========================================
// 1. DOMAIN MODELS (Predefined Targets)
// ==========================================
enum PurposeType {
    TRANSACTION, SUPPORT, INFORMATION_SEEKING, MALICIOUS, UNKNOWN
}

class MappingRule {
    private final String pattern;
    private final PurposeType purpose;
    private final double confidence;

    public MappingRule(String pattern, PurposeType purpose, double confidence) {
        this.pattern = pattern.toLowerCase();
        this.purpose = purpose;
        this.confidence = confidence;
    }

    public String getPattern() { return pattern; }
    public PurposeType getPurpose() { return purpose; }
    public double getConfidence() { return confidence; }
}

// ==========================================
// 2. DYNAMIC ENGINE LOADED VIA CONFIG
// ==========================================
class JsonConfiguredEngine {
    private final String name;
    private final int priority;
    private final List<MappingRule> rules = new ArrayList<>();

    public JsonConfiguredEngine(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public void addRule(MappingRule rule) {
        this.rules.add(rule);
    }

    public int getPriority() { return priority; }
    public String getName() { return name; }

    /**
     * Evaluates text based on JSON-injected rule parameters
     */
    public Optional<PurposeMatch> evaluate(String contextText) {
        String input = contextText.toLowerCase();
        for (MappingRule rule : rules) {
            if (input.contains(rule.getPattern())) {
                return Optional.of(new PurposeMatch(rule.getPurpose(), rule.getConfidence(), name));
            }
        }
        return Optional.empty();
    }
}

class PurposeMatch {
    private final PurposeType purpose;
    private final double confidence;
    private final String sourceEngine;

    public PurposeMatch(PurposeType purpose, double confidence, String sourceEngine) {
        this.purpose = purpose;
        this.confidence = confidence;
        this.sourceEngine = sourceEngine;
    }

    @Override
    public String toString() {
        return String.format("[%s] (Conf: %.0f%% via JSON:%s)", purpose, confidence * 100, sourceEngine);
    }
}

// ==========================================
// 3. CORE SERVICE ORCHESTRATOR
// ==========================================
class DynamicPurposeAnalyser {
    private final List<JsonConfiguredEngine> configuredEngines = new ArrayList<>();

    /**
     * Bootstraps the engine pipeline using values parsed from your JSON configuration
     */
    public void loadConfiguration(List<JsonConfiguredEngine> engines) {
        this.configuredEngines.clear();
        this.configuredEngines.addAll(engines);
        // Sort engines dynamically by JSON-defined priority (highest first)
        this.configuredEngines.sort(Comparator.comparingInt(JsonConfiguredEngine::getPriority).reversed());
    }

    public PurposeMatch analyse(String rawText) {
        if (rawText == null || rawText.strip().isEmpty()) {
            return new PurposeMatch(PurposeType.UNKNOWN, 0.0, "System");
        }

        // Cycle through JSON configured engines sequentially based on priority
        for (JsonConfiguredEngine engine : configuredEngines) {
            Optional<PurposeMatch> match = engine.evaluate(rawText);
            if (match.isPresent()) {
                return match.get(); // Returns early if a high-priority profile hits
            }
        }

        return new PurposeMatch(PurposeType.UNKNOWN, 0.10, "Fallback");
    }
}

// ==========================================
// 4. RUNNABLE APPLICATION HARNESS
// ==========================================
public class Main {
    public static void main(String[] args) {
        DynamicPurposeAnalyser analyser = new DynamicPurposeAnalyser();

        // SIMULATION: Simulating parsing the real JSON elements into the schema objects
        List<JsonConfiguredEngine> parsedEngines = simulateJsonParsing();
        analyser.loadConfiguration(parsedEngines);

        // Test vectors evaluating against JSON defined limits
        String securePayload = "SELECT * FROM Users; DROP TABLE SystemSettings;";
        String commercialPayload = "Can you help me checkout my cart items?";
        String genericPayload = "What is the weather outside today?";

        System.out.println("Payload 1: " + analyser.analyse(securePayload));
        System.out.println("Payload 2: " + analyser.analyse(commercialPayload));
        System.out.println("Payload 3: " + analyser.analyse(genericPayload));
    }

    /**
     * Helper mimicking standard Jackson/Gson parsing loop of your purpose-map.json
     */
    private static List<JsonConfiguredEngine> simulateJsonParsing() {
        List<JsonConfiguredEngine> engines = new ArrayList<>();

        // Parse Engine A (from JSON string blocks)
        JsonConfiguredEngine security = new JsonConfiguredEngine("SecurityEngine", 100);
        security.addRule(new MappingRule("DROP TABLE", PurposeType.MALICIOUS, 1.0));
        security.addRule(new MappingRule("<script>", PurposeType.MALICIOUS, 1.0));
        engines.add(security);

        // Parse Engine B (from JSON string blocks)
        JsonConfiguredEngine intent = new JsonConfiguredEngine("IntentEngine", 50);
        intent.addRule(new MappingRule("buy", PurposeType.TRANSACTION, 0.90));
        intent.addRule(new MappingRule("checkout", PurposeType.TRANSACTION, 0.95));
        intent.addRule(new MappingRule("help", PurposeType.SUPPORT, 0.85));
        engines.add(intent);

        return engines;
    }
}
