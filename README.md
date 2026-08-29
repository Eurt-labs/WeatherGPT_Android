# WeatherGPT (Android) 🌤️🤖

[![SIH 2026](https://img.shields.io/badge/SIH%202026-Problem%20Statement%2026068-blue?style=for-the-badge)](https://github.com/Eurt-labs/WeatherGPT_Android)
[![Android](https://img.shields.io/badge/Android-Jetpack%20Compose-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Architecture](https://img.shields.io/badge/Architecture-Domain--Driven%20Design%20(DDD)-orange?style=for-the-badge)](#-architecture--package-structure)
[![License](https://img.shields.io/badge/License-Apache%202.0-green?style=for-the-badge)](LICENSE)

> **Smart India Hackathon (SIH) 2026** — Problem Statement ID: **26068**  
> **Title**: *WeatherGPT — AI-Powered Conversational Weather & Climate Intelligence Mobile Platform*

---

## 📌 Problem Background & Context

Weather and meteorological information is traditionally fragmented across disparate portals, bulletin systems, satellite imagery channels, and complex numerical forecast models. This fragmentation creates significant friction for everyday citizens, rural agricultural communities, disaster response authorities, aviation operators, and researchers trying to extract actionable, timely insights.

**WeatherGPT** bridges this gap through a mobile-first, conversational AI platform designed with **flushed Apple-inspired aesthetics**, floating dynamic islands, tactile micro-animations, and rural voice accessibility.

---

## 🎯 Project Objectives

- **Unified Intelligence**: Consolidate meteorological datasets, forecasting models (GFS/WRF), and disaster warning systems into a single conversational interface.
- **Natural Language Interaction**: Enable complex weather queries in plain language (e.g., *"What is the best window for wheat harvest in my village?"*).
- **Rural Accessibility**: Provide hands-free **Voice AI** interaction with multilingual support for regional Indian languages.
- **Actionable Decision Support**: Deliver hyper-localized advisory generation for agriculture, aviation briefing, urban planning, and cyclone/flood preparedness.

---

## 📱 Mobile UI & Design System

The Android application is developed with **Jetpack Compose** and follows a **Flushed Light-Themed Design Language**:

1. **Top Dynamic Island Header**:
   - Floating rounded capsule (`30dp` radius) detached from status bars.
   - Interactive notification bell with unread badge counter.
   - Expandable live weather widget on tap showing temperature, AQI, and rain probability.
   - User account avatar with profile management bottom sheet.
2. **Floating Island Navigation Bar**:
   - Detached bottom pill (`34dp` radius) with ambient shadow and **no harsh outlines**.
   - Spring-bouncing icon interactions and active pill indicator.
   - 5 Navigation Destinations: **Weather Home**, **Climate News**, **Voice AI**, **WeatherGPT Chat**, and **Settings**.
3. **Domain-Driven Architecture (DDD)**:
   - Strict modular separation of domain models, UI components, theme tokens, and navigation.

---

## 🏗️ Architecture & Package Structure

```
com.example.weathergpt_android/
├── MainActivity.kt                      # Root entry point & edge-to-edge Scaffold
│
├── core/                                # Shared foundational modules
│   ├── theme/                           # Flushed Light Theme Design System
│   │   ├── Color.kt                     # Palette & semantic accent tokens
│   │   ├── Type.kt                      # Typography scale definitions
│   │   └── Theme.kt                     # Material3 Theme & transparent system bars
│   ├── navigation/                      # App navigation contract
│   │   └── NavTab.kt                    # Nav tabs & AutoMirrored icons
│   └── components/                      # Core floating island components
│       ├── TopIslandHeader.kt           # Dynamic top pill island & expanded widget
│       └── FloatingBottomNavBar.kt      # Floating bottom island navigation bar
│
└── domain/                              # Feature-based domain boundaries
    ├── weather/                         # Weather Home Domain
    │   ├── model/WeatherModels.kt       # WeatherScenario, HourlyForecast, DayForecast, WeatherMetrics
    │   └── ui/
    │       ├── HomeScreen.kt            # Weather dashboard with interactive scenario switcher
    │       └── WeatherCards.kt          # Hero greeting, AI suggestion pill, metric grid, 7-day outlook
    │
    ├── news/                            # Weather News & Radar Domain
    │   ├── model/NewsArticle.kt         # NewsArticle model
    │   └── ui/NewsScreen.kt             # News feed, climate advisories, filter pills
    │
    ├── voice/                           # Voice AI Domain
    │   ├── model/VoiceModels.kt         # VoiceInteractionState, VoiceSuggestion
    │   └── ui/VoiceAiScreen.kt          # Pulsing voice orb, equalizer bars, quick suggestions
    │
    ├── assistant/                       # WeatherGPT Assistant Domain
    │   ├── model/ChatModels.kt          # ChatMessage data model
    │   └── ui/GptChatScreen.kt          # Conversational chat UI, weather bubble pills, input capsule
    │
    ├── settings/                        # User Preferences Domain
    │   └── ui/SettingsScreen.kt         # Temperature unit switch (°C/°F), alert preferences, about card
    │
    ├── notifications/                   # Notifications Domain
    │   ├── model/WeatherNotification.kt # Notification data model
    │   └── ui/NotificationSheet.kt      # Notification modal bottom sheet
    │
    └── profile/                         # User Profile Domain
        └── ui/ProfileSheet.kt           # User profile modal bottom sheet
```

---

## 📋 Implementation Progress & Roadmap (Tracking Checklist)

### 🎨 Phase 1: Frontend & UI Implementation (Current Phase)
- [x] **Project Scaffolding**: Gradle Kotlin DSL, Version Catalog (`libs.versions.toml`), AGP 9.3.2, Kotlin 2.0.21.
- [x] **Flushed Design System**: Light theme color palette, squircle shapes, and typography.
- [x] **Top Dynamic Island**:
  - [x] Floating pill capsule header with zero border outlines.
  - [x] Notification bell with unread badge counter and modal sheet.
  - [x] Profile avatar with user preferences modal sheet.
  - [x] Animated expandable live weather widget on title tap.
- [x] **Floating Bottom Island Nav Bar**:
  - [x] Detached pill navigation with spring physics and scale bounce.
  - [x] 5 core tabs (Weather, News, Voice AI, GPT Assistant, Settings).
- [x] **Weather Dashboard Screen**:
  - [x] Wireframe Hero Greeting: *"Hi Dhruv 👋, today's weather is good. Suggestion: go for a walk"*.
  - [x] Interactive weather scenario switcher (`☀️ Sunny`, `🍃 Breezy`, `🌧️ Rain`, `🌇 Golden Hour`).
  - [x] Real-time primary weather overview card.
  - [x] Hourly forecast horizontal pill carousel.
  - [x] 2x2 Metric Grid (Wind Speed, Humidity, UV Index, Air Quality AQI).
  - [x] 7-Day Extended forecast with progress indicators.
- [x] **Climate & Weather News Screen**:
  - [x] Meteorology feed with category filter chips (`All`, `Advisories`, `Global Climate`, `Science`, `Radar`).
  - [x] Article bookmarking and share triggers.
- [x] **Voice AI Assistant Screen**:
  - [x] Animated pulsing voice orb visualizer.
  - [x] Live audio soundwave equalizer bars.
  - [x] Instant voice query suggestion pills.
- [x] **WeatherGPT Assistant Chat Screen**:
  - [x] Conversational message bubbles with weather highlight pills.
  - [x] Quick prompt recommendation chips.
  - [x] Modern floating text input capsule.
- [x] **Settings & Preferences Screen**:
  - [x] Unit switcher pill (`°C` vs `°F`).
  - [x] Notification preferences toggles (Severe alerts, daily walk prompts).
- [x] **Domain-Driven Refactoring (DDD)**: Modular package structure and clean models.
- [x] **Architecture & Build Logging**: Comprehensive [PROJECT_LOG.md](PROJECT_LOG.md).

---

### ⚙️ Phase 2: Backend, AI Engine & Data Integration (Upcoming Phases)
- [ ] **Backend Services & API Gateway**:
  - [ ] Python / FastAPI asynchronous backend services.
  - [ ] WebSocket / MQTT / WIS2.0 protocol for real-time sensor & bulletin streaming.
- [ ] **Numerical Weather Prediction (NWP) Pipelines**:
  - [ ] Ingestion pipelines for GFS (Global Forecast System) and WRF (Weather Research and Forecasting) models.
  - [ ] IMD (India Meteorological Department) data integration.
  - [ ] OpenWeatherMap / ECMWF satellite and radar tile mapping.
- [ ] **AI / LLM Conversational Engine**:
  - [ ] Multi-agent query understanding (OpenAI GPT-4o / Google Gemini / Meta Llama 3).
  - [ ] Domain-specific meteorological fine-tuning and RAG (Retrieval-Augmented Generation).
  - [ ] Natural language weather explanation engine.
- [ ] **Multilingual & Rural Voice Engine**:
  - [ ] Regional Indian language support (Hindi, Marathi, Bengali, Tamil, Telugu, Kannada, Gujarati, Punjabi, etc.).
  - [ ] Speech-to-Text (STT) and Text-to-Speech (TTS) integration with low-bandwidth offline caching.
- [ ] **Disaster Warning & Push Notification Engine**:
  - [ ] Real-time cyclone, flash flood, and heatwave early warning system.
  - [ ] Firebase Cloud Messaging (FCM) push alerts with Geo-fencing.
- [ ] **Cloud & Database Infrastructure**:
  - [ ] Geospatial Database (PostgreSQL + PostGIS / MongoDB).
  - [ ] Containerization with Docker & Kubernetes.

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Mobile App (Frontend)** | Android (Kotlin 2.0.21), Jetpack Compose, Material3, Navigation Compose, Coroutines |
| **UI Design System** | Flushed Light Theme, Apple Dynamic Island, Spring Physics, Diffuse Shadows |
| **Architecture** | Domain-Driven Design (DDD), Feature Modular Pattern |
| **Future Backend** | Python (FastAPI), Node.js, WebSockets, MQTT, WIS2.0 |
| **Future AI / LLM** | Gemini / GPT-4o / Llama 3 with Meteorological RAG |
| **Future Geospatial & NWP** | PostGIS, GFS/WRF datasets, IMD Open APIs, Satellite Radar Tiles |

---

## 💡 Practical Use Cases

1. **🌾 Agriculture & Farmers**:
   - Hyper-local micro-climate advice for sowing, irrigation, pesticide spraying, and harvesting windows in regional languages.
2. **✈️ Aviation & Maritime Operations**:
   - Terminal Aerodrome Forecasts (TAF), METAR briefings, crosswind alerts, and sea swell conditions.
3. **🚨 Disaster Management & Early Warning**:
   - Instant dissemination of cyclone paths, flash flood alerts, and extreme weather warnings to at-risk populations.
4. **🏙️ Smart Cities & Urban Planning**:
   - Heat island monitoring, urban drainage flood risk indices, and localized air quality analytics.
5. **🔬 Climate Researchers & Policy Makers**:
   - Historical temperature anomalies, rainfall trends, and seasonal monsoon tracking.

---

## 🚀 Getting Started & Local Setup

### Prerequisites
- Android Studio Ladybug | 2024.2.1 or newer (or Antigravity IDE).
- JDK 17 or higher (e.g., JetBrains Runtime / OpenJDK 17).
- Android SDK Platform 35 (`compileSdk = 35`, `minSdk = 26`).

### Clone and Build
```bash
# Clone the repository
git clone https://github.com/Eurt-labs/WeatherGPT_Android.git
cd WeatherGPT_Android

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew test
```

---

## 👥 Contributors & Acknowledgements

- **Team**: Sentinel Core
- **Event**: Smart India Hackathon (SIH) 2026 — Problem Statement **26068**
- **Repository**: [https://github.com/Eurt-labs/WeatherGPT_Android.git](https://github.com/Eurt-labs/WeatherGPT_Android.git)
