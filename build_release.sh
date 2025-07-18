#!/bin/bash

# Script to build release APK and AAB for Google Play Store
# Make sure you have generated the keystore first!

echo "Building HouseMeter for Google Play Store..."
echo "=========================================="

# Check if keystore exists
if [ ! -f "keystore.properties" ]; then
    echo "ERROR: keystore.properties not found!"
    echo "Please run ./generate_keystore.sh first and create keystore.properties"
    echo "You can copy keystore.properties.template to keystore.properties"
    exit 1
fi

# Check if keystore file exists
KEYSTORE_FILE=$(grep "storeFile" keystore.properties | cut -d'=' -f2)
if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "ERROR: Keystore file $KEYSTORE_FILE not found!"
    echo "Please run ./generate_keystore.sh first"
    exit 1
fi

echo "✓ Keystore configuration found"

# Clean project
echo "Cleaning project..."
./gradlew clean

# Build release AAB (recommended for Play Store)
echo "Building release AAB..."
./gradlew bundleRelease

# Build release APK (for testing)
echo "Building release APK..."
./gradlew assembleRelease

echo ""
echo "Build completed!"
echo "==============="
echo ""
echo "Files generated:"
echo "- AAB (for Play Store): app/build/outputs/bundle/release/app-release.aab"
echo "- APK (for testing): app/build/outputs/apk/release/app-release.apk"
echo ""
echo "Next steps:"
echo "1. Test the APK on a device"
echo "2. Upload the AAB to Google Play Console"
echo "3. Follow Play Store submission guidelines"
echo ""
echo "IMPORTANT: Keep your keystore file secure and backed up!"