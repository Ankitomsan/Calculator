# Modern Android Calculator

[![Build Android APK](https://github.com/Ankitomsan/Calculator/actions/workflows/build-apk.yml/badge.svg)](https://github.com/Ankitomsan/Calculator/actions/workflows/build-apk.yml)

A modern, fast, and feature-rich Android scientific calculator built with Jetpack Compose, Material 3, and Room Database for persistent calculation history.

## Features

- **Standard Operations**: Addition, subtraction, multiplication, division, percentage, sign toggle.
- **Scientific Functions**: Trigonometric functions (sin, cos, tan), logarithmic functions (ln, log), powers, roots, factorials, and constants ($\pi$, $e$).
- **Angle Modes**: Toggle between Degrees (DEG) and Radians (RAD).
- **Interactive History**: View and reload previous calculations, powered by local Room database.
- **Material 3 Design**: Dynamic theming, responsive layout for portrait and landscape/tablets, and haptic feedback.

## Building the APK via GitHub Actions

This repository includes an automated GitHub Actions CI workflow in `.github/workflows/build-apk.yml`.

### How to download the APK:
1. Go to the **[Actions tab](https://github.com/Ankitomsan/Calculator/actions)** of this repository.
2. Click on the latest workflow run (e.g., **Build Android APK**).
3. Scroll down to the **Artifacts** section at the bottom of the page.
4. Click on **`calculator-debug-apk`** to download the APK.
5. Transfer the APK to your Android device and install it.

## Local Build Instructions

Prerequisites:
- Android SDK (API 36)
- JDK 21

To build the debug APK locally:
```bash
./gradlew assembleDebug
```
The APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```
