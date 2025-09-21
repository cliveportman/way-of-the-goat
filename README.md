# Way of the Goat

A multi-platform nutrition tracking application that helps endurance athletes monitor their daily food intake and scoring.

## Project Structure

This repository contains multiple implementations of the same core application:

```
way-of-the-goat/
├── mobile/           # Native mobile apps
│   ├── androidApp/   # Native Android app (Jetpack Compose)
│   └── iosApp/       # Native iOS app (SwiftUI - future)
├── api/              # Go backend API (future)
└── global-assets/    # Shared assets across platforms
```

## About the App

The scoring methodology comes from "Racing Weight" by Matt Fitzgerald, designed for serious endurance athletes managing weight for performance. This implementation offers:

- **Local-first approach**: No account required - data stays on your device
- **Improved UX**: Clean, intuitive interface optimized for daily use
- **Customized scoring**: Refined food group scoring based on practical usage
- **Multi-platform**: Native performance on Android and iOS

## Technology Stack

### Native Mobile Apps (Current Focus)
![kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![ios](https://img.shields.io/badge/iOS-000000?style=flat&logo=ios&logoColor=white)
![jetpack-compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![swiftui](https://img.shields.io/badge/SwiftUI-FA7343?style=flat&logo=swift&logoColor=white)

## Getting Started

### Prerequisites
- IntelliJ IDEA (Community Edition works fine)
- Android SDK
- Java 17+ (Azul Zulu recommended)

### Running the Android App
1. Open the `mobile/` directory in IntelliJ IDEA
2. Let Gradle sync complete
3. Create "Android App" run configuration
4. Run on emulator or connected device

## User interface

### UI library
I'm leaning on Tailwind UI as I love it and happen to have a commercial licence from years ago. You can't just copy the components into the React Native, though, so I've started building my own UI library as I go.

### Typeface
The typeface is [Inter](https://rsms.me/inter/), a font freely available on Google Fonts. Why this font?
- It has elements of Helvetica in it, which I love.
- It's used for NASA instrumentation and computer interfaces, which is cool.
- It's used by Tailwind UI, so I know it won't look out of place with the other styles.
- The smaller "text" designs (e.g. light, regular, medium and semi-bold) aid legibility of the lower-case text, so use them for text and UI-work.
- The larger "display" versions (e.g. bold, black) are designed for headings and titles. 

I've configured it for Tailwind/Nativewind usage, accessible via `font-light`, `font-regular`, `font-medium`, `font-semibold`, and `font-bold`. 