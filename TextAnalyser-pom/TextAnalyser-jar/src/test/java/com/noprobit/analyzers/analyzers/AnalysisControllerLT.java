package com.noprobit.analyzers.analyzers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.noprobit.analyzers.analyzers.model.AnalysisResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AnalysisController Layer Tests (Mocked PurposeAnalyser)")
public class AnalysisControllerLT {

    private AnalysisController controller;

    @Mock
    private PurposeAnalyser mockAnalyser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AnalysisController(mockAnalyser);
    }

    @Test
    @DisplayName("Controller delegates to analyser")
    public void testControllerDelegates() {
        AnalysisResult expected = new AnalysisResult("UserController", "UserCtrl", "CONTROLLER", "JPanel");
        when(mockAnalyser.analyzeAndLog("UserController", "JPanel")).thenReturn(expected);

        var result = controller.analyzeHandler();

        assertNotNull(result);
        verify(mockAnalyser, never()).analyzeAndLog(anyString(), anyString());
    }

    @Test
    @DisplayName("Controller handles null extendsClass")
    public void testHandleNullExtendsClass() {
        AnalysisResult expected = new AnalysisResult("UserPanel", "UserPnl", "PANEL", null);
        when(mockAnalyser.analyzeAndLog("UserPanel", null)).thenReturn(expected);

        var result = controller.analyzeHandler();

        assertNotNull(result);
    }

    @Test
    @DisplayName("Multiple analyses delegated correctly")
    public void testMultipleAnalyses() {
        AnalysisResult result1 = new AnalysisResult("Class1", "Cls1", "CONTROLLER", null);
        AnalysisResult result2 = new AnalysisResult("Class2", "Cls2", "PANEL", null);

        when(mockAnalyser.analyzeAndLog("Class1", null)).thenReturn(result1);
        when(mockAnalyser.analyzeAndLog("Class2", null)).thenReturn(result2);

        var handler = controller.analyzeHandler();

        assertNotNull(handler);
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("Controller creates appropriate handler")
    public void testHealthHandler() {
        var healthHandler = controller.healthHandler();

        assertNotNull(healthHandler);
    }
}
