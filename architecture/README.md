# WebGate & MQ Architecture Documentation

**Complete Architecture Reference for WebGate and Message Queue**

---

## 📚 Documentation Structure

```
architecture/
├── README.md (this file)
│
├── components/
│   ├── WEBGATE_ARCHITECTURE.md      (Complete WebGate service)
│   └── MQ_ARCHITECTURE.md            (Message queue server)
│
├── ACTUAL_ARCHITECTURE_DATAFLOW.md  (WebGate data flows)
├── ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md (WebGate design patterns)
├── ACTUAL_ARCHITECTURE_MODULES.md   (Complete module specs)
├── ACTUAL_ARCHITECTURE_OVERVIEW.md  (System overview)
└── ARCHITECTURE_DEEP_MAP.md         (Code-level reference)
```

---

## 🎯 Quick Navigation

### Start Here

**New to WebGate?**
1. Read: [Architecture Overview](ACTUAL_ARCHITECTURE_OVERVIEW.md)
2. Read: [WebGate Architecture](components/WEBGATE_ARCHITECTURE.md)
3. Explore: [MQ Architecture](components/MQ_ARCHITECTURE.md)

**Need specific information?**

| Need | Document |
|------|----------|
| **Understand WebGate completely** | [components/WEBGATE_ARCHITECTURE.md](components/WEBGATE_ARCHITECTURE.md) |
| **Understand MQ Server** | [components/MQ_ARCHITECTURE.md](components/MQ_ARCHITECTURE.md) |
| **How WebGate processes requests** | [ACTUAL_ARCHITECTURE_DATAFLOW.md](ACTUAL_ARCHITECTURE_DATAFLOW.md) |
| **Design patterns used** | [ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md](ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md) |
| **Module specifications** | [ACTUAL_ARCHITECTURE_MODULES.md](ACTUAL_ARCHITECTURE_MODULES.md) |
| **System architecture** | [ACTUAL_ARCHITECTURE_OVERVIEW.md](ACTUAL_ARCHITECTURE_OVERVIEW.md) |
| **Code-level details** | [ARCHITECTURE_DEEP_MAP.md](ARCHITECTURE_DEEP_MAP.md) |

---

## 📖 Document Descriptions

### 🌐 components/WEBGATE_ARCHITECTURE.md
**Complete WebGate Service Documentation**
- Standalone Spring Boot gateway
- REST API endpoints (verify-purpose, query, health)
- DuckDuckGo integration
- Confidence scoring (0.0-1.0)
- Deployment (Docker, standalone JAR)
- Monitoring & operations
- Security considerations
- **Read this for:** Understanding WebGate's complete architecture

**Key Sections:**
- Project overview & characteristics
- Architecture diagram (local + distributed)
- Core components (MQPoller, SearchProcessor, InternetSearchService)
- Communication protocol (MQ polling)
- Deployment topology
- API specifications
- Configuration options
- Monitoring dashboard
- Operations checklist
- Security architecture
- Deployment checklist
- Quick start commands

### 📦 components/MQ_ARCHITECTURE.md
**Complete Message Queue Server Documentation**
- TCP server on port 7000
- FIFO request queue
- HashMap response storage
- Auto-cleanup with TTL
- 6 core commands (enqueue/dequeue request/response, has_response, stats)
- In-memory architecture
- **Read this for:** Understanding MQ operations and protocol

**Key Sections:**
- Overview & characteristics
- Architecture & message lifecycle
- Core components (MQServer, ClientHandler, MessageStore)
- Protocol specification (JSON over TCP)
- Data models
- Operations & monitoring
- Docker deployment
- Kubernetes setup
- Performance characteristics
- Operations checklist
- Success metrics

### 📡 ACTUAL_ARCHITECTURE_DATAFLOW.md
**WebGate Data Flow and Communication Patterns**
- WebGate purpose verification flow
- Generic query flow
- MQ request-response cycle
- Error handling & graceful degradation
- Communication patterns (REST vs MQ)
- **Read this for:** Understanding how data flows through WebGate and MQ

**Key Sections:**
- Workflow 1: WebGate Verifies Purpose (REST API)
- Workflow 2: WebGate Generic Query (REST API)
- Workflow 3: MQ Request-Response Cycle (polling)
- Error handling scenarios
- Communication patterns

### 🎨 ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md
**Design Patterns Used in WebGate**
- Adapter Pattern (DuckDuckGo integration)
- Polling Pattern (MQ communication)
- Graceful Degradation (optional verification)
- **Read this for:** Understanding design philosophy

**Key Sections:**
- Adapter Pattern: Hide DuckDuckGo complexity
- Polling Pattern: NAT-friendly communication
- Graceful Degradation: System works without services
- Design philosophy summary

### 📦 ACTUAL_ARCHITECTURE_MODULES.md
**Complete Module Specifications**
- WebGate module (Spring Boot gateway)
- MQ module (Message queue server)
- Module interactions & dependencies
- **Read this for:** Implementation details and specifications

**Key Sections:**
- WebGate: Package structure, classes, endpoints, configuration, data models
- MQ: Architecture, components, protocol, deployment
- Module interactions (REST + MQ)

### 🏗️ ACTUAL_ARCHITECTURE_OVERVIEW.md
**High-Level System Architecture**
- What WebGate & MQ are
- Architecture diagrams (REST vs MQ)
- Key components
- Communication patterns
- Deployment scenarios
- Design principles
- **Read this for:** Understanding how everything fits together

**Key Sections:**
- Component overview
- Architecture diagrams
- Communication patterns (sync vs async)
- Deployment scenarios
- Design principles
- Key metrics
- Configuration summary
- Testing strategy

### 🔍 ARCHITECTURE_DEEP_MAP.md
**Code-Level Architecture Reference**
- WebGate module: All classes and methods
- Data models with field details
- Data flow: Complete trace through code
- REST API contracts
- Configuration reference
- **Read this for:** Understanding every class and method

**Key Sections:**
- WebGate package structure
- All class signatures and methods
- Complete data flow with code steps
- REST API request/response examples
- Configuration file reference

---

## 🚀 Getting Started

### 1. Understand the Architecture
```
Start → ACTUAL_ARCHITECTURE_OVERVIEW.md
       → components/WEBGATE_ARCHITECTURE.md
       → components/MQ_ARCHITECTURE.md
```

### 2. Learn the Data Flow
```
Read → ACTUAL_ARCHITECTURE_DATAFLOW.md
     → Understand request → processing → response
```

### 3. Understand Design Decisions
```
Review → ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md
       → Learn why polling, adapter, graceful degradation
```

### 4. Deep Dive into Code
```
Reference → ACTUAL_ARCHITECTURE_MODULES.md
          → ARCHITECTURE_DEEP_MAP.md
          → All class details and signatures
```

---

## 📊 Key Statistics

### WebGate
- **Language:** Java 11
- **Framework:** Spring Boot 2.7.14
- **Port:** 8080
- **Lines of Code:** ~500-700 (core classes)
- **REST Endpoints:** 3 (verify-purpose, query, health)
- **External APIs:** DuckDuckGo (public, no auth)

### MQ
- **Language:** Java 11
- **Port:** 7000
- **Architecture:** TCP server, in-memory storage
- **Commands:** 6 (enqueue/dequeue request/response, has_response, stats)
- **Storage:** HashMap (thread-safe)
- **Message TTL:** 30 seconds (auto-cleanup)

---

## 🔄 Request Flow Overview

### REST API Flow (Current)
```
JAR Module
  ↓
POST /webgate/api/verify-purpose
  ↓
PurposeVerificationController
  ↓
InternetSearchService
  ↓
DuckDuckGo API (HTTPS)
  ↓
Parse & Score
  ↓
Return confidence result
  ↓
JAR combines scores
  ↓
Analysis complete
```

### MQ Polling Flow (Phase 6+)
```
JAR enqueues request to MQ
  ↓
WebGate polls MQ (500ms interval)
  ↓
WebGate dequeues request
  ↓
WebGate calls DuckDuckGo
  ↓
WebGate enqueues response
  ↓
JAR polls MQ for response
  ↓
JAR retrieves response
  ↓
Analysis complete
```

---

## 📋 Document Index

| Document | Size | Focus | Audience |
|----------|------|-------|----------|
| WEBGATE_ARCHITECTURE | ~60KB | Gateway service | Developers / DevOps |
| MQ_ARCHITECTURE | ~30KB | Message queue | Developers / DevOps |
| ACTUAL_ARCHITECTURE_DATAFLOW | ~10KB | Data flows | Developers |
| ACTUAL_ARCHITECTURE_DESIGN_PATTERNS | ~8KB | Design decisions | Architects |
| ACTUAL_ARCHITECTURE_MODULES | ~30KB | Complete specs | Developers |
| ACTUAL_ARCHITECTURE_OVERVIEW | ~20KB | Big picture | Everyone |
| ARCHITECTURE_DEEP_MAP | ~40KB | Code details | Developers |

---

## 🎯 Use Case Examples

### "How does WebGate verify a class purpose?"
→ Read: [ACTUAL_ARCHITECTURE_DATAFLOW.md](ACTUAL_ARCHITECTURE_DATAFLOW.md) - Workflow 1

### "What's the DuckDuckGo integration?"
→ Read: [ARCHITECTURE_DEEP_MAP.md](ARCHITECTURE_DEEP_MAP.md) - InternetSearchService class

### "How does MQ work?"
→ Read: [components/MQ_ARCHITECTURE.md](components/MQ_ARCHITECTURE.md) - Complete documentation

### "How do I deploy WebGate?"
→ Read: [components/WEBGATE_ARCHITECTURE.md](components/WEBGATE_ARCHITECTURE.md) - Deployment section

### "What's the REST API?"
→ Read: [ARCHITECTURE_DEEP_MAP.md](ARCHITECTURE_DEEP_MAP.md) - REST API Contracts section

### "Why use polling instead of direct calls?"
→ Read: [ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md](ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md) - Polling Pattern

### "How does graceful degradation work?"
→ Read: [ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md](ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md) - Graceful Degradation Pattern

---

## ✅ Understanding Checklist

- [ ] Read [ACTUAL_ARCHITECTURE_OVERVIEW.md](ACTUAL_ARCHITECTURE_OVERVIEW.md)
- [ ] Understand [components/WEBGATE_ARCHITECTURE.md](components/WEBGATE_ARCHITECTURE.md)
- [ ] Study [components/MQ_ARCHITECTURE.md](components/MQ_ARCHITECTURE.md)
- [ ] Review [ACTUAL_ARCHITECTURE_DATAFLOW.md](ACTUAL_ARCHITECTURE_DATAFLOW.md)
- [ ] Learn design patterns [ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md](ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md)
- [ ] Reference [ACTUAL_ARCHITECTURE_MODULES.md](ACTUAL_ARCHITECTURE_MODULES.md)
- [ ] Deep dive [ARCHITECTURE_DEEP_MAP.md](ARCHITECTURE_DEEP_MAP.md)

---

## 📞 Questions?

- **WebGate architecture?** → [components/WEBGATE_ARCHITECTURE.md](components/WEBGATE_ARCHITECTURE.md)
- **MQ architecture?** → [components/MQ_ARCHITECTURE.md](components/MQ_ARCHITECTURE.md)
- **How requests flow?** → [ACTUAL_ARCHITECTURE_DATAFLOW.md](ACTUAL_ARCHITECTURE_DATAFLOW.md)
- **Design decisions?** → [ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md](ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md)
- **Code details?** → [ARCHITECTURE_DEEP_MAP.md](ARCHITECTURE_DEEP_MAP.md)
- **Module specs?** → [ACTUAL_ARCHITECTURE_MODULES.md](ACTUAL_ARCHITECTURE_MODULES.md)

---

## 📚 Document Versions

```
WEBGATE_ARCHITECTURE.md              v2.0  | 2026-07-20 | Gateway service
MQ_ARCHITECTURE.md                   v1.0  | 2026-07-20 | Message queue
ACTUAL_ARCHITECTURE_DATAFLOW.md      v1.0  | 2026-07-20 | Data flows
ACTUAL_ARCHITECTURE_DESIGN_PATTERNS.md v1.0 | 2026-07-20 | Design patterns
ACTUAL_ARCHITECTURE_MODULES.md       v1.0  | 2026-07-20 | Module specs
ACTUAL_ARCHITECTURE_OVERVIEW.md      v1.0  | 2026-07-20 | System overview
ARCHITECTURE_DEEP_MAP.md             v1.0  | 2026-07-20 | Code reference
```

---

## 🎯 Last Updated

**2026-07-22**

All documentation is current, focused on WebGate & MQ, and production-ready.

**Start with: [ACTUAL_ARCHITECTURE_OVERVIEW.md](ACTUAL_ARCHITECTURE_OVERVIEW.md) →**
