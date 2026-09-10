# WeatherGPT Android — Changelog & Version History

All notable changes to the WeatherGPT Android project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [3.5.2] - 2026-09-10
### Fixed
- **Main Page Widget & Card Crash Resolution** ([`ChatDatabaseHelper.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/assistant/data/ChatDatabaseHelper.kt)):
  - **Root Cause**: `ChatDatabaseHelper` was calling `.use { }` on `readableDatabase` and `writableDatabase`. Because `use` invokes `close()` on the database, the singleton connection was closed after the initial count query on `UnifiedMainScreen`. Tapping any widget or box ("Live Weather", "Disaster Radar", "Kisan AI", quick pills, bottom bar) crashed with `IllegalStateException: attempt to re-open an already-closed object: SQLiteDatabase`.
  - **Fix**: Removed database-closing `.use` blocks across all queries in `ChatDatabaseHelper`, ensuring persistent connection reuse and scoped `cursor.use` disposal. Wrapped all database calls in resilient `try-catch` handlers.
- **ONNX Runtime Linkage Hardening** ([`SherpaOnnxEngine.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/voice/sherpa/engine/SherpaOnnxEngine.kt)):
  - Caught `Throwable` instead of `Exception` during `OrtEnvironment.getEnvironment()` initialization and release to prevent native `UnsatisfiedLinkError` crashes on devices without compatible C++ binaries.
- **Voice AI Permission & Lifecycle Protection** ([`ImmersiveVoiceScreen.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/voice/ui/ImmersiveVoiceScreen.kt)):
  - Added explicit runtime `RECORD_AUDIO` permission verification before invoking `SpeechRecognizer.startListening`.
  - Debounced `onError` retries to prevent infinite recursion loops and ANR.
  - Wrapped `SpeechRecognizer` and `TextToSpeech` initialization and cleanup in `try-catch` blocks.

---

## [3.5.1] - 2026-09-10
### Fixed & Improved
- **Gemini 2.5 Flash Integration & OpenRouter Link**:
  - Locked default AI model strictly to **Google Gemini 2.5 Flash** (`google/gemini-2.5-flash` on OpenRouter, `gemini-2.5-flash` on Google Direct API).
  - Injected `OPENROUTER_API_KEY` and `GEMINI_API_KEY` from `local.properties` directly into `BuildConfig` via `app/build.gradle.kts`.
  - Added automatic fallback to `BuildConfig.OPENROUTER_API_KEY` in `AiPreferences.getOpenRouterApiKey()` and automatic provider mode selection when keys are defined in `local.properties`.
  - Added mandatory OpenRouter headers (`HTTP-Referer`, `X-Title`) and whitespace/Bearer prefix sanitization to prevent connection rejections.
  - Implemented detailed JSON error extraction for OpenRouter HTTP 400, 401, 402, and 429 errors.

---

## [3.5.0] - 2026-09-10
### Added
- **Multi-Provider AI Engine Architecture** ([`AiPreferences.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/core/network/AiPreferences.kt)):
  - Added support for 3 selectable AI provider modes:
    1. `Cloud Backend` (Render FastAPI default).
    2. `Google Gemini Direct` (100% free forever, 15 RPM from Google AI Studio, no credit card required).
    3. `OpenRouter Direct` (Personal keys and custom models).
- **HTTP 402 Interception & Self-Healing Fallback Engine** ([`OpenRouterService.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/core/network/OpenRouterService.kt)):
  - Intercepts HTTP 402 ("Payment Required") status codes and upstream SSE error tokens when cloud credit balances hit $0.00.
  - Auto-switches seamlessly to Google Gemini Direct if a user key is saved.
  - Otherwise generates an **Intelligent Local Meteorological Advisory** directly from live Open-Meteo telemetry in memory (temperature, feels-like, wind, humidity, precipitation timing, and agricultural soil guidance).
  - In voice mode, synthesizes a natural 2-sentence conversational summary instead of speaking raw error codes aloud.
- **AI Settings & Connection Diagnostics UI** ([`FrostedSettingsSheet.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/settings/ui/FrostedSettingsSheet.kt)):
  - Added "AI REASONING ENGINE" card with provider selector buttons (`Cloud`, `Gemini Free`, `OpenRouter`).
  - Added secure API key input with password mask toggle, clear button, and "Save Key" action.
  - Added live "Test Key" button providing instant green verification (`✓ Gemini Online!`).
  - Updated System Health Diagnostics to explicitly identify `Quota Depleted (HTTP 402)`.
- **Architectural & Philosophy Documentation**:
  - Created [`CONCEPT.md`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/CONCEPT.md) detailing the target personas, domain rationale, and technical decisions.
  - Created [`CHANGELOG.md`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/CHANGELOG.md).

---

## [3.4.0] - 2026-09-09
### Changed
- **Monochromatic OLED & Clean Minimal Redesign**:
  - Eliminated legacy neon colors (purple, magenta, cyan, coral) in favor of high-contrast monochromatic palettes.
  - **OLED Dark Mode**: Pitch black (`#000000`) background and deep obsidian surfaces (`#09090B`) to maximize battery efficiency on OLED displays.
  - **Clean Minimal Light Mode**: Pure porcelain white (`#FFFFFF`) with sharp charcoal/black typography for crystal-clear readability under direct outdoor sunlight.
  - **Champagne Beige Accents**: Curated warm beige (`#E8E3D5` in dark, `#C4BCAF` in light) for editorial elegance.
- **Shader Monochromatic Adaptation**:
  - Updated `ConstellationField.kt` to silver/white star nodes in dark mode and graphite nodes in light mode.
  - Updated `AmbientGlowBackground.kt` to soft monochromatic radiance pools.
  - Updated `ConcentricPulsingOrb.kt` and `VoiceWaveLineAnimation.kt` to monochromatic wave colors.
  - Updated `VoiceEdgeLighting.kt` to silver-white and obsidian linear bezel gradients.

---

## [3.3.0] - 2026-09-04
### Added
- **FastAPI Cloud Backend on Render**:
  - Deployed dedicated backend services at `https://weathergpt-backend-m5kk.onrender.com`.
  - Added `/api/ai/chat-stream` endpoint streaming Server-Sent Events (SSE).
- **Cryptographic HMAC-SHA256 Security** ([`HmacSigner.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/core/network/HmacSigner.kt)):
  - Generates dynamic timestamped HMAC-SHA256 headers (`X-Timestamp`, `X-Signature`, `X-WeatherGPT-Key`) preventing replay attacks and unauthorized scraping.
- **Multi-Sector Personalized Onboarding & Profiles**:
  - Added questionnaire for 6 sectors: Farmer, Commuter, Disaster Management, Aviation, Marine, Climate Analyst.
  - Dynamic profile storage in `UserPreferences.kt`.
  - OTP authentication screen with Supabase backend.

---

## [3.2.0] - 2026-09-02
### Added
- **Multilingual Neural Voice Engine**:
  - Added regional Indian voice profiles for 7 languages: English, Hindi (हिन्दी), Marathi (मराठी), Bengali (বাংলা), Tamil (தமிழ்), Telugu (తెలుగు), and Gujarati (ગુજરાતી).
  - Tuned speech cadence and female neural voices for natural, fluent regional speech.
  - Added live language switcher in Settings Sheet.
- **Dense Open-Meteo Meteorological Ingestion**:
  - Expanded `LiveWeatherData.kt` with agricultural metrics (surface soil moisture, root zone 1-9cm, soil temperature, evapotranspiration ET0, irrigation advice).
  - Added disaster indicators (river discharge, flood risk, heatwave alerts, 3-day rainfall patterns).
  - Added barometric pressure trends, dew point gap, and 24h/48h rain window analysis.

---

## [3.1.0] - 2026-08-28
### Added
- **Canvas & GPU Shader System**:
  - Built `ConstellationField.kt`: dynamic starfield simulation with distance-based link lines.
  - Built `AmbientGlowBackground.kt`: atmospheric radial gradient pools.
  - Built `VoiceEdgeLighting.kt`: dual-edge linear illumination expanding from bezel center during voice interaction.
  - Built `ConcentricPulsingOrb.kt`: voice orb with reactive concentric ripple halos.
  - Built `VoiceWaveLineAnimation.kt`: Siri-like multi-harmonic fluid liquid sine waves.

---

## [3.0.0] - 2026-08-20
### Changed
- **Unified Main Screen Architecture** ([`UnifiedMainScreen.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/assistant/ui/UnifiedMainScreen.kt)):
  - Replaced fragmented multi-tab screens with a single fluid, glassmorphic dashboard.
  - Added `BentoHubCards.kt` displaying live weather metrics, hourly forecast carousel, and agricultural pills.
  - Added `AnimatedChatCapsuleBar` with flowing liquid border and breathing mic orb.
- **SQLite Database Persistence** ([`ChatDatabaseHelper.kt`](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/assistant/data/ChatDatabaseHelper.kt)):
  - Offline local SQLite database tracking chat history, session IDs, and token usage metrics.
  - Added previous chat history sheet with search and delete capabilities.

---

## [2.8.0] - 2026-08-10
### Changed
- **Pure Open-Meteo Transition**:
  - Completely removed proprietary paid weather APIs in favor of 100% Open-Meteo.
  - Zero API keys required; 10,000 requests/day free tier.
  - Ingests multi-depth soil moisture, humidity, wind gusts, and precipitation probabilities.

---

## [1.5.0] - 2026-07-25
### Added
- **Sherpa-ONNX Integration**:
  - Integrated `com.microsoft.onnxruntime:onnxruntime-android:1.20.0`.
  - Added on-device streaming ASR (Speech-To-Text) and offline TTS (Text-To-Speech) pipeline.
  - Added Voice Activity Detection (VAD) energy monitoring.

---

## [1.4.0] - 2026-07-15
### Added
- **Location Engine (`domain/location`)**:
  - Added `LocationProvider`, `LocationCache`, and asynchronous reverse geocoding via Android native `Geocoder`.
  - Instant startup from cached GPS coordinates with background resolution.
- **Conversational Voice Mode**:
  - Interactive voice sphere with 4 states: `IDLE`, `LISTENING`, `THINKING`, `SPEAKING`.
  - Native Android TTS audio speech with tap-to-interrupt.

---

## [1.0.0] - 2026-07-01
### Added
- **Initial Setup & Domain-Driven Design (DDD)**:
  - Jetpack Compose with Material 3, Kotlin 2.0.21, AGP 9.3.2.
  - Modular package separation: `weather`, `news`, `voice`, `assistant`, `settings`, `notifications`, `profile`.
  - Top Dynamic Island header and floating bottom navigation bar.
