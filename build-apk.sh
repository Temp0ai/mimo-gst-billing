#!/bin/bash

echo "========================================"
echo "  Mimo GST Billing - Build Script"
echo "========================================"
echo ""

# Check if Android SDK is set
if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
    echo "Error: ANDROID_SDK_ROOT or ANDROID_HOME not set!"
    echo "Please set your Android SDK path:"
    echo "  export ANDROID_SDK_ROOT=/path/to/android/sdk"
    exit 1
fi

# Run Gradle build
echo "Starting build..."
./gradlew clean assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on device:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
else
    echo "Build failed! Check errors above."
    exit 1
fi

echo ""
echo "========================================"
echo "  Alternative: Open in Android Studio"
echo "  File -> Open -> Select this folder"
echo "  Build -> Build Bundle/APK -> APK"
echo "========================================"
