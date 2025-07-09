# BikerBox 🏍

## ⚠️ Project Status: Active Development
This project is currently under development and not yet finalized. Some features may be incomplete or subject to change.
Current version: Alpha 0.4

## About
BikerBox is a personal mobile application project under development, designed for bikers. This application is a prototype/technical demonstrator developed for learning purposes and experimentation with Kotlin Multiplatform and Jetpack Compose. It simulates a secure locker management system allowing bikers to store their equipment (helmets, jackets, etc.).

⚠️ **Important notes**:
- Project in active development: major changes may occur
- This is a personal demonstration project and is not intended for production use
- Some features are still being implemented

## 🌟 Features

### Recently Implemented in v0.4 ✅
- **Map Integration**: Integrated Google Maps to display station locations.
- **Real-Time Station Markers**: Added real-time display of BikerBox station markers on the map, fetched directly from Firebase.
- **Location Permissions**: Configured necessary location permissions (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`) for geolocation features.
- **Expanded Localization**: Added new string resources for map and permission features, maintaining a fully sorted and organized file structure.

### Core Features ✅
- Authentication system
- Main user interface with improved navigation
- **Map view with real-time station markers**
- Date selection with enhanced calendar interface
- Reservation confirmation flow with localized dates
- Payment processing system
- User reservation management
- Complete bilingual support (English/French)

### In Development 🚧
- **Runtime permission handling for location**
- Advanced user profile features
- Notification system enhancements
- Performance optimizations

### Planned 📋
- Offline mode
- Administrator interface
- Advanced analytics
- Extended language support
- Extended payment options

## 🔧 Technical Prerequisites

- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 21
- Kotlin 2.1
- Android SDK 34 (compileSdk)
- Gradle 8.14.2
- AGP 8.11.0-alpha07
- A Firebase account (for development)
- Git

## 📥 Installation

### 1. Clone the Repository

bash git clone [https://github.com/votre-username/BikerBox.git](https://github.com/votre-username/BikerBox.git) cd BikerBox

### 2. Configure API Keys and Services

1.  **Firebase**:
    *   Create a project in the [Firebase Console](https://console.firebase.google.com/).
    *   Add an Android application with the package name `org.perso.bikerbox`.
    *   Download the `google-services.json` file and place it in the `composeApp/` directory.
    *   Enable **Authentication** (Email/Password) and the **Firestore Database**.

2.  **Google Maps**:
    *   In the [Google Cloud Console](https://console.cloud.google.com/), ensure the **Maps SDK for Android** is enabled for your project.
    *   Get your **Google Maps API Key**.

### 3. Configure Local Properties

1.  Open the `local.properties` file at the project root (create it if it doesn't exist).
2.  Add your Google Maps API key to this file. **This file is ignored by Git and keeps your key secure.**

    ```properties
    # local.properties (This file should NOT be committed to Git)
    MAPS_API_KEY="YOUR_GOOGLE_MAPS_API_KEY_HERE"
    ```

3.  Sync the project with Gradle in Android Studio.

## ⚡ Known Issues
- The application may be unstable on certain devices.
- Some features are simulated or partially implemented.
- Performance may not be optimal during the development phase.

## 🚀 Build and Run
### Android
1. Select the 'composeApp' configuration
2. Choose an Android device or emulator
3. Click 'Run'

### Web (Development version)
``` bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
## 🏗 Architecture
This personal project follows the MVVM (Model-View-ViewModel) architecture and is structured in several layers:
- **data**: Repositories and data sources
- **domain**: Models and use cases
- **ui**: Screens and user interface components
- **di**: Dependency injection
- **utils**: Utilities and extensions

## 🛠 Technologies Used
- **Kotlin Multiplatform 2.2.0** - For code sharing between platforms
- **Compose Multiplatform 1.8.2** - For user interface
- **Firebase** - For authentication and real-time database
- **Google Maps SDK for Android** - For map display and interaction
- **Koin** - For dependency injection
- **Kotlin Coroutines & Flow** - For asynchronous programming
- **Lifecycle 2.9.1** - For lifecycle management

## 🌍 Localization
The app now features a complete bilingual localization system:
- **Complete French translation** with proper locale handling
- **Localized date and month names** for natural language display
- All UI text is externalized to string resources
- Alphabetically organized string resources for better maintainability
- Easy to extend for additional languages
- Consistent translation management across the application

## 💳 Payment Integration
Supports multiple payment methods with visual branding:
- Apple Pay
- Google Pay
- Visa
- Mastercard
- PayPal

## 📈 Recent Updates
- **v0.4**: Integrated Google Maps with real-time station markers, configured location permissions, and expanded localization resources.
- **v0.3**: Complete French localization, localized date formatting, enhanced locker size system, code cleanup and optimization.
- **v0.2**: Complete UI localization, enhanced payment flow, improved reservation system.
- **v0.1**: Initial release with basic authentication and navigation.

## 🎯 Project Goals
This personal project was developed with the aim of:
- Exploring Kotlin Multiplatform capabilities
- Experimenting with Jetpack Compose
- Putting MVVM architecture principles into practice
- Creating a technical demonstration of a modern mobile application
- Implementing modern UI/UX patterns and localization best practices

Personal project in active development 🚧 | Developed with ❤️ to explore modern mobile development