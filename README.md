# Matematik åk4

Android practice app for Swedish year 4. It has 400 mathematics questions, 100 Swedish questions, and 100 science (NO) questions. Every question has an answer key.

Practice can be a short set, a whole subject, or one category. Statistics stay on the phone: how many times the app was opened each week, time in the app, practice time per subject, and how many answers were correct.

## Build

Requires JDK 17 and the Android SDK. Create `local.properties` with `sdk.dir` pointing at the SDK, then:

```bat
gradlew.bat assembleDebug
```

The debug APK is written to `build\outputs\apk\debug\`. Version 1.0.
