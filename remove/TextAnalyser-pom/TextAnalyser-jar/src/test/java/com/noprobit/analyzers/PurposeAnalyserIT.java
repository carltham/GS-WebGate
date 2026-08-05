package com.noprobit.analyzers.analyzers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import com.noprobit.analyzers.analyzers.model.*;
import com.noprobit.analyzers.analyzers.config.PurposeMappingLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PurposeAnalyser Integration Tests")
public class PurposeAnalyserIT {

    private PurposeAnalyser analyser;
    private Path logDir;

    @BeforeEach
    public void setUp() {
        analyser = new PurposeAnalyser();
        logDir = Paths.get("logs");
    }

    @Test
    @DisplayName("Load configuration from classpath on initialization")
    public void testLoadConfigurationOnInit() {
        assertNotNull(analyser);
        List<UnknownPattern> patterns = analyser.getUnknownPatterns();
        assertNotNull(patterns);
    }

    @Test
    @DisplayName("Analyse controller class name")
    public void testAnalyseControllerClass() {
        PurposeMatch match = analyser.analyse("UserController");

        assertNotNull(match);
        assertNotNull(match.getPurpose());
        assertTrue(match.getConfidence() > 0.0);
    }

    @Test
    @DisplayName("Analyse panel class name")
    public void testAnalysePanelClass() {
        PurposeMatch match = analyser.analyse("UserPanel");

        assertNotNull(match);
        assertEquals(PurposeType.PANEL, match.getPurpose());
        assertTrue(match.getConfidence() > 0.8);
    }

    @Test
    @DisplayName("Analyse worker class name")
    public void testAnalyseWorkerClass() {
        PurposeMatch match = analyser.analyse("DataWorker");

        assertNotNull(match);
        assertEquals(PurposeType.WORKER, match.getPurpose());
        assertTrue(match.getConfidence() > 0.8);
    }

    @Test
    @DisplayName("Analyse validator class name")
    public void testAnalyseValidatorClass() {
        PurposeMatch match = analyser.analyse("InputValidator");

        assertNotNull(match);
        assertEquals(PurposeType.VALIDATOR, match.getPurpose());
        assertTrue(match.getConfidence() > 0.8);
    }

    @Test
    @DisplayName("Analyse exporter class name")
    public void testAnalyseExporterClass() {
        PurposeMatch match = analyser.analyse("ReportExporter");

        assertNotNull(match);
        assertEquals(PurposeType.EXPORTER, match.getPurpose());
        assertTrue(match.getConfidence() > 0.8);
    }

    @Test
    @DisplayName("Analyse unknown class name")
    public void testAnalyseUnknownClass() {
        PurposeMatch match = analyser.analyse("XyzAbc123");

        assertNotNull(match);
        assertEquals(PurposeType.UNKNOWN, match.getPurpose());
        assertTrue(match.getConfidence() < 0.5);
    }

    @Test
    @DisplayName("Learn and recall patterns")
    public void testLearnPattern() {
        analyser.learnPattern("custom", PurposeType.CONTROLLER);

        PurposeMatch match = analyser.analyse("custom");
        assertEquals(PurposeType.CONTROLLER, match.getPurpose());
        assertEquals(0.95, match.getConfidence());
    }

    @Test
    @DisplayName("Track unknown patterns")
    public void testTrackUnknownPatterns() {
        analyser.analyse("UnknownXyz");
        analyser.analyse("UnknownXyz");
        analyser.analyse("AnotherUnknown");

        List<UnknownPattern> unknown = analyser.getUnknownPatterns();
        assertNotNull(unknown);
        assertTrue(unknown.size() >= 2);
    }

    @Test
    @DisplayName("Log analysis to file")
    public void testLogAnalysis() {
        analyser.logAnalysis("UserController", "UserCtrl", "CONTROLLER", "Object");

        Path logFile = analyser.getPurposeLogFile();
        assertTrue(Files.exists(logFile));
    }

    @Test
    @DisplayName("Analyse with parent class hint")
    public void testAnalyseWithParentClass() {
        String purpose = analyser.analyzePurpose("UserPanel", "JPanel");

        assertNotNull(purpose);
        assertTrue(purpose.toLowerCase().contains("panel"));
    }

    @Test
    @DisplayName("Suggest improved name for controller")
    public void testSuggestNameForController() {
        String suggested = analyser.suggestName("UserCtr", "Object");

        assertNotNull(suggested);
        assertFalse(suggested.isEmpty());
    }

    @Test
    @DisplayName("Validate PascalCase names")
    public void testValidatePascalCase() {
        assertTrue(analyser.isPascalCase("UserController"));
        assertTrue(analyser.isPascalCase("DataPanel"));
        assertFalse(analyser.isPascalCase("userController"));
        assertFalse(analyser.isPascalCase("user_controller"));
    }

    @Test
    @DisplayName("Comprehensive analysis with logging")
    public void testAnalyzeAndLog() {
        AnalysisResult result = analyser.analyzeAndLog("UserController", "Object");

        assertNotNull(result);
        assertNotNull(result.actualName);
        assertNotNull(result.suggestedName);
        assertNotNull(result.purpose);
    }

    @Test
    @DisplayName("Clear learned patterns")
    public void testClearLearned() {
        analyser.learnPattern("test1", PurposeType.CONTROLLER);
        analyser.learnPattern("test2", PurposeType.PANEL);

        int learnedCount = analyser.getLearned().size();
        assertTrue(learnedCount >= 2);

        analyser.clearLearned();
        assertEquals(0, analyser.getLearned().size());
    }

    @Test
    @DisplayName("Get learned patterns")
    public void testGetLearned() {
        analyser.clearLearned();
        analyser.learnPattern("ctrl", PurposeType.CONTROLLER);
        analyser.learnPattern("pnl", PurposeType.PANEL);

        var learned = analyser.getLearned();
        assertEquals(2, learned.size());
        assertTrue(learned.containsKey("ctrl"));
        assertTrue(learned.containsKey("pnl"));
    }

    @Test
    @DisplayName("Reload configuration")
    public void testReloadConfiguration() {
        assertDoesNotThrow(() -> analyser.reloadConfiguration());
    }

    @Test
    @DisplayName("Set and verify WebGate URL")
    public void testSetWebgateUrl() {
        analyser.setWebgateUrl("http://custom:9090/api");
        // Configuration set, verify through behavior
        analyser.setRemoteVerificationEnabled(true);
    }

    @Test
    @DisplayName("Enable/disable remote verification")
    public void testRemoteVerificationToggle() {
        analyser.setRemoteVerificationEnabled(true);
        analyser.setRemoteVerificationEnabled(false);
        analyser.setRemoteVerificationEnabled(true);
        // Should not throw
    }

    @Test
    @DisplayName("Analyse empty string")
    public void testAnalyseEmptyString() {
        PurposeMatch match = analyser.analyse("");

        assertNotNull(match);
        assertEquals(PurposeType.UNKNOWN, match.getPurpose());
    }

    @Test
    @DisplayName("Analyse null string")
    public void testAnalyseNull() {
        PurposeMatch match = analyser.analyse(null);

        assertNotNull(match);
        assertEquals(PurposeType.UNKNOWN, match.getPurpose());
    }

    @Test
    @DisplayName("Analyse case insensitivity")
    public void testAnalyseCaseInsensitive() {
        PurposeMatch match1 = analyser.analyse("UserController");
        PurposeMatch match2 = analyser.analyse("usercontroller");
        PurposeMatch match3 = analyser.analyse("USERCONTROLLER");

        assertEquals(match1.getPurpose(), match2.getPurpose());
        assertEquals(match2.getPurpose(), match3.getPurpose());
    }

    @Test
    @DisplayName("Multiple consecutive analyses")
    public void testMultipleAnalyses() {
        String[] classes = {"UserController", "UserPanel", "DataWorker", "InputValidator", "ReportExporter"};

        for (String className : classes) {
            PurposeMatch match = analyser.analyse(className);
            assertNotNull(match);
            assertNotNull(match.getPurpose());
        }
    }

    @Test
    @DisplayName("Extract entity from class name")
    public void testExtractEntity() {
        String purpose1 = analyser.analyzePurpose("CategoryPanel", "JPanel");
        String purpose2 = analyser.analyzePurpose("ProductWorker", null);
        String purpose3 = analyser.analyzePurpose("CashierController", null);

        assertNotNull(purpose1);
        assertNotNull(purpose2);
        assertNotNull(purpose3);
    }

    @Test
    @DisplayName("Log file is created in correct location")
    public void testLogFileLocation() {
        analyser.logAnalysis("TestClass", "TestCls", "TEST", null);

        Path logFile = analyser.getPurposeLogFile();
        assertNotNull(logFile);
        assertTrue(logFile.toString().contains("logs"));
        assertTrue(logFile.toString().contains("purpose-analysis.log"));
    }
}
