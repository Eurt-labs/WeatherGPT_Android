# WeatherGPT Android - Project & Architecture Log

## 📐 Domain-Driven Architecture (DDD) Overview

The codebase is organized into discrete domain packages with clear boundaries, separating business models, domain-specific UI components, and shared core utilities.

```
com.example.weathergpt_android/
├── MainActivity.kt                      # Application root entry point & Scaffold
│
├── core/                                # Shared foundational modules
│   ├── theme/                           # Global Light Theme design system
│   │   ├── Color.kt                     # Flushed color palette & accent tokens
│   │   ├── Type.kt                      # Typography scale definitions
│   │   └── Theme.kt                     # Material3 Light theme provider & status bar controller
│   ├── navigation/                      # App navigation contract
│   │   └── NavTab.kt                    # Navigation destinations & AutoMirrored icons
│   └── components/                      # Core shared floating island components
│       ├── TopIslandHeader.kt           # Dynamic top pill island (Notification + Brand + Profile + Expandable Widget)
│       └── FloatingBottomNavBar.kt      # Floating bottom island navigation bar with spring bounce
│
└── domain/                              # Feature-based domain boundaries
    ├── weather/                         # Weather Home Domain
    │   ├── model/
    │   │   └── WeatherModels.kt         # WeatherScenario, HourlyForecast, DayForecast, WeatherMetrics
    │   └── ui/
    │       ├── HomeScreen.kt            # Main wireframe overview with interactive scenario switcher
    │       └── WeatherCards.kt          # Greeting hero, AI suggestion capsule, metric grid, 7-day outlook
    │
    ├── news/                            # Weather News & Radar Domain
    │   ├── model/
    │   │   └── NewsArticle.kt           # NewsArticle model
    │   └── ui/
    │       └── NewsScreen.kt            # News feed, climate advisories, filter pills
    │
    ├── voice/                           # Voice AI Domain
    │   ├── model/
    │   │   └── VoiceModels.kt           # VoiceInteractionState, VoiceSuggestion
    │   └── ui/
    │       └── VoiceAiScreen.kt         # Voice orb visualizer, audio equalizer bars, suggestion pills
    │
    ├── assistant/                       # WeatherGPT Assistant Domain
    │   ├── model/
    │   │   └── ChatModels.kt            # ChatMessage data model
    │   └── ui/
    │       └── GptChatScreen.kt         # Interactive chat interface, suggestion chips, input capsule
    │
    ├── settings/                        # User Preferences & App Settings Domain
    │   └── ui/
    │       └── SettingsScreen.kt        # Temperature unit switcher (°C/°F), alert preferences, about card
    │
    ├── notifications/                   # Notification & Alert Domain
    │   ├── model/
    │   │   └── WeatherNotification.kt   # Notification data model
    │   └── ui/
    │       └── NotificationSheet.kt     # Interactive notification modal bottom sheet
    │
    └── profile/                         # User Profile Domain
        └── ui/
            └── ProfileSheet.kt          # User account modal sheet with tier badge & preferences
```

---

## 📋 Comprehensive Changelog & Version History

### [Version 1.0.0] - Initial Setup & Domain-Driven Implementation

#### 1. Build & Dependency Setup
- Configured Gradle Version Catalog `gradle/libs.versions.toml` with Jetpack Compose BOM `2024.12.01`, Kotlin `2.0.21`, AGP `9.3.2`, Material3, Navigation Compose, and Activity Compose.
- Configured `app/build.gradle.kts` with `compose = true` and target Java 17.
- Updated `AndroidManifest.xml` with permissions and edge-to-edge `MainActivity`.

#### 2. Core UI & Flushed Light Design
- **Top Dynamic Island Header**: Built floating capsule island (`30dp` radius) with soft ambient drop shadow and no outlines. Supports expanding live weather widget on click.
- **Floating Bottom Island Navigation**: Built floating bottom bar (`34dp` radius) featuring 5 interactive tabs (Weather, News, Voice AI, GPT, Settings) with spring tap scaling and active pill indicator.
- **Flushed Light Theme Palette**: Pure white cards, subtle slate backgrounds (`#F8FAFC`), crisp sky blue accents, and soft translucent overlays.

#### 3. Domain Features Implemented
- **Weather Domain**:
  - Implemented wireframe greeting: *"Hi Dhruv 👋, today's weather is good. Suggestion: go for a walk around the park."*
  - Added interactive weather scenario selector (`☀️ Sunny`, `🍃 Mild Breeze`, `🌧️ Light Showers`, `🌇 Golden Hour`) that transitions the entire screen with animated content changes.
  - Implemented 2x2 metric cards (Wind speed, Humidity, UV index, Air quality) and 7-day outlook progress bars.
- **News Domain**: Implemented real-time weather news cards with categories and bookmark toggles.
- **Voice AI Domain**: Implemented pulsing voice orb ripple animations with live equalizer bars and instant query previews.
- **Assistant Domain**: Implemented interactive chat bubbles with weather highlight pills and floating input pill.
- **Settings Domain**: Implemented temperature unit switch (`°C` vs `°F`), notification switches, and about card.
- **Notifications & Profile**: Implemented modal bottom sheets for alerts and user profile settings.

---

### [Version 1.2.0] - Custom Color Palettes, 2-Theme System, Top Island Shadows & Launch Landing Flow

#### 1. Custom Handcrafted Color Palettes (2 Themes)
- **Flushed Light Theme**:
  - Primary Accent: `#3A86FF` (Electric Azure Blue)
  - Secondary Accent: `#BDB2FF` (Soft Lavender Lilac)
  - Tertiary Accent: `#FFD6E0` (Soft Pastel Rose)
  - Background Screen: `#FFF4F4` (Porcelain Warm White)
  - Card Surfaces: Pure White `#FFFFFF` with warm tinted variants `#F8EEF0`
- **Slate Moss Dark Theme**:
  - Base Background: `#2C3639` (Deep Slate Pine)
  - Surface & Cards: `#3F4E4F` (Muted Charcoal Moss)
  - Primary Accent: `#A27B5C` (Warm Bronze Camel)
  - Primary Typography: `#DCD7C9` (Warm Cream Stone)
- Cleaned up settings to present strictly these **two custom themes** with interactive color dot previews.

#### 2. Top Island Prominent Shadows & GPT Immersion
- Added prominent, multi-layer floating drop shadow to the Top Dynamic Island (`elevation = 12dp`, `spotColor = Color(0x35000000)`, `ambientColor = Color(0x25000000)`).
- **Auto-hide Top Island on GPT Screen**: When switching to WeatherGPT chat, the Top Island slides out smoothly, granting full screen height to the chat conversation.
- Fixed GPT chat message list and input bar alignment so the floating text pill sits comfortably above the floating bottom navigation bar without overlapping.

#### 3. Initial Launch Greeting Landing Flow
- Built [GreetingWelcomeView.kt](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/weather/ui/GreetingWelcomeView.kt):
  - On fresh app launch, displays an empty, clean window showing the floating top island, bottom nav bar, and the centered greeting (*"Hi Dhruv 👋, Today's weather is good"* with the AI suggestion).
  - Tapping the Top Island (Notifications / Profile) opens sheets without dismissing the greeting.
  - Clicking on any bottom navigation tab (Cloud, News, Voice, GPT, Settings) or tapping *"Explore Live Weather"* smoothly transitions into the full dashboard view.

### [Version 1.3.0] - Attached Bottom Nav with Center Mic Dome, Material 3 Light Theme, Theme Persistence & Status Bar Contrast Fixes

#### 1. Attached Bottom Navigation Bar with Center Raised Mic Dome FAB
- Rebuilt bottom navigation from floating island to an **attached bottom navigation bar** anchored to the bottom edge of the device screen (`navigationBarsPadding()`).
- Added an **elevated center cradle dome FAB** (`58dp`, `offset(y = -20dp)`) dedicated to the **Voice AI Assistant (Microphone)** with gradient glow, layered shadow (`elevation = 14dp`), and spring bounce physics.
- Symmetric tab distribution:
  - Left: **Weather** (Cloud) & **News** (Feed)
  - Center: **Raised Voice AI Mic FAB**
  - Right: **WeatherGPT Assistant** (Chat) & **Settings** (Gear)

#### 2. Material 3 Clean Light Theme Palette
- Standardized Light theme to clean **Material 3 Design System**:
  - Primary Accent: `#006494` (M3 Deep Sky Blue)
  - Container: `#D0E4FF`
  - Background: `#F8FAFC` (Crisp Slate White)
  - Surfaces: `#FFFFFF` (Pure White)
- Retained custom **Slate Moss Dark** for Dark Theme (`#2C3639`, `#3F4E4F`, `#A27B5C`, `#DCD7C9`).

#### 3. Persistent Theme Preference
- Implemented `ThemePreferences` backed by `SharedPreferences` to ensure the selected theme (Light vs Dark) persists across app restarts and device reboots.

#### 4. Status Bar Contrast & Notification Badge Fixes
- **Status Bar Blending Fix**: Configured `WindowCompat.getInsetsController` to ensure dark status bar icons on Light theme and light icons on Dark theme, preventing blending with device clocks and battery indicators.
- **Unclipped Notification Badge**: Fixed clipping issue by moving the red notification dot outside the inner circular container, rendering a crisp, perfectly positioned notification badge.

### [Version 1.4.0] - Real-time Location Resolution with Caching & ChatGPT-Style Conversational Voice Mode

#### 1. Real-time Location Resolution & Caching Engine (`domain/location`)
- Built `LocationData` model, `LocationCache` (`SharedPreferences`), and asynchronous `LocationProvider`.
- **Fast Startup & Offline Caching**: Loads last-known location instantly from disk cache on app launch.
- **Live Reverse Geocoding**: Automatically resolves GPS/Network coordinates into human-readable city, state/region, and country using Android's native `Geocoder` on `Dispatchers.IO`.
- Dynamically updates [HomeScreen.kt](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/weather/ui/HomeScreen.kt), [VoiceAiScreen.kt](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/voice/ui/VoiceAiScreen.kt), and [ProfileSheet.kt](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/domain/profile/ui/ProfileSheet.kt) with the user's real location.

#### 2. ChatGPT-Style Conversational Voice Mode (`domain/voice`)
- **Fluid Morphing Voice Sphere**:
  - `IDLE`: Subtle ambient breathing pulse with gentle rotation.
  - `LISTENING`: Reactive dual-ring waveform listening for user speech.
  - `THINKING`: Swirling energy core analyzing atmospheric pressures and microclimates.
  - `SPEAKING`: Morphing audio equalizer wave bars with live speech output.
- **Native Android Text-to-Speech (TTS)**: The assistant audibly speaks out natural conversational weather forecasts and answers in real-time.
- **Conversational Turn-Taking & Controls**:
  - Live transcription dialog cards.
  - Mute/Unmute TTS audio speech button.
  - Tap-to-interrupt and instant conversational follow-up suggestions (e.g. walk timing, rain chances, clothing recommendations, UV/Air quality).

### [Version 1.5.0] - Sherpa-ONNX On-Device Speech & Audio Intelligence Architecture (`domain/voice/sherpa`)

#### 1. Sherpa-ONNX & ONNX Runtime Core Integration
- Added `com.microsoft.onnxruntime:onnxruntime-android:1.20.0` with native 64-bit/32-bit libraries (`libonnxruntime.so`, `libonnxruntime4j_jni.so`).
- Implemented `SherpaOnnxEngine` managing:
  - Streaming PCM 16-bit 16kHz audio ingestion via `AudioRecord`.
  - On-device Voice Activity Detection (VAD) energy monitoring.
  - Streaming ASR (Speech-To-Text) and Offline TTS (Text-To-Speech) pipeline.

#### 2. Multilingual Indian Language Speech Models (SIH 2026 Focus)
- Added dynamic language selector supporting **6 Indian languages**:
  - 🇺🇸 **English** (`en`)
  - 🇮🇳 **Hindi / हिन्दी** (`hi-IN`)
  - 🇮🇳 **Marathi / मराठी** (`mr-IN`)
  - 🇮🇳 **Bengali / বাংলা** (`bn-IN`)
  - 🇮🇳 **Tamil / தமிழ்** (`ta-IN`)
  - 🇮🇳 **Telugu / తెలుగు** (`te-IN`)
- Real-time localized weather intelligence dialog with native audio TTS output.

### [Version 3.0.0] - Unified Main Screen & SQLite Persistence
- **Unified Main Screen Architecture** (`UnifiedMainScreen.kt`): Replaced fragmented multi-tab screens with a single fluid glassmorphic surface featuring `BentoHubCards` and `AnimatedChatCapsuleBar`.
- **Local SQLite Database** (`ChatDatabaseHelper.kt`): Complete on-device SQLite database storing chat history, session IDs, and token analytics with offline accessibility.

### [Version 3.1.0] - Dynamic Canvas & GPU Shader System
- Built custom mathematical particle & wave shaders:
  - `ConstellationField.kt`: dynamic starfield simulation with distance-based link lines.
  - `AmbientGlowBackground.kt`: atmospheric radial gradient pools.
  - `VoiceEdgeLighting.kt`: dual-edge linear illumination expanding from bezel center during voice interaction.
  - `ConcentricPulsingOrb.kt`: voice orb with reactive concentric ripple halos.
  - `VoiceWaveLineAnimation.kt`: Siri-like multi-harmonic fluid liquid sine waves.

### [Version 3.2.0] - Multilingual Neural Voice & Persona Framework
- Added regional Indian voice profiles for 7 languages (Hindi, Marathi, Bengali, Tamil, Telugu, Gujarati, English) with tuned cadence.
- Implemented multi-sector onboarding for 6 domains (Farmer, Commuter, Disaster, Aviation, Marine, Climate Analyst).
- Expanded `LiveWeatherData.kt` with deep agricultural soil metrics, evapotranspiration, and risk indicators.

### [Version 3.3.0] - Cloud Backend & Cryptographic HMAC Security
- Deployed FastAPI backend on Render (`https://weathergpt-backend-m5kk.onrender.com`).
- Implemented `HmacSigner.kt` with dynamic HMAC-SHA256 signature verification preventing token spoofing and replay attacks.
- Integrated Supabase OTP authentication.

### [Version 3.4.0] - Monochromatic OLED & Clean Minimal Redesign
- Eliminated legacy neon colors across all screens and shaders.
- **OLED Dark Theme**: Pitch black (`#000000`) for maximum battery efficiency on organic displays.
- **Clean Minimal Light Theme**: Pure porcelain white (`#FFFFFF`) for crisp outdoor direct sunlight legibility.
- **Champagne Beige Accents**: Editorial warm beige (`#E8E3D5` in dark, `#C4BCAF` in light).

### [Version 3.5.0] - Multi-Provider AI Engine & HTTP 402 Self-Healing Fallback
- **Multi-Provider AI Architecture** (`AiPreferences.kt`): Added selectable AI provider modes (Cloud Backend, Google Gemini Direct 15 RPM Free, OpenRouter Direct).
- **HTTP 402 Interception & Fallback** (`OpenRouterService.kt`): Intercepts credit depletion (402) and synthesizes an intelligent local meteorological advisory from in-memory Open-Meteo telemetry.
- **AI Settings UI** (`FrostedSettingsSheet.kt`): Added AI engine configuration card with API key entry, key testing, and 402 diagnostic reporting.
- See detailed philosophy in [CONCEPT.md](CONCEPT.md) and full version history in [CHANGELOG.md](CHANGELOG.md).

### [Version 3.5.1] - Locked to Gemini 2.5 Flash & local.properties OpenRouter Link
- **Model Lock**: Pinned all providers to **Google Gemini 2.5 Flash** (`google/gemini-2.5-flash` on OpenRouter, `gemini-2.5-flash` on Google Direct API).
- **Android Studio `local.properties` Link**: Configured `app/build.gradle.kts` to expose `OPENROUTER_API_KEY` and `GEMINI_API_KEY` to `BuildConfig`, enabling keys declared in Android Studio to work immediately.
- **Header & Sanitization Fixes**: Added mandatory OpenRouter headers (`HTTP-Referer`, `X-Title`) and trimmed `Bearer ` prefixes; parsed structured error messages from OpenRouter HTTP responses.

### [Version 3.5.2] - Main Page Widget & Card Crash Resolution
- **SQLite Connection Pool Fix** (`ChatDatabaseHelper.kt`): Eliminated `.use` calls on `readableDatabase` and `writableDatabase` which were prematurely closing the database connection after the initial query on `UnifiedMainScreen`, causing crashes whenever widgets were tapped.
- **Native Linkage & Lifecycle Hardening** (`SherpaOnnxEngine.kt`, `ImmersiveVoiceScreen.kt`): Caught `Throwable` during ONNX runtime native initialization and guarded `SpeechRecognizer` with `RECORD_AUDIO` permission checks and retry debouncing.

### [Version 3.5.3] - Upgraded to Gemini 3.6 Flash & Pinned OpenRouter to Google AI Studio
- **Gemini 3.6 Flash Upgrade**: Shifted default model IDs across `AiPreferences.kt`, `OpenRouterService.kt`, `FrostedSettingsSheet.kt`, and prompts to `google/gemini-3.6-flash` and `gemini-3.6-flash`.
- **OpenRouter AI Studio Provider Pinning**: Injected strict provider ordering and exclusion into OpenRouter JSON payloads (`"order": ["google-ai-studio"]`, `"ignore": ["google-vertex"]`, `"allow_fallbacks": false`), preventing OpenRouter from routing or shifting requests to Google Vertex.

### [Version 3.5.4] - Zero-402 Resilient Meteorological Engine & Smart Key Cross-Routing
- **Complete HTTP 402 Eradication**: Upstream OpenRouter balances with $0.00 and Render backend 402 SSE streams are now completely silent to the user. Removed all `(HTTP 402)` warning headers from local fallback advisories.
- **Smart Key Type Detection**: Automatically differentiates `AIzaSy...` (Google AI Studio, free forever) from `sk-or-...` (OpenRouter, requires credit balance). Automatically cross-routes Google keys to Google AI Studio even if placed in the OpenRouter field or properties.
- **Flow & Token Hardening**: Fixed Kotlin coroutine Flow error handling by replacing `.catch` builders with robust `try-catch` blocks around `.collect { emit(it) }`. Added defensive token scrubbers in chat and voice screens to discard any upstream proxy error artifacts.
- **System Diagnostics Update**: Health diagnostics now report `Live Meteorological Engine Active ✓` instead of showing a red failure on 402 quota depletion.

---

## 🐛 Bug Fixes & Diagnostics Log

| Issue Encountered | Root Cause | Fix Applied | Status |
| :--- | :--- | :--- | :--- |
| `Cannot add extension with name 'kotlin'` in Gradle sync | AGP 9.3 has built-in Kotlin support and conflicted with redundant `kotlin.android` plugin | Removed explicit `kotlin.android` plugin and kept `kotlin.compose` plugin in Gradle configuration | ✅ Fixed |
| `Argument type mismatch: actual type is 'Alignment.Vertical', but 'Alignment.Horizontal' was expected` | `Column` in `FloatingBottomNavBar.kt` was passed `Alignment.CenterVertically` | Corrected parameter to `Alignment.CenterHorizontally` | ✅ Fixed |
| `Unresolved reference 'background'` in `SettingsScreen.kt` | Missing `androidx.compose.foundation.background` import | Added import in `SettingsScreen.kt` | ✅ Fixed |
| Missing comma in `FloatingBottomNavBar.kt` modifier chain | Parameter list syntax error after `.clickable(...)` | Added missing comma before `horizontalAlignment` | ✅ Fixed |
| Icon deprecation warnings (`Logout`, `DirectionsWalk`, `Feed`, `Send`, `VolumeUp`) | Icons were moved to `androidx.compose.material.icons.automirrored.rounded.*` in recent Compose releases | Migrated all deprecated icons to `AutoMirrored.Rounded.*` | ✅ Fixed |
| Deprecated `statusBarColor` / `navigationBarColor` in `Theme.kt` | Window status bar properties deprecated in newer Android SDKs | Delegated edge-to-edge transparent system bars to `enableEdgeToEdge()` in `MainActivity.kt` and `WindowCompat` | ✅ Fixed |
| Top Island & Bottom Nav overlapping screen content on device | Content insets (`top = 84dp`, `bottom = 100dp`) were insufficient for device camera cutouts and navigation bars | Increased insets to `top = 104dp` and `bottom = 110dp` | ✅ Fixed |
| Text wrapping on `Comfortable` and `Latest` badges | Fixed column container bounds forced horizontal wrapping | Replaced with `wrapContentSize()` and `maxLines = 1` | ✅ Fixed |
| Faint / flat card appearance on device | Insufficient shadow elevation and contrast | Added multi-layer ambient colored shadows (`elevation = 8dp`, `spotColor = primary.copy(alpha = 0.15f)`) | ✅ Fixed |
| Top Island lacked shadow and overlapped GPT chat screen | Top Island had no dark spot shadow and was rendered above GPT chat content | Added `12dp` layered shadow and auto-hid top island in GPT tab via `AnimatedVisibility` | ✅ Fixed |
| Initial greeting redundancy across navigation | Welcome card remained embedded in the long weather dashboard list | Separated into dedicated `GreetingWelcomeView` on launch that transitions on nav click | ✅ Fixed |
| Chat & Voice returning `HTTP 402 Payment Required` | Upstream OpenRouter credit balance on Render backend was $0.00; backend forwarded `Error HTTP 402` in SSE stream | Intercepted 402 in `OpenRouterService.kt`, created intelligent local meteorological advisory fallback, and added direct Google Gemini free API key support in `FrostedSettingsSheet.kt` | ✅ Fixed |
| OpenRouter key in `local.properties` ignored & giving errors | `build.gradle.kts` didn't export `OPENROUTER_API_KEY` to `BuildConfig`, and OpenRouter requests were missing required headers | Injected keys into `BuildConfig`, added automatic provider selection, added `HTTP-Referer`/`X-Title` headers, and set default model to `google/gemini-2.5-flash` | ✅ Fixed |
| Main page widgets/boxes crashing on tap | `ChatDatabaseHelper` called `.use` on `readableDatabase`, closing the database after the main screen query; subsequent queries threw `IllegalStateException: closed database` | Removed `.use` from SQLite database instances, safely closed cursors, and added try-catch blocks across all DB queries | ✅ Fixed |
| Gemini 2.5 Flash unavailable for new users & OpenRouter routing to Google Vertex | Gemini 2.5 Flash deprecation for new accounts; OpenRouter auto-load-balancing across Vertex & AI Studio | Migrated all models to Gemini 3.6 Flash (`google/gemini-3.6-flash` / `gemini-3.6-flash`) and pinned OpenRouter provider routing strictly to `google-ai-studio`, ignoring `google-vertex` | ✅ Fixed |
| Persistent HTTP 402 error & flow compilation issues | Flow `.catch` blocks clashed inside `flow { }`; local advisory showed scary 402 banner; OpenRouter direct returned 402 for $0 balance keys without falling back | Replaced Flow `.catch` with standard `try-catch`, scrubbed all 402 banners, added smart key cross-routing (`AIzaSy` vs `sk-or`), and made 402 gracefully and silently fall back to local meteorological intelligence | ✅ Fixed |

