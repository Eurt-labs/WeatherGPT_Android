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

### [Version 1.2.1] - Scroll-to-Fade Dynamic Island & Enhanced Multi-Layer Shadows

#### 1. Scroll-Aware Dynamic Island Animation
- Attached a global `NestedScrollConnection` in [MainActivity.kt](file:///c:/Users/Dhruv%20Saraswat/Documents/SIh/WeatherGPT_Android/app/src/main/java/com/example/weathergpt_android/MainActivity.kt) that monitors vertical scrolling across all screens.
- **Scroll Down**: Top Dynamic Island smoothly fades out and slides upwards (`slideOutVertically`) to maximize visible screen space for weather data, news, voice, and settings.
- **Scroll Up / Top**: Top Dynamic Island smoothly fades back in (`slideInVertically`).
- Resets visibility automatically upon tab switching or returning to the initial welcome screen.

#### 2. Enhanced Drop Shadows for Floating Islands
- **Floating Bottom Nav Bar**: Upgraded to `elevation = 16dp`, `spotColor = Color(0x45000000)`, `ambientColor = Color(0x30000000)`, and `tonalElevation = 4dp` for prominent depth.
- **Top Dynamic Island**: Upgraded to `elevation = 18dp / 14dp`, `spotColor = Color(0x45000000)`, `ambientColor = Color(0x30000000)`, and `tonalElevation = 4dp`.

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
