package com.noprobit.analyzers.webgate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PurposeVerificationIT {

    @Autowired
    private MockMvc mockMvc;

    private JsonObject validRequest;

    @BeforeEach
    public void setUp() {
        validRequest = new JsonObject();
        validRequest.addProperty("className", "UserController");
        validRequest.addProperty("detectedPurpose", "CONTROLLER");
        validRequest.addProperty("keyword", "orchestrating business logic");
        validRequest.addProperty("timestamp", "2026-07-19 10:00:00");
    }

    @Test
    public void testHealthCheckEndpoint() throws Exception {
        mockMvc.perform(get("/api/verify-purpose/health")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.service").value("PurposeVerification"))
            .andExpect(jsonPath("$.version").value("1.0"));
    }

    @Test
    public void testVerifyPurposeWithControllerPattern() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/verify-purpose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.className").value("UserController"))
            .andExpect(jsonPath("$.detectedPurpose").value("CONTROLLER"))
            .andExpect(jsonPath("$.verified").exists())
            .andExpect(jsonPath("$.reason").exists())
            .andExpect(jsonPath("$.internetSource").exists())
            .andExpect(jsonPath("$.confidence").isNumber())
            .andExpect(jsonPath("$.processingTime").isNumber())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        assert json.get("confidence").getAsDouble() >= 0.0;
        assert json.get("confidence").getAsDouble() <= 1.0;
        assert json.get("processingTime").getAsLong() >= 0;
    }

    @Test
    public void testVerifyPurposeWithPanelPattern() throws Exception {
        JsonObject panelRequest = new JsonObject();
        panelRequest.addProperty("className", "UserPanel");
        panelRequest.addProperty("detectedPurpose", "PANEL");
        panelRequest.addProperty("keyword", "UI view component");
        panelRequest.addProperty("timestamp", "2026-07-19 10:00:00");

        mockMvc.perform(post("/api/verify-purpose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(panelRequest.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.className").value("UserPanel"))
            .andExpect(jsonPath("$.detectedPurpose").value("PANEL"));
    }

    @Test
    public void testVerifyPurposeWithWorkerPattern() throws Exception {
        JsonObject workerRequest = new JsonObject();
        workerRequest.addProperty("className", "DataWorker");
        workerRequest.addProperty("detectedPurpose", "WORKER");
        workerRequest.addProperty("keyword", "background async thread");
        workerRequest.addProperty("timestamp", "2026-07-19 10:00:00");

        mockMvc.perform(post("/api/verify-purpose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(workerRequest.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.verified").exists());
    }

    @Test
    public void testVerifyPurposeInvalidJson() throws Exception {
        mockMvc.perform(post("/api/verify-purpose")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    public void testVerifyPurposeMissingField() throws Exception {
        JsonObject incompleteRequest = new JsonObject();
        incompleteRequest.addProperty("className", "TestClass");

        mockMvc.perform(post("/api/verify-purpose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteRequest.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    public void testVerifyPurposeResponseStructure() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/verify-purpose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest.toString()))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        assert json.has("className");
        assert json.has("detectedPurpose");
        assert json.has("keyword");
        assert json.has("verified");
        assert json.has("reason");
        assert json.has("internetSource");
        assert json.has("confidence");
        assert json.has("processingTime");
        assert json.has("timestamp");
    }

    @Test
    public void testVerifyPurposeConfidenceRange() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/verify-purpose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest.toString()))
            .andExpect(status().isOk())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        double confidence = json.get("confidence").getAsDouble();

        assert confidence >= 0.0 && confidence <= 1.0 : "Confidence must be between 0.0 and 1.0";
    }

    @Test
    public void testMultipleConsecutiveRequests() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/verify-purpose")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequest.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").exists());
        }
    }
}
