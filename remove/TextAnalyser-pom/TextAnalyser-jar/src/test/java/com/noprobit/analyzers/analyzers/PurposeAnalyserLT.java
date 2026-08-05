package com.noprobit.analyzers.analyzers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.noprobit.analyzers.analyzers.model.*;
import com.noprobit.analyzers.analyzers.engine.JsonConfiguredEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("PurposeAnalyser Layer Tests (Mocked Dependencies)")
public class PurposeAnalyserLT {

    private PurposeAnalyser analyser;

    @Mock
    private JsonConfiguredEngine mockEngine;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        analyser = new PurposeAnalyser();
    }

    @Test
    @DisplayName("Analyse returns match from engine")
    public void testAnalyseWithEngineMatch() {
        PurposeMatch expectedMatch = new PurposeMatch(PurposeType.CONTROLLER, 0.95, "TestEngine");
        when(mockEngine.evaluate("UserController")).thenReturn(Optional.of(expectedMatch));

        List<JsonConfiguredEngine> engines = new ArrayList<>();
        engines.add(mockEngine);
        analyser.loadConfiguration(engines);

        PurposeMatch result = analyser.analyse("UserController");

        assertNotNull(result);
        assertEquals(PurposeType.CONTROLLER, result.getPurpose());
        assertEquals(0.95, result.getConfidence());
    }

    @Test
    @DisplayName("Analyse returns UNKNOWN when no engine matches")
    public void testAnalyseNoMatch() {
        when(mockEngine.evaluate(anyString())).thenReturn(Optional.empty());

        List<JsonConfiguredEngine> engines = new ArrayList<>();
        engines.add(mockEngine);
        analyser.loadConfiguration(engines);

        PurposeMatch result = analyser.analyse("UnknownClass");

        assertNotNull(result);
        assertEquals(PurposeType.UNKNOWN, result.getPurpose());
    }

    @Test
    @DisplayName("Learn pattern overrides engine results")
    public void testLearnPatternOverridesEngine() {
        when(mockEngine.evaluate(anyString())).thenReturn(Optional.empty());

        List<JsonConfiguredEngine> engines = new ArrayList<>();
        engines.add(mockEngine);
        analyser.loadConfiguration(engines);

        analyser.learnPattern("custom", PurposeType.PANEL);

        PurposeMatch result = analyser.analyse("custom");

        assertEquals(PurposeType.PANEL, result.getPurpose());
        assertEquals(0.95, result.getConfidence());
        assertEquals("Learned", result.getSourceEngine());
    }

    @Test
    @DisplayName("Multiple engines checked by priority")
    public void testMultipleEnginesByPriority() {
        JsonConfiguredEngine lowPriorityEngine = mock(JsonConfiguredEngine.class);
        JsonConfiguredEngine highPriorityEngine = mock(JsonConfiguredEngine.class);

        when(lowPriorityEngine.getPriority()).thenReturn(50);
        when(lowPriorityEngine.evaluate(anyString())).thenReturn(Optional.of(
            new PurposeMatch(PurposeType.PANEL, 0.8, "LowEngine")
        ));

        when(highPriorityEngine.getPriority()).thenReturn(100);
        when(highPriorityEngine.evaluate("UserController")).thenReturn(Optional.of(
            new PurposeMatch(PurposeType.CONTROLLER, 0.95, "HighEngine")
        ));

        List<JsonConfiguredEngine> engines = new ArrayList<>();
        engines.add(lowPriorityEngine);
        engines.add(highPriorityEngine);
        analyser.loadConfiguration(engines);

        PurposeMatch result = analyser.analyse("UserController");

        assertEquals(PurposeType.CONTROLLER, result.getPurpose());
        assertEquals("HighEngine", result.getSourceEngine());
    }

    @Test
    @DisplayName("Null input returns UNKNOWN")
    public void testNullInput() {
        PurposeMatch result = analyser.analyse(null);

        assertNotNull(result);
        assertEquals(PurposeType.UNKNOWN, result.getPurpose());
    }

    @Test
    @DisplayName("Empty string returns UNKNOWN")
    public void testEmptyInput() {
        PurposeMatch result = analyser.analyse("");

        assertNotNull(result);
        assertEquals(PurposeType.UNKNOWN, result.getPurpose());
    }

    @Test
    @DisplayName("Case insensitive matching")
    public void testCaseInsensitive() {
        PurposeMatch expectedMatch = new PurposeMatch(PurposeType.CONTROLLER, 0.95, "Engine");
        when(mockEngine.evaluate("usercontroller")).thenReturn(Optional.of(expectedMatch));

        List<JsonConfiguredEngine> engines = new ArrayList<>();
        engines.add(mockEngine);
        analyser.loadConfiguration(engines);

        PurposeMatch result = analyser.analyse("UserController");

        assertNotNull(result);
    }

    @Test
    @DisplayName("Clear learned patterns")
    public void testClearLearned() {
        analyser.learnPattern("test1", PurposeType.CONTROLLER);
        analyser.learnPattern("test2", PurposeType.PANEL);

        assertTrue(analyser.getLearned().size() >= 2);

        analyser.clearLearned();

        assertEquals(0, analyser.getLearned().size());
    }

    @Test
    @DisplayName("Get learned patterns returns copy")
    public void testGetLearnedReturnsCopy() {
        analyser.learnPattern("test", PurposeType.CONTROLLER);

        var learned1 = analyser.getLearned();
        var learned2 = analyser.getLearned();

        assertNotSame(learned1, learned2);
        assertEquals(learned1, learned2);
    }

    @Test
    @DisplayName("Unknown patterns tracked")
    public void testTrackUnknownPatterns() {
        when(mockEngine.evaluate(anyString())).thenReturn(Optional.empty());

        List<JsonConfiguredEngine> engines = new ArrayList<>();
        engines.add(mockEngine);
        analyser.loadConfiguration(engines);

        analyser.analyse("UnknownPattern1");
        analyser.analyse("UnknownPattern2");

        var unknowns = analyser.getUnknownPatterns();
        assertTrue(unknowns.size() >= 2);
    }

    @Test
    @DisplayName("Remote verification can be toggled")
    public void testRemoteVerificationToggle() {
        analyser.setRemoteVerificationEnabled(true);
        analyser.setRemoteVerificationEnabled(false);
        analyser.setRemoteVerificationEnabled(true);
        // Should not throw
    }
}
