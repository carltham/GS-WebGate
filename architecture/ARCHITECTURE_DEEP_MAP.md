# WebGate & MQ: Complete Code Map

**Version:** 1.0 (WebGate/MQ focused)  
**Date:** 2026-07-20

---

# WebGate Module: Complete Code Reference

## 📍 Package Structure

```
com.noprobit.analyzers.webgate/
├── Application
│   └── WebGateApplication.java
├── Controllers
│   └── PurposeVerificationController.java
├── Services
│   └── InternetSearchService.java
├── Models
│   ├── SearchResult.java
│   ├── QueryRequest.java
│   └── QueryResponse.java
└── Configuration
    └── application.yml
```

## 🔍 Class Details

### Application Layer

#### WebGateApplication.java (Spring Boot Entry Point)

```java
@SpringBootApplication
@EnableScheduling
public class WebGateApplication {
    
    public static void main(String[] args): void
    // Starts Spring Boot application
    // Embedded Tomcat on port 8080
    // Enables @Scheduled annotation for MQ polling (Phase 6+)
}
```

**Configuration:**
- Server Port: 8080 (configured in application.yml)
- Context Path: /webgate (optional)
- Hot-reload: Enabled (mvn spring-boot:run)

### Controller Layer

#### PurposeVerificationController.java (REST Endpoints)

```java
@RestController
@RequestMapping("/api")
public class PurposeVerificationController {
    
    @Autowired
    private InternetSearchService searchService;
    
    // Purpose Verification Endpoint
    @PostMapping("/verify-purpose")
    public ResponseEntity<String> verifyPurpose(
        @RequestBody String payload
    ): ResponseEntity<String>
    // Input: className, detectedPurpose, keyword
    // Output: verified, confidence, sources
    // Calls: InternetSearchService.search()
    
    // Generic Query Endpoint
    @PostMapping("/query")
    public ResponseEntity<String> queryGeneric(
        @RequestBody QueryRequest request
    ): ResponseEntity<String>
    // Input: question, context, maxResults, timeout
    // Output: answer, confidence, sources, processingTime
    // Calls: InternetSearchService.queryGeneric()
    
    // Health Check Endpoint
    @GetMapping("/health")
    public ResponseEntity<String> health(): ResponseEntity<String>
    // Returns: status, uptime, version, processingRequests, completedRequests
}
```

### Service Layer

#### InternetSearchService.java (DuckDuckGo Integration)

```java
@Service
public class InternetSearchService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${webgate.search.enabled:true}")
    private boolean searchEnabled;
    
    @Value("${webgate.search.timeout-ms:10000}")
    private long timeout;
    
    // Purpose Verification Search
    public SearchResult search(String query): SearchResult
    // 1. Build query: className + keyword + pattern
    // 2. Call DuckDuckGo API: /api?q=...&format=json
    // 3. Parse response (AbstractText, Answer, RelatedTopics)
    // 4. Calculate confidence based on answer type
    // 5. Return SearchResult with metadata
    
    // Generic Query Search
    public QueryResponse queryGeneric(QueryRequest request): QueryResponse
    // 1. Build query: question + context
    // 2. Add maxResults parameter
    // 3. Set timeout
    // 4. Call DuckDuckGo API
    // 5. Extract best answer (instant > abstract > related)
    // 6. Score confidence based on result type
    // 7. Return QueryResponse with sources
    
    // Internal Helpers
    private SearchResult performDuckDuckGoSearch(String query): SearchResult
    // Core DuckDuckGo API call via RestTemplate
    // Handles timeouts and errors
    
    private double calculateConfidence(
        String query, 
        String abstractText, 
        String answer
    ): double
    // Score logic:
    // - Instant answer found: 0.95
    // - Abstract found: 0.80
    // - Related topics: 0.70
    // - No results: 0.20
    
    private String buildSearchReason(
        String query, 
        String abstract_, 
        String answer, 
        double confidence
    ): String
    // Human-readable explanation of confidence score
    // Example: "Direct answer found"
    
    private SearchResult fallbackKeywordAnalysis(
        String query, 
        long startTime
    ): SearchResult
    // Fallback if DuckDuckGo call fails or times out
    // Returns low confidence result with "keyword matching" source
    
    private QueryResponse fallbackGenericResponse(
        QueryRequest request, 
        long startTime
    ): QueryResponse
    // Fallback for generic query if API fails
    // Returns answerFound=false with timeout reason
}
```

**DuckDuckGo API Integration:**
```
Endpoint: https://api.duckduckgo.com/
Method: GET

Parameters:
  q: search query (URL encoded)
  format: json
  no_html: 1
  t: textanalyser (user agent for tracking)

Response Fields Used:
  AbstractText: Summary/description (confidence 0.80)
  Answer: Instant answer (confidence 0.95)
  RelatedTopics: Related searches (confidence 0.70)

Error Handling:
  - Timeout (>10s): Return fallback with low confidence
  - Connection error: Return fallback with low confidence
  - Malformed response: Return fallback with low confidence
```

### Model Layer

#### SearchResult.java (Purpose Verification Response)

```java
public class SearchResult {
    
    private boolean relevant
    private String reason
    private String source
    private double confidence
    private long processingTime
    
    // Accessors
    public boolean isRelevant(): boolean
    public String getReason(): String
    public String getSource(): String
    public double getConfidence(): double
    public long getProcessingTime(): long
    
    // Example Response:
    // {
    //   "relevant": true,
    //   "reason": "Pattern found in search results",
    //   "source": "DuckDuckGo",
    //   "confidence": 0.85,
    //   "processingTime": 245
    // }
}
```

#### QueryRequest.java (Generic Question Request)

```java
public class QueryRequest {
    
    private String question        // Required: the question
    private String context         // Optional: contextual info
    private int maxResults         // Optional: max results (default 5)
    private long timeout           // Optional: timeout ms (default 5000)
    
    // Accessors
    public String getQuestion(): String
    public void setQuestion(String question): void
    public String getContext(): String
    public void setContext(String context): void
    public int getMaxResults(): int
    public void setMaxResults(int maxResults): void
    public long getTimeout(): long
    public void setTimeout(long timeout): void
    
    // Example Request:
    // {
    //   "question": "What is REST API?",
    //   "context": "java spring boot",
    //   "maxResults": 5,
    //   "timeout": 5000
    // }
}
```

#### QueryResponse.java (Generic Question Response)

```java
public class QueryResponse {
    
    private String question         // Original question
    private String answer           // The answer text
    private String context          // Context used for search
    private List<String> sources    // Source attribution
    private double confidence       // Score 0.0-1.0
    private long processingTime     // Time taken (ms)
    private boolean answerFound     // Whether answer found
    private String summary          // "Direct answer found" etc.
    private int maxResults          // Max results parameter used
    
    // Accessors
    public String getQuestion(): String
    public void setQuestion(String question): void
    public String getAnswer(): String
    public void setAnswer(String answer): void
    public String getContext(): String
    public void setContext(String context): void
    public List<String> getSources(): List<String>
    public void addSource(String source): void
    public double getConfidence(): double
    public void setConfidence(double confidence): void
    public long getProcessingTime(): long
    public void setProcessingTime(long processingTime): void
    public boolean isAnswerFound(): boolean
    public void setAnswerFound(boolean answerFound): void
    public String getSummary(): String
    public void setSummary(String summary): void
    public int getMaxResults(): int
    public void setMaxResults(int maxResults): void
    
    // Example Response:
    // {
    //   "question": "What is REST API?",
    //   "answer": "REST is Representational State Transfer...",
    //   "context": "java spring boot",
    //   "sources": ["DuckDuckGo", "Wikipedia"],
    //   "confidence": 0.92,
    //   "processingTime": 245,
    //   "answerFound": true,
    //   "summary": "Direct answer found",
    //   "maxResults": 5
    // }
}
```

---

# Data Flow: WebGate Processes Verification Request

## Synchronous REST Flow

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 1: JAR Detects Purpose                                │
└─────────────────────────────────────────────────────────────┘
JAR Analysis completes:
  ├─ Class: UserController
  ├─ Detected Purpose: CONTROLLER
  ├─ Local Confidence: 0.88
  └─ Decision: Needs verification (< 0.90 threshold)

┌─────────────────────────────────────────────────────────────┐
│ STEP 2: JAR Calls WebGate REST API                         │
└─────────────────────────────────────────────────────────────┘
AnalysisRequest → REST call:
  POST http://localhost:8080/webgate/api/verify-purpose
  
  Payload:
  {
    "className": "UserController",
    "detectedPurpose": "CONTROLLER",
    "keyword": "controller"
  }

┌─────────────────────────────────────────────────────────────┐
│ STEP 3: WebGate Controller Receives Request               │
└─────────────────────────────────────────────────────────────┘
PurposeVerificationController.verifyPurpose()
  ├─ Deserialize JSON payload
  ├─ Validate inputs
  └─ Call InternetSearchService.search()

┌─────────────────────────────────────────────────────────────┐
│ STEP 4: Build DuckDuckGo Query                             │
└─────────────────────────────────────────────────────────────┘
InternetSearchService.search()
  ├─ Build query: "UserController controller REST API"
  ├─ Combine className + keyword + domain patterns
  └─ URL encode for API call

┌─────────────────────────────────────────────────────────────┐
│ STEP 5: Call DuckDuckGo API                                │
└─────────────────────────────────────────────────────────────┘
RestTemplate.exchange():
  GET https://api.duckduckgo.com/?q=UserController+controller&format=json
  
  Response (example):
  {
    "AbstractText": "A controller is a class in the MVC pattern...",
    "AbstractURL": "https://example.com/mvc",
    "Answer": "REST API controllers handle HTTP requests",
    "RelatedTopics": [...]
  }

┌─────────────────────────────────────────────────────────────┐
│ STEP 6: Parse Response & Calculate Confidence             │
└─────────────────────────────────────────────────────────────┘
parseAndScore():
  ├─ Found AbstractText: true → +0.40
  ├─ Found Answer: true → +0.30
  ├─ Pattern match: "controller" in abstract → +0.25
  └─ Final confidence: 0.85 (clamped to 1.0)

┌─────────────────────────────────────────────────────────────┐
│ STEP 7: Build Response Object                              │
└─────────────────────────────────────────────────────────────┘
SearchResult result = {
  relevant: true,
  reason: "Pattern found in search results",
  source: "DuckDuckGo (Abstract + Answer)",
  confidence: 0.85,
  processingTime: 245
}

┌─────────────────────────────────────────────────────────────┐
│ STEP 8: Return to JAR                                       │
└─────────────────────────────────────────────────────────────┘
HTTP 200 OK response:
  {
    "relevant": true,
    "reason": "Pattern found in search results",
    "source": "DuckDuckGo",
    "confidence": 0.85,
    "processingTime": 245
  }

┌─────────────────────────────────────────────────────────────┐
│ STEP 9: JAR Combines Scores                                │
└─────────────────────────────────────────────────────────────┘
JAR receives verification:
  ├─ Local confidence: 0.88
  ├─ Remote confidence: 0.85
  ├─ Combined: (0.88 × 0.85) = 0.748 ≈ 0.75
  └─ Source: "Local + DuckDuckGo verification"

Final AnalysisResult:
  {
    actualName: "UserController",
    suggestedName: "UserController",
    purpose: "CONTROLLER",
    confidence: 0.75,
    sources: ["Pattern matching", "DuckDuckGo"]
  }
```

---

# REST API Contracts

## Purpose Verification Endpoint

```
POST /webgate/api/verify-purpose

Request:
{
  "className": "UserController",
  "detectedPurpose": "CONTROLLER",
  "keyword": "controller",
  "timestamp": "2026-07-20T12:00:00Z"
}

Response (200 OK):
{
  "className": "UserController",
  "detectedPurpose": "CONTROLLER",
  "verified": true,
  "reason": "Pattern found in search results",
  "source": "DuckDuckGo",
  "confidence": 0.85,
  "processingTime": 245,
  "timestamp": "2026-07-20T12:00:00Z"
}

Response (503 Service Unavailable):
{
  "error": "DuckDuckGo API unavailable",
  "verified": false,
  "confidence": 0.0
}
```

## Generic Query Endpoint

```
POST /webgate/api/query

Request:
{
  "question": "What is REST API?",
  "context": "java spring boot",
  "maxResults": 5,
  "timeout": 5000
}

Response (200 OK):
{
  "question": "What is REST API?",
  "answerFound": true,
  "answer": "REST is Representational State Transfer, an architectural style...",
  "context": "java spring boot",
  "sources": ["DuckDuckGo", "Wikipedia"],
  "confidence": 0.92,
  "processingTime": 245,
  "summary": "Direct answer found",
  "maxResults": 5
}

Response (408 Request Timeout):
{
  "question": "What is REST API?",
  "answerFound": false,
  "error": "DuckDuckGo API timeout",
  "confidence": 0.0,
  "processingTime": 5000
}
```

## Health Check Endpoint

```
GET /webgate/health

Response (200 OK):
{
  "status": "UP",
  "service": "WebGate",
  "version": "2.0",
  "mqConnected": true,
  "processingRequests": 2,
  "completedRequests": 1245,
  "uptime": 86400000
}
```

---

# Configuration

## application.yml

```yaml
spring:
  application:
    name: GS-WebGate
  boot:
    admin:
      client:
        enabled: false

server:
  port: 8080
  servlet:
    context-path: /webgate
  shutdown: graceful
  tomcat:
    threads:
      max: 200
      min-spare: 10

# Search Configuration
search:
  duckduckgo:
    enabled: true
    url: https://api.duckduckgo.com/
    timeout-ms: 10000
    user-agent: GS-WebGate/2.0
  cache:
    enabled: false
    ttl-minutes: 60
    max-size: 1000

# MQ Configuration (Phase 6+)
mq:
  host: on-site-server.example.com
  port: 7000
  connection-timeout-ms: 10000
  read-timeout-ms: 30000
  retry-attempts: 3
  retry-delay-ms: 5000

# Logging
logging:
  level:
    root: INFO
    com.noprobit.analyzers.webgate: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

# Management
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

---

# Testing

## Unit Tests

**InternetSearchServiceTest**
- Test DuckDuckGo response parsing
- Test confidence scoring logic
- Test timeout handling
- Test error fallback

**PurposeVerificationControllerTest**
- Test REST endpoint parameter validation
- Test JSON deserialization
- Test response formatting

**QueryServiceTest**
- Test generic query building
- Test answer extraction
- Test source attribution

## Layer Tests

**PurposeVerificationControllerLT**
- End-to-end REST endpoint test
- Mock InternetSearchService
- Verify request/response flow

**QueryControllerLT**
- Generic query endpoint testing
- JSON serialization testing

## Integration Tests

**PurposeVerificationIT**
- Real DuckDuckGo API calls
- Confidence scoring validation
- Timeout scenario testing

**GenericQueryServiceIT**
- Full query pipeline testing
- Answer extraction validation
- Source attribution testing

---

# Summary

WebGate provides:
- ✅ Spring Boot REST API
- ✅ DuckDuckGo integration
- ✅ Confidence scoring (0.0-1.0)
- ✅ Source attribution
- ✅ Error handling & fallback
- ✅ Answer type detection (instant/abstract/related)
- ✅ Timeout protection
- ✅ Health monitoring
- ✅ Graceful degradation
- ✅ Ready for MQ polling (Phase 6+)
