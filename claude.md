# Way of the Goat - Project Context

A multi-platform nutrition tracking application that helps endurance athletes monitor their daily food intake and scoring based on the Racing Weight methodology by Matt Fitzgerald.

## Project Overview

**Purpose:** Nutrition tracking app for endurance athletes managing weight for performance

**Key Features:**
- Local-first approach: No account required - data stays on your device
- Daily food quality scoring system
- Activity tracking integration with intervals.icu
- Multi-platform: Native performance on Android and iOS

**Target Audience:** Serious endurance athletes (runners, cyclists, triathletes)

### Food Scoring System

The app implements a daily food quality scoring system based on Racing Weight:
- Athletes track what they eat across multiple food categories
- Each category has a target number of servings
- Points are awarded based on meeting daily targets
- Higher scores indicate better nutrition quality
- Full algorithm details available in `references/core/` for migration reference

## Repository Structure

This is a monorepo containing:

```
way-of-the-goat/
├── mobile/              # Kotlin Multiplatform mobile app (ACTIVE)
│   ├── composeApp/      # Shared KMP code
│   ├── iosApp/          # iOS wrapper (SwiftUI)
│   └── androidApp/      # Android target
├── references/          # Legacy React Native v1.4.3 (REFERENCE ONLY)
├── api/                 # Go backend API (PLANNED)
├── website/             # Website (PLANNED)
└── global-assets/       # Shared assets
```

**Migration Status:** Actively migrating from React Native (Expo) to Kotlin Multiplatform. The `references/` directory contains the production v1.4.3 codebase preserved for business logic reference during migration.

## Mobile App (Kotlin Multiplatform)

### Tech Stack

- **Language:** Kotlin Multiplatform
- **UI Framework:** Compose Multiplatform (Jetpack Compose for cross-platform)
- **Design System:** Material Design 3
- **Networking:** Ktor Client (OkHttp on Android, Darwin on iOS)
- **State Management:** ViewModels + StateFlow
- **Navigation:** Navigation Compose
- **Serialization:** kotlinx.serialization
- **Date/Time:** kotlinx.datetime

### Architecture

**Pattern:** MVVM (Model-View-ViewModel) with Repository pattern

**Module Structure:**
- Single `composeApp` module with platform-specific source sets
- `commonMain/` - Shared code (business logic, UI, data layer)
- `androidMain/` - Android-specific implementations
- `iosMain/` - iOS-specific implementations

**Data Flow:**
```
UI (Composables)
  ↓
ViewModels (StateFlow)
  ↓
Repository (business logic)
  ↓
API Client / Cache / Local DB
```

### Source Code Structure

```
mobile/composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/
├── App.kt                    # Main app entry point, navigation scaffold
├── Screen.kt                 # Navigation route definitions
├── screens/                  # UI screens and ViewModels
│   ├── HomeScreen.kt         # Splash/onboarding screen
│   ├── ScoresScreen.kt       # Daily food tracking (IN PROGRESS)
│   ├── ScoresViewModel.kt
│   ├── ProgressScreen.kt     # Weekly view, historical data
│   ├── ProgressViewModel.kt
│   ├── ActivityScreen.kt     # intervals.icu integration
│   ├── ActivityViewModel.kt
│   └── HelpScreen.kt         # User guide
├── ui/
│   ├── theme/
│   │   ├── Theme.kt          # Material3 theme configuration
│   │   └── Typography.kt     # Inter font definitions
│   └── icons/
│       └── GoatMoon.kt       # Custom app icon
└── data/
    ├── remote/               # API communication
    │   ├── HttpClientFactory.kt
    │   ├── IntervalsApiClient.kt
    │   └── models/           # Data transfer objects
    │       ├── Activity.kt
    │       └── WellnessData.kt
    ├── repository/           # Business logic layer
    │   └── IntervalsRepository.kt
    ├── cache/                # Local state management
    │   └── ActivityDataManager.kt
    └── auth/                 # Authentication
        ├── IntervalsAuthProvider.kt
        └── ApiKeyAuthProvider.kt
```

### Key Screens

1. **Home** (`HomeScreen.kt`) - Splash screen and onboarding
2. **Scores/Today** (`ScoresScreen.kt`) - Daily food tracking interface (currently in development)
3. **Progress** (`ProgressScreen.kt`) - Weekly chart, historical trends
4. **Activity** (`ActivityScreen.kt`) - Training activities from intervals.icu
5. **Help** (`HelpScreen.kt`) - User guide and documentation

**Navigation:** Bottom navigation bar with 4 tabs (Scores, Activity, Progress, Help)

### Data Layer Pattern

**Repository Pattern:**
- `Repository` classes handle business logic and API coordination
- Return `Result<T>` wrapper for error handling
- Abstract platform-specific implementations

**Caching:**
- `ActivityDataManager` provides StateFlow-based caching
- Reduces redundant API calls
- Manages data freshness

**Example:**
```kotlin
// Repository
class IntervalsRepository {
    suspend fun getActivities(oldest: String, newest: String): Result<List<Activity>>
}

// ViewModel
class ActivityViewModel : ViewModel() {
    val activities = MutableStateFlow<List<Activity>>(emptyList())
}

// UI
@Composable
fun ActivityScreen(viewModel: ActivityViewModel) {
    val activities by viewModel.activities.collectAsState()
}
```

## Design System

### Typography
- **Font Family:** Inter (from Google Fonts)
- **Usage:** Inspired by Tailwind UI patterns
- **Variants:** Light, Regular, Medium, SemiBold, Bold
- **Rationale:** NASA-grade legibility, Helvetica influence, excellent web/native compatibility

### Theme
- Material Design 3 components
- Custom color scheme
- Dark mode support (planned)

### Icons
- Material Icons Extended
- Custom `GoatMoon` icon for branding

## Development Setup

### Prerequisites
- **IDE:** IntelliJ IDEA (Community or Ultimate) or Android Studio
- **Android SDK:** compileSdk 34, minSdk 24
- **Java:** 17+ (Azul Zulu recommended)
- **Xcode:** Latest version (for iOS development)

### Building the App

**Android:**
```bash
cd mobile
./gradlew :composeApp:assembleDebug
```

**iOS:**
```bash
cd mobile/iosApp
open iosApp.xcodeproj
# Build and run from Xcode
```

Or use IDE run configurations for both platforms.

## External Integrations

### intervals.icu API

**Purpose:** Activity tracking and wellness data for endurance athletes

**Base URL:** `https://intervals.icu/api/v1`

**Authentication:**
- Basic Auth with username `API_KEY`
- API key as password
- Format: `Authorization: Basic base64(API_KEY:your_key)`

**Official Documentation:** https://intervals.icu/api/v1/docs/swagger-ui/index.html

#### Endpoints in Use

**1. Get Wellness Data**
```
GET /athlete/0/wellness?oldest={YYYY-MM-DD}&newest={YYYY-MM-DD}
```
Returns list of wellness metrics:
- Weight, resting heart rate, HRV
- Sleep duration and quality
- Mood, stress, fatigue, soreness ratings
- Training load metrics (CTL, ATL, ramp rate)

Response model: `WellnessData` (`data/remote/models/WellnessData.kt`)

**2. Get Activities**
```
GET /athlete/0/activities?oldest={YYYY-MM-DD}&newest={YYYY-MM-DD}
```
Returns list of training activities:
- Activity type (Run, Ride, Swim, etc.)
- Distance, duration, elevation
- Power metrics (watts)
- Heart rate data
- Perceived exertion and feel ratings

Response model: `Activity` (`data/remote/models/Activity.kt`)

**3. Get Single Activity**
```
GET /activity/{activityId}
```
Returns detailed data for a specific activity.

Response model: `Activity`

**Note:** Athlete ID `0` is a special value representing the currently authenticated user.

#### Implementation Files
- `data/remote/IntervalsApiClient.kt` - API client with Ktor
- `data/remote/models/Activity.kt` - Activity data model
- `data/remote/models/WellnessData.kt` - Wellness data model
- `data/remote/HttpClientFactory.kt` - Ktor HTTP client setup
- `data/auth/ApiKeyAuthProvider.kt` - Authentication (⚠️ currently contains hardcoded API key for development - will be replaced)
- `data/repository/IntervalsRepository.kt` - Business logic wrapper

## References Directory (Legacy React Native)

**Purpose:** Production v1.4.3 codebase preserved as reference during KMP migration

**Status:** Not actively maintained - use as reference only

**Contents:**
- Complete food scoring algorithm implementation
- Business logic patterns and data models
- SQLite database schema
- UI component patterns
- React Native + Expo + NativeWind stack

**Location:** `references/`

### Migration Guidance

When migrating features from `references/` to the KMP codebase:

#### 1. Locate the Feature in References

- `references/app/` - Screen components using Expo Router
- `references/core/` - Business logic, database operations, constants
- `references/features/` - Feature-specific UI components

#### 2. Extract Business Logic First

Look in `references/core/` for:
- Algorithms and calculations
- Data models and types
- Database operations
- Helper/utility functions

**Pattern Mappings:**
- TypeScript interfaces → Kotlin data classes
- Helper functions → Kotlin extension functions or utility objects
- Enums → Kotlin sealed classes or enum classes
- Constants → Kotlin companion objects

#### 3. Adapt UI Patterns

**React Native → Compose Multiplatform:**
- `<View>` → `Box`, `Column`, `Row`
- `<Text>` → `Text`
- `<ScrollView>` → `LazyColumn`, `LazyRow`
- `<FlatList>` → `LazyColumn` with items
- NativeWind classes (`className="..."`) → Modifier chains
- Tailwind utilities → Material3 theme + Modifier methods

**Navigation:**
- Expo Router file structure → Navigation Compose
- `useRouter()` → `NavController`
- Route parameters → navigation arguments

**Custom Components:**
- `references/core/components/` (TwButton, TwText, etc.) → Material3 equivalents

#### 4. Database Migration

- SQLite schema in `references/core/database.ts`
- Plan to use **Room** (Android-first) or **SQLDelight** (KMP-native)
- Preserve table structures and relationships
- Maintain backward compatibility with existing data

#### 5. Key Differences to Account For

**State Management:**
- React hooks (`useState`, `useEffect`) → ViewModels with StateFlow/MutableStateFlow
- `useEffect` → `LaunchedEffect`, `DisposableEffect`
- Context API → Dependency injection (manual or Koin)

**Side Effects:**
- `useEffect(() => { ... }, [deps])` → `LaunchedEffect(keys) { ... }`
- Cleanup functions → `DisposableEffect`

**Platform APIs:**
- Expo modules → `expect`/`actual` declarations
- Platform-specific code → `androidMain`/`iosMain` source sets

**Styling:**
- Tailwind CSS classes → Compose Modifiers
- `className="flex-1 bg-white p-4"` → `Modifier.fillMaxSize().background(Color.White).padding(16.dp)`

## Future Projects

### API (Planned - Go)

**Status:** Placeholder directory

**Expected Responsibilities:**
- User authentication and account management
- Data sync across devices
- Backend services for future features
- Potential integration hub for third-party services

### Website (Planned)

**Status:** Single placeholder HTML file

**Potential Use Cases:**
- Marketing and landing page
- Documentation portal
- Web-based companion app (view-only)

## Key Conventions

### Package Structure
```
co.theportman.way_of_the_goat
├── screens              # UI screens and ViewModels
├── ui.theme             # Design system
├── data.remote          # API clients and models
├── data.repository      # Business logic
├── data.cache           # Local data management
└── data.auth            # Authentication
```

### Naming Conventions
- Screens: `*Screen.kt` (e.g., `ActivityScreen.kt`)
- ViewModels: `*ViewModel.kt` (e.g., `ActivityViewModel.kt`)
- Composables: PascalCase functions
- Data models: Simple nouns (e.g., `Activity`, `WellnessData`)

### Navigation
- Sealed class `Screen` defines all routes
- Routes use lowercase with underscores (e.g., `"activity"`, `"scores"`)
- Navigation arguments passed via state hoisting

### State Management
- ViewModels for screen-level state
- StateFlow for reactive data streams
- State hoisting for component reusability
- Avoid global state - prefer scoped state

### Error Handling
- Repository methods return `Result<T>`
- ViewModels handle success/failure cases
- UI displays error states using Compose state

## Important Files

### Configuration
- `mobile/composeApp/build.gradle.kts` - Dependencies, build configuration, KMP setup
- `mobile/gradle.properties` - Gradle and Kotlin settings
- `mobile/settings.gradle.kts` - Project modules

### Core Application
- `mobile/composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/App.kt` - Main app structure, navigation scaffold
- `mobile/composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/Screen.kt` - Navigation route definitions

### Design System
- `mobile/composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/ui/theme/Theme.kt` - Material3 theme configuration
- `mobile/composeApp/src/commonMain/kotlin/co/theportman/way_of_the_goat/ui/theme/Typography.kt` - Font definitions

### Platform Entry Points
- `mobile/composeApp/src/androidMain/kotlin/co/theportman/way_of_the_goat/MainActivity.kt` - Android entry point
- `mobile/iosApp/iosApp/iOSApp.swift` - iOS entry point

## Work In Progress

Current development priorities:

1. **Food scoring system implementation**
   - Migrating algorithm from `references/core/`
   - Building UI for daily food tracking
   - Local database setup pending

2. **Local database (SQLite)**
   - Evaluating Room vs SQLDelight
   - Schema design based on `references/core/database.ts`
   - Data persistence for food logs

3. **Data sync**
   - Potential sync between local food scores and intervals.icu wellness data
   - Offline-first architecture

## Testing

**Current State:** Test infrastructure in place, minimal coverage

**Structure:**
- `commonTest/` - Shared unit tests
- `androidTest/` - Android instrumentation tests (future)
- `iosTest/` - iOS-specific tests (future)

**Testing Libraries:**
- kotlin-test for assertions
- (More to be added as testing strategy evolves)

---

**Last Updated:** 2025-12-02
**Current Branch:** `kmp-splash-screen`
**Main Branch:** `main`
