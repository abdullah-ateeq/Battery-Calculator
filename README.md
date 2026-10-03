# VoltDuration - Battery Energy & Runtime Calculator

**VoltDuration** is a modern, offline Android application built with Jetpack Compose and Material Design 3. It provides accurate, real-time battery energy estimation, usable capacity breakdown, and runtime calculation based on battery voltage, capacity, load, efficiency, and depth of discharge.

---

## ⚡ Key Features

- **🔋 Battery Energy Calculation**: Computes nominal energy, usable energy, and effective energy in Watt-hours (Wh) and Kilowatt-hours (kWh).
- **⏱️ Live Runtime Estimation**: Live calculation of battery backup time presented in days, hours, and minutes alongside decimal hours.
- **⚡ Dual Load Input Modes**: Support for both **Watts (W)** for direct power loads and **Amperes (A)** for voltage-referenced current loads.
- **🔌 Efficiency & Loss Adjustment**: Configurable efficiency percentage to account for inverter, voltage regulator, and wire transmission losses.
- **📊 Usable Capacity / Depth of Discharge (DoD)**: Protect battery chemistry (e.g., Lead-Acid, LiFePO4, Lithium-ion) by specifying max usable capacity.
- **🔄 Dual Capacity Units**: Seamless switching and conversion between **Ampere-hours (Ah)** and **Milliampere-hours (mAh)**.
- **📜 Calculation History**: Saves up to 50 previous calculation records locally using Jetpack DataStore Preferences, with one-tap restore and clear features.
- **🎨 Modern Material 3 UI**: Clean interface with dynamic color support, light/dark themes, interactive visual battery indicator, and energy breakdown cards.
- **🔒 100% Offline & Private**: Zero external network requests, zero tracking, completely private standalone utility.

---

## 📐 Formulas & Calculation Engine

VoltDuration calculates energy and runtime metrics using standard electrical engineering equations:

1. **Capacity Conversion (Ah)**:
   $$\text{Capacity (Ah)} = \begin{cases} \text{Capacity}_{\text{Ah}} & \text{if unit = Ah} \\ \frac{\text{Capacity}_{\text{mAh}}}{1000} & \text{if unit = mAh} \end{cases}$$

2. **Nominal Energy (Wh)**:
   $$\text{Nominal Energy (Wh)} = \text{Voltage (V)} \times \text{Capacity (Ah)}$$

3. **Usable Energy (Wh)**:
   $$\text{Usable Energy (Wh)} = \text{Nominal Energy (Wh)} \times \left( \frac{\text{Usable Capacity \%}}{100} \right)$$

4. **Effective Energy (Wh)**:
   $$\text{Effective Energy (Wh)} = \text{Usable Energy (Wh)} \times \left( \frac{\text{Efficiency \%}}{100} \right)$$

5. **Load Power (Watts)**:
   $$\text{Load (W)} = \begin{cases} \text{Load}_{\text{Watts}} & \text{if mode = Watts} \\ \text{Voltage (V)} \times \text{Load}_{\text{Amps}} & \text{if mode = Amps} \end{cases}$$

6. **Runtime (Hours)**:
   $$\text{Runtime (Hours)} = \frac{\text{Effective Energy (Wh)}}{\text{Load (W)}}$$

---

## 🛠️ Tech Stack & Architecture

VoltDuration is built following modern Android development best practices and Clean Architecture principles:

- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose with Material 3
- **Navigation**: Jetpack Navigation Compose
- **State Management**: MVVM Pattern with `ViewModel` and Kotlin `StateFlow`
- **Data Persistence**: Jetpack DataStore Preferences
- **Build System**: Gradle Version Catalogs (`libs.versions.toml`) with Android Gradle Plugin 9.4+
- **Testing**: JUnit 4 for unit testing domain logic

### Project Structure
```text
com.example.voltduration
├── data
│   ├── model          # Data classes (Calculation models, Settings, Enums)
│   └── storage        # PreferencesManager with DataStore
├── domain
│   ├── CalculatorEngine.kt  # Mathematical calculation and validation engine
│   └── Formatters.kt        # Formatting utilities for date, energy, and runtime
├── ui
│   ├── components     # Reusable UI components (BatteryVisualIndicator, etc.)
│   ├── navigation     # AppNavHost and Screen destinations
│   ├── screens        # Calculator, History, Settings, About, and Splash screens
│   ├── theme          # Material 3 Color, Type, and Theme configuration
│   └── viewmodel      # VoltDurationViewModel
└── MainActivity.kt
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Ladybug / Meerkat (2024.3+) or higher
- **JDK**: Java 11 or higher
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 37 (Android 15)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Battery-Calculator.git
   cd Battery-Calculator
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and build project:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run unit tests to verify calculator engine:
   ```bash
   ./gradlew test
   ```
5. Deploy to an emulator or physical device.

---

## ⚠️ Disclaimer

Runtime predictions produced by **VoltDuration** are theoretical calculations. Actual battery duration may vary depending on battery health, age, discharge curve characteristics (e.g. Peukert's Law for lead-acid batteries), ambient temperature, voltage drop, and efficiency fluctuations.

---

## 📄 License

```text
Copyright 2026 Abdullah Ateeq

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
