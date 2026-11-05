# PureCanvas

PureCanvas is an offline-first Android image enhancement studio built with Kotlin, Jetpack Compose and Material 3. Minimum Android version: 8.0 (API 26).

## Features
- Android Photo Picker via `GetContent` (no broad storage permission).
- JPG/JPEG, PNG and WebP decoding through Android Bitmap APIs.
- EXIF orientation correction and bounded decode size for large images.
- On-device denoising, tone/color controls, restrained unsharp-mask detail enhancement.
- JPEG/PNG export to `Pictures/PureCanvas` using MediaStore.
- No network permission, server, upload, watermark/provenance detection, or provenance manipulation.

## Build
1. Install Android Studio (stable) and Android SDK Platform 35 (or update `compileSdk` and `targetSdk` to the installed stable platform).
2. Open this folder in Android Studio and allow Gradle sync.
3. Use JDK 17 for Gradle.
4. Run the `app` configuration on an Android 8.0+ device/emulator.
5. Build debug APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

CLI (after installing Gradle 8.9 and Android SDK):
```bash
gradle :app:assembleDebug
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Signed release APK
In Android Studio choose **Build > Generate Signed Bundle / APK > APK**, create/select a keystore, choose the `release` variant, and finish. Keep the keystore and passwords private and backed up. For repeatable CI, configure signing credentials through environment variables or an untracked `keystore.properties`; never commit secrets.

## Architecture
- `ui/`: Compose screen and ViewModel state.
- `domain/`: processing settings model.
- `engine/`: background bitmap enhancement pipeline.
- `data/`: image decode/import and MediaStore export.

## Image quality and limitations
The enhancement pipeline is deliberately conservative. Denoising is neighborhood averaging, while detail enhancement is a restrained unsharp mask; it does not claim AI super-resolution or professional JPEG deblocking. Large images above 24 MP are downscaled for processing to reduce out-of-memory risk. The original image remains untouched. Export writes a new file; source EXIF/provenance metadata is not currently copied into the exported bitmap. No provenance identifier is intentionally searched for or altered. Validate behavior and image quality on representative devices and source images before production distribution.

## Tests
Run `gradle :app:testDebugUnitTest` and `gradle :app:connectedDebugAndroidTest` after installing the SDK and a test device. Physical-device verification is required for final release sign-off.
