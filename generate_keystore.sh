#!/bin/bash

# Script to generate a keystore for signing the Android app
# This creates a release keystore that should be kept secure

echo "Generating Android Release Keystore..."
echo "Please provide the following information:"

read -p "Enter your name: " USER_NAME
read -p "Enter your organization: " ORGANIZATION
read -p "Enter your city: " CITY
read -p "Enter your state/province: " STATE
read -p "Enter your country code (e.g., US, ES): " COUNTRY

# Generate keystore
keytool -genkey -v -keystore release-key.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias housemeter-key \
    -dname "CN=$USER_NAME, O=$ORGANIZATION, L=$CITY, S=$STATE, C=$COUNTRY" \
    -storepass android -keypass android

echo ""
echo "Keystore generated successfully: release-key.keystore"
echo ""
echo "IMPORTANT SECURITY NOTES:"
echo "1. Keep this keystore file secure and backed up"
echo "2. Never commit this file to version control"
echo "3. Store the passwords securely"
echo "4. You'll need this exact keystore for all future app updates"
echo ""
echo "Default passwords used:"
echo "- Store password: android"
echo "- Key password: android"
echo ""
echo "To use different passwords, edit the keytool command above"