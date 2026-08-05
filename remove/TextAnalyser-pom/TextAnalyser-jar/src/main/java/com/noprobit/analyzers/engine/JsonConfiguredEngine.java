package com.noprobit.analyzers.analyzers.engine;

import com.noprobit.analyzers.analyzers.model.MappingRule;
import com.noprobit.analyzers.analyzers.model.PurposeMatch;
import java.util.*;

/**
 * JSON Configured Engine
 * Evaluates text against a set of JSON-configured rules
 */
public class JsonConfiguredEngine {
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

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    /**
     * Evaluate text based on JSON-injected rule parameters
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
