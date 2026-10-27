# Building Lumina Browser

## Requirements
- Android Studio with Android/Gradle support.
- Android SDK components matching the project's Gradle configuration.
- A compatible Android emulator or Android device for running the app.

The app module currently declares `minSdk = 24`, `targetSdk = 36`, `versionCode = 1`, and `versionName = "1.0"` in `app/build.gradle.kts`. These values describe configuration only; they do not prove that a release build has been produced or tested.

## Open the project
1. Clone or download the repository.
2. Open the project root in Android Studio.
3. Allow Gradle sync to finish. Install any SDK components requested by Android Studio.
4. Review `.env.example` and the project's Firebase configuration before running AI-related functionality.
5. Run the app on an emulator or connected Android device.

## Gemini / Firebase configuration
The project uses Firebase AI Logic / Gemini according to its current project documentation. Follow the configuration expected by the repository's Gradle setup and Firebase project. Do not paste API keys into source files or commit private configuration.

## Build from Android Studio
Use Android Studio's Gradle sync and Build/Run controls first. If the project syncs successfully, the Gradle wrapper can also be used from the repository root:
- Linux/macOS: `./gradlew assembleDebug`
- Windows: `gradlew.bat assembleDebug`

These are standard Gradle commands, not a claim that a build was executed during documentation preparation. If the build fails, capture the first actionable error and your Gradle/JDK/Android SDK versions.

## Signing and secrets
- Keep keystores and signing passwords outside version control.
- Do not commit `.env`, service-account files, API keys, or debug tokens.
- Use debug builds for local testing. Treat release signing as a separate, deliberate process.
