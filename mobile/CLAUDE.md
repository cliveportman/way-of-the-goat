# KMP Setup Instructions

## Current State
- ✅ Android KMP target working
- ❌ iOS targets disabled due to Kotlin Native dependency issues

## Issue with iOS Support
The Kotlin Native compiler cannot find `kotlin-native-prebuilt-macos-aarch64` artifacts, likely due to:
1. **Xcode Configuration**: System pointing to `/Library/Developer/CommandLineTools` instead of full Xcode
2. **Repository Access**: Kotlin Native repositories not accessible
3. **Environment Setup**: Missing proper macOS development environment

## To Fix iOS Support (Requires Manual Steps)

### Step 1: Fix Xcode Configuration
```bash
# Check current path
xcode-select --print-path

# Should show: /Library/Developer/CommandLineTools
# Need to switch to full Xcode (requires password):
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
```

### Step 2: Enable iOS Targets
After fixing Xcode, edit `shared/build.gradle.kts`:
```kotlin
val enableIosTargets = true // Change from false to true
```

### Step 3: Test Build
1. **Restart IntelliJ IDEA**
2. **Sync Gradle project**
3. **Look for iOS run configurations**

## Build Commands
- **Android**: Use IntelliJ IDEA run configuration for `androidApp`
- **iOS**: Use IntelliJ IDEA run configuration for `iosApp` (when working)

## Architecture
- `shared/` - Common KMP code with Compose Multiplatform UI
- `androidApp/` - Android-specific app wrapper
- `iosApp/` - iOS-specific app wrapper (SwiftUI → Compose bridge)

## Troubleshooting iOS
If iOS still fails after Xcode fix:
1. Check Kotlin version compatibility with your macOS version
2. Try cleaning Gradle cache: `./gradlew clean`
3. Consider using older Kotlin version (1.9.22 → 1.9.10)
4. Verify network access to JetBrains repositories