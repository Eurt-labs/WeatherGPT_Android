# WeatherGPT (Android) 📱🌦️

[![SIH 2026](https://img.shields.io/badge/SIH%202026-Problem%20Statement%2026068-blue?style=for-the-badge)](https://github.com/Eurt-labs/WeatherGPT_Android)
[![Android](https://img.shields.io/badge/Android-Jetpack%20Compose%20M3-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![AI Engine](https://img.shields.io/badge/AI%20Engine-Gemini%203.6%20Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://ai.google.dev/)
[![Backend](https://img.shields.io/badge/Backend-FastAPI%20on%20Render-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://weathergpt-backend-m5kk.onrender.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-green?style=for-the-badge)](LICENSE)

> **Smart India Hackathon (SIH) 2026** — Problem Statement ID: **26068**  
> **Title**: *WeatherGPT — AI-Powered Conversational Weather & Climate Intelligence Mobile Platform*

---

## 🌟 Overview & Production Target

WeatherGPT is India's premier multi-sector, conversational weather and climate intelligence system built for native Android devices. The mobile application operates as the **primary production target**, delivering real-time meteorological reasoning, localized voice interactions in 6+ Indian languages, agricultural advisories, and disaster alerts directly to users' phones.

In production, the Android app connects directly to the high-performance **Render Cloud Backend (`WeatherGPT_Backend`)** powered by **Google Gemini 3.6 Flash**. When offline or facing low connectivity, the app automatically transitions to its on-device SQLite message history, persistent disk caches, and native meteorological calculation algorithms.

---

## 🎯 Key System Capabilities

- **⚡ Multi-Provider AI Architecture**: Seamlessly switches between the Production Cloud Backend (Render), Direct Google AI Studio (15 RPM Free Tier), and OpenRouter with HTTP 402 self-healing fallback.
- **🌾 Dense Meteorological Grounding**: Telemetry from Open-Meteo grounds every response with live FAO evapotranspiration ($ET_0$), topsoil volumetric moisture, river discharge, and barometric pressure trends.
- **🗣️ Multilingual Voice AI**: ChatGPT-style fluid morphing voice sphere with native Text-to-Speech (TTS) and Sherpa-ONNX streaming speech intelligence supporting Hindi, Marathi, Bengali, Tamil, Telugu, and Indian English.
- **📍 Real-Time Fused Location & Caching**: GPS/Network location provider with automatic reverse geocoding to human-readable Indian districts and persistent disk caching.
- **🎨 Editorial OLED Design System**: Battery-efficient Pitch Black OLED (`#000000`) and Clean Minimal Light (`#FFFFFF`) themes with Champagne Beige accents and theme persistence.
- **💾 Local SQLite History**: On-device SQLite database (`ChatDatabaseHelper.kt`) preserving conversation sessions and token analytics offline.

---

## 📐 Architecture & Domain-Driven Structure (DDD)

The Android client follows strict Domain-Driven Design (DDD) principles, isolating business logic, models, and UI components into dedicated domain boundaries:

```
app/src/main/java/com/example/weathergpt_android/
├── MainActivity.kt                      # Root entry point, navigation container & theme provider
│
├── core/                                # Shared foundational modules
│   ├── network/
│   │   ├── OpenRouterService.kt         # SSE streaming client, HTTP 402 self-healing & Gemini routing
│   │   └── HmacSigner.kt                # Cryptographic HMAC-SHA256 request signer
│   ├── theme/
│   │   ├── Color.kt                     # OLED Pitch Black, Clean Minimal Light, Champagne Beige
│   │   ├── Theme.kt                     # Material3 theme provider & status bar controller
│   │   └── ThemePreferences.kt          # SharedPreferences persistence for theme selection
│   └── components/
│       ├── TopIslandHeader.kt           # Floating top island header with expandable live widget
│       └── FloatingBottomNavBar.kt      # Attached bottom navigation bar with raised Center Mic FAB
│
└── domain/                              # Feature-based domain boundaries
    ├── weather/                         # Live Open-Meteo REST client, soil/flood metrics, dashboard
    ├── assistant/                       # Real-time SSE streaming chat UI, sector chips, SQLite history
    ├── voice/                           # Fluid morphing voice sphere, Sherpa-ONNX engine, native TTS
    ├── location/                        # Fused GPS/Network location provider & reverse geocoding
    ├── news/                            # Meteorological bulletins, IMD warnings, radar updates
    ├── settings/                        # Temperature units (°C/°F), cloud backend & PC pairing
    ├── profile/                         # User profile modal (Crops, Land acreage, Sector focus)
    └── notifications/                   # Emergency weather alerts & warnings modal
```

---

## 🔄 Multi-Provider AI Routing & Offline Fallbacks

```
                               ┌───────────────────────────────────┐
                               │     WeatherGPT Android Client     │
                               │   - Multi-Sector Telemetry        │
                               │   - Live Open-Meteo Integration   │
                               └─────────────────┬─────────────────┘
                                                 │
                  ┌──────────────────────────────┴──────────────────────────────┐
                  │ (Cloud Online Mode)                                         │ (Offline / Zero-Data)
                  ▼                                                             ▼
┌───────────────────────────────────┐                         ┌───────────────────────────────────┐
│     WeatherGPT_Backend (Render)   │                         │    On-Device SQLite & Telemetry   │
│  - Hosted on Render (FastAPI)     │                         │  - ChatDatabaseHelper.kt          │
│  - Google Gemini 3.6 Flash        │                         │  - LocationCache.kt               │
│  - SlowAPI Rate Limiting          │                         │  - Native ET0 & Soil Math Layers  │
│  - Supabase Auth & Sync           │                         │  - Instant Localized Advisory     │
└───────────────────────────────────┘                         └───────────────────────────────────┘
```

1. **Cloud Render Backend**: High-concurrency streaming via Google Gemini 3.6 Flash.
2. **Direct Google AI Studio**: Zero-cost, permanent free-tier 15 RPM routing for individual users.
3. **OpenRouter Direct**: Multi-model cloud gateway with automatic provider ordering.
4. **Self-Healing Fallback**: If cloud credits deplete (HTTP 402) or the device loses internet connection, the app synthesizes a local meteorological advisory in-memory without displaying cryptic error codes.

---

## 🛠️ Getting Started & Local Setup

### Prerequisites
- **Android Studio**: Ladybug (2024.2+) or newer.
- **JDK**: Java 17 or higher.
- **Android Device / Emulator**: Android 8.0+ (API Level 26+).

### Secrets Configuration (`local.properties`)
Declare your AI inference keys in `local.properties` (never committed to Git):

```properties
OPENROUTER_API_KEY=sk-or-v1-...
GEMINI_API_KEY=AIzaSy...
```

### Build & Run
```bash
# Clone the repository
git clone https://github.com/Eurt-labs/WeatherGPT_Android.git
cd WeatherGPT_Android

# Assemble debug APK
./gradlew assembleDebug

# Or install directly onto connected device
./gradlew installDebug
```

---

## 📱 Developer Ecosystem Documentation

For architectural guardrails, streaming protocol contracts, and contributor safety rules, refer to:
- **[`AGENTS.md`](AGENTS.md)** — Architectural contracts, SSE streaming rules, and agent guardrails.
- **[`PROJECT_LOG.md`](PROJECT_LOG.md)** — Complete domain changelog, architecture logs, and bug fix history.

---

## 📄 License

This project is licensed under the Apache License 2.0 — see the [LICENSE](LICENSE) file for details.
