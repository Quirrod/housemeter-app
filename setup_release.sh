#!/bin/bash

# Complete setup script for Google Play Store release
# This script will guide you through the entire process

echo "🚀 HouseMeter - Google Play Store Release Setup"
echo "=============================================="

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

echo ""
echo "📋 Step 1: Generate Release Keystore"
echo "====================================="

if [ -f "release-key.keystore" ]; then
    echo "✅ Keystore already exists"
else
    echo "🔑 Generating new keystore..."
    read -p "Do you want to generate a new keystore? (y/n): " generate_keystore
    
    if [ "$generate_keystore" = "y" ]; then
        ./generate_keystore.sh
    else
        echo "⚠️  Skipping keystore generation"
    fi
fi

echo ""
echo "📋 Step 2: Create Keystore Properties"
echo "===================================="

if [ -f "keystore.properties" ]; then
    echo "✅ keystore.properties already exists"
else
    echo "📝 Creating keystore.properties..."
    cp keystore.properties.template keystore.properties
    echo "✅ keystore.properties created from template"
    echo "⚠️  Please edit keystore.properties with your actual values"
    echo "   Default values are already set for quick testing"
fi

echo ""
echo "📋 Step 3: Test Release Build"
echo "============================="

read -p "Do you want to build the release version now? (y/n): " build_release

if [ "$build_release" = "y" ]; then
    echo "🔨 Building release version..."
    ./build_release.sh
    
    if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
        echo "✅ Release build successful!"
        echo "   AAB file: app/build/outputs/bundle/release/app-release.aab"
    else
        echo "❌ Release build failed"
        echo "   Please check the error messages above"
    fi
else
    echo "⚠️  Skipping release build"
fi

echo ""
echo "📋 Step 4: Next Steps"
echo "===================="

echo "✅ Setup complete! Here's what to do next:"
echo ""
echo "1. 📱 Test the release APK on a physical device:"
echo "   adb install app/build/outputs/apk/release/app-release.apk"
echo ""
echo "2. 🎯 Complete the store listing materials:"
echo "   - Create app screenshots"
echo "   - Design app icon (512x512)"
echo "   - Write app description"
echo "   - Create privacy policy"
echo ""
echo "3. 🏪 Google Play Console setup:"
echo "   - Create Play Console account ($25 fee)"
echo "   - Create new app listing"
echo "   - Upload AAB file"
echo "   - Complete store listing"
echo ""
echo "4. 📖 Read the documentation:"
echo "   - PLAY_STORE_SUBMISSION.md (detailed guide)"
echo "   - PLAY_STORE_CHECKLIST.md (submission checklist)"
echo "   - PRIVACY_POLICY_TEMPLATE.md (privacy policy template)"
echo ""
echo "5. 🔐 Security reminders:"
echo "   - Keep your keystore file secure and backed up"
echo "   - Never share your keystore passwords"
echo "   - You'll need the same keystore for all future updates"
echo ""
echo "Good luck with your Play Store submission! 🚀"