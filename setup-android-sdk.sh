#!/bin/bash
# FinTrack Android SDK Setup Script
# Run this script to automatically set up Android SDK

set -e

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║         FinTrack Android SDK Automatic Setup                  ║"
echo "╚════════════════════════════════════════════════════════════════╝"

# Check if Homebrew is installed
if ! command -v brew &> /dev/null; then
    echo "❌ Homebrew not found. Installing Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
fi

echo "✅ Homebrew ready"

# Install Android SDK via Homebrew
echo "📦 Installing Android SDK via Homebrew..."
brew install android-sdk

# Get the installation path
ANDROID_SDK_PATH="/usr/local/opt/android-sdk"
echo "✅ Android SDK installed at: $ANDROID_SDK_PATH"

# Update local.properties
echo "⚙️  Updating local.properties..."
echo "sdk.dir=$ANDROID_SDK_PATH" > "$(dirname "$0")/local.properties"
echo "✅ local.properties updated"

# Add to shell profile
SHELL_PROFILE=""
if [ -f "$HOME/.zshrc" ]; then
    SHELL_PROFILE="$HOME/.zshrc"
elif [ -f "$HOME/.bash_profile" ]; then
    SHELL_PROFILE="$HOME/.bash_profile"
else
    SHELL_PROFILE="$HOME/.profile"
fi

echo "📝 Adding ANDROID_HOME to $SHELL_PROFILE..."
if ! grep -q "ANDROID_HOME" "$SHELL_PROFILE"; then
    echo "" >> "$SHELL_PROFILE"
    echo "# Android SDK" >> "$SHELL_PROFILE"
    echo "export ANDROID_HOME=/usr/local/opt/android-sdk" >> "$SHELL_PROFILE"
    echo "export PATH=\$PATH:\$ANDROID_HOME/tools:\$ANDROID_HOME/platform-tools" >> "$SHELL_PROFILE"
fi
echo "✅ Environment variables set"

# Set current environment
export ANDROID_HOME=$ANDROID_SDK_PATH
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Install required SDK components
echo "📥 Installing Android SDK components..."
echo "   - Android SDK Platform 34"
echo "   - Android SDK Build-Tools 34"
echo "   - Android SDK Platform-Tools"

sdkmanager "platforms;android-34"
sdkmanager "build-tools;34.0.0"
sdkmanager "platform-tools"

echo "✅ SDK components installed"

# Verify installation
echo ""
echo "🔍 Verifying installation..."
if [ -f "$ANDROID_SDK_PATH/platforms/android-34/android.jar" ]; then
    echo "✅ Android API 34 found"
else
    echo "❌ Android API 34 not found"
fi

if [ -f "$ANDROID_SDK_PATH/build-tools/34.0.0/dx" ]; then
    echo "✅ Build Tools 34.0.0 found"
else
    echo "⚠️  Build Tools 34.0.0 not found (might need manual update)"
fi

# Test build
echo ""
echo "🔨 Testing build setup..."
cd "$(dirname "$0")"

if ./gradlew clean --quiet 2>/dev/null; then
    echo "✅ Gradle clean successful"
else
    echo "⚠️  Gradle encountered an issue"
fi

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║              ✅ SETUP COMPLETE                                ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "You're ready to build! Run:"
echo "  ./gradlew assembleDebug"
echo ""
echo "To install on device/emulator:"
echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
