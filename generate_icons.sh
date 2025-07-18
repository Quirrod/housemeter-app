#!/bin/bash

# Script to generate all required app icon sizes from SVG
# Requires ImageMagick (convert command)

echo "🎨 HouseMeter Icon Generator"
echo "============================"

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "❌ ImageMagick is not installed. Please install it first:"
    echo "   sudo apt-get install imagemagick"
    echo "   or"
    echo "   sudo yum install ImageMagick"
    exit 1
fi

# Check if SVG file exists
if [ ! -f "app_icon_512.svg" ]; then
    echo "❌ app_icon_512.svg not found!"
    echo "Please make sure the SVG file is in the current directory"
    exit 1
fi

echo "✅ Found app_icon_512.svg"
echo "📱 Generating Android launcher icons..."

# Create directories if they don't exist
mkdir -p app/src/main/res/mipmap-mdpi
mkdir -p app/src/main/res/mipmap-hdpi
mkdir -p app/src/main/res/mipmap-xhdpi
mkdir -p app/src/main/res/mipmap-xxhdpi
mkdir -p app/src/main/res/mipmap-xxxhdpi

# Generate Android launcher icons
echo "   📐 Generating mdpi (48x48)..."
convert app_icon_512.svg -resize 48x48 app/src/main/res/mipmap-mdpi/ic_launcher.png

echo "   📐 Generating hdpi (72x72)..."
convert app_icon_512.svg -resize 72x72 app/src/main/res/mipmap-hdpi/ic_launcher.png

echo "   📐 Generating xhdpi (96x96)..."
convert app_icon_512.svg -resize 96x96 app/src/main/res/mipmap-xhdpi/ic_launcher.png

echo "   📐 Generating xxhdpi (144x144)..."
convert app_icon_512.svg -resize 144x144 app/src/main/res/mipmap-xxhdpi/ic_launcher.png

echo "   📐 Generating xxxhdpi (192x192)..."
convert app_icon_512.svg -resize 192x192 app/src/main/res/mipmap-xxxhdpi/ic_launcher.png

# Generate round icons (same as regular for now)
echo "📱 Generating round launcher icons..."
cp app/src/main/res/mipmap-mdpi/ic_launcher.png app/src/main/res/mipmap-mdpi/ic_launcher_round.png
cp app/src/main/res/mipmap-hdpi/ic_launcher.png app/src/main/res/mipmap-hdpi/ic_launcher_round.png
cp app/src/main/res/mipmap-xhdpi/ic_launcher.png app/src/main/res/mipmap-xhdpi/ic_launcher_round.png
cp app/src/main/res/mipmap-xxhdpi/ic_launcher.png app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png
cp app/src/main/res/mipmap-xxxhdpi/ic_launcher.png app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png

# Generate Play Store icon
echo "🏪 Generating Play Store icon (512x512)..."
convert app_icon_512.svg -resize 512x512 play_store_icon_512.png

# Generate feature graphic template
echo "🖼️ Generating feature graphic template..."
convert -size 1024x500 xc:"#1976D2" \
        \( app_icon_512.svg -resize 200x200 \) -gravity west -geometry +50+0 -composite \
        \( -font Arial -pointsize 72 -fill white -gravity center -annotate +100+0 "HouseMeter" \) \
        feature_graphic_1024x500.png

echo ""
echo "✅ Icon generation complete!"
echo "📂 Files generated:"
echo "   • Android launcher icons: app/src/main/res/mipmap-*/ic_launcher.png"
echo "   • Play Store icon: play_store_icon_512.png"
echo "   • Feature graphic: feature_graphic_1024x500.png"
echo ""
echo "🔧 Next steps:"
echo "1. Remove old .webp files: rm app/src/main/res/mipmap-*/*.webp"
echo "2. Build and test your app"
echo "3. Upload play_store_icon_512.png to Google Play Console"
echo "4. Use feature_graphic_1024x500.png for store listing"
echo ""
echo "💡 Tip: You can edit app_icon_512.svg and run this script again to regenerate all icons"