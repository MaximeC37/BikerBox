# BikerBox 🏍

## ⚠️ Project Status: Active Development
This project is currently under development and not yet finalized. Some features may be incomplete or subject to change.
Current version: Alpha 0.2

## About
BikerBox is a personal mobile application project under development, designed for bikers. This application is a prototype/technical demonstrator developed for learning purposes and experimentation with Kotlin Multiplatform and Jetpack Compose. It simulates a secure locker management system allowing bikers to store their equipment (helmets, jackets, etc.).

⚠️ **Important notes**:
- Project in active development: major changes may occur
- This is a personal demonstration project and is not intended for production use
- Some features are still being implemented

## 🌟 Features

### Recently Implemented ✅
- **Complete localization system** with string resources
- **Enhanced payment flow** with confirmation and booking management
- **Multiple payment methods** support (Apple Pay, Google Pay, Visa, Mastercard, PayPal)
- **Improved reservation system** with simplified navigation
- **Enhanced pricing logic** for better accuracy
- **Clean architecture** with unused components removal
- **Better user interface** with consistent translations

### Core Features ✅
- Authentication system
- Main user interface with improved navigation
- Date selection with enhanced calendar interface
- Reservation confirmation flow
- Payment processing system
- User reservation management

### In Development 🚧
- Advanced user profile features
- Notification system enhancements
- Performance optimizations

### Planned 📋
- Offline mode
- Administrator interface
- Advanced analytics
- Extended payment options

## 🔧 Technical Prerequisites

- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 23
- Kotlin 2.2.0
- Android SDK 36 (compileSdk)
- Gradle 8.14.2
- AGP 8.11.0-alpha07
- A Firebase account (for development)
- Git

## 📥 Installation

### 1. Clone the Repository


bash git clone [https://github.com/votre-username/BikerBox.git](https://github.com/votre-username/BikerBox.git) cd BikerBox

### 2. Firebase Configuration

1. Create a project in the [Firebase Console](https://console.firebase.google.com/)
2. Add an Android application with the package `org.perso.bikerbox`
3. Download the `google-services.json` file
4. Place it in the `composeApp/` folder

Note: A `google-services.json.example` file is provided in the repository as a structure example. **Do not use it in production.**

Additional required steps:
- Enable Authentication (Email/Password)
- Configure Firestore Database
- Configure Storage if necessary

### 3. Project Configuration

1. Open the project in Android Studio
2. Sync the project with Gradle
3. Verify that all dependencies are downloaded
4. Configure your `local.properties` file with:
```properties
sdk.dir=CHEMIN_VERS_VOTRE_SDK_ANDROID
```

## ⚡ Problèmes Connus
- L'application peut être instable sur certains appareils
- Certaines fonctionnalités sont simulées ou partiellement implémentées
- Les performances peuvent ne pas être optimales pendant la phase de développement

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
- **Firebase** - For authentication and data storage
- **Koin** - For dependency injection
- **Kotlin Coroutines** - For asynchronous programming
- **Kotlin Flow** - For reactive programming
- **Lifecycle 2.9.1** - For lifecycle management

## 🌍 Localization
The app now features a complete localization system:
- All UI text is externalized to string resources
- Consistent English translation throughout the application
- Easy to extend for additional languages
- Improved maintainability and translation management

## 💳 Payment Integration
Supports multiple payment methods with visual branding:
- Apple Pay
- Google Pay
- Visa
- Mastercard
- PayPal

## 📈 Recent Updates
- **v0.2**: Complete UI localization, enhanced payment flow, improved reservation system
- **v0.1**: Initial release with basic authentication and navigation

## 🎯 Project Goals
This personal project was developed with the aim of:
- Exploring Kotlin Multiplatform capabilities
- Experimenting with Jetpack Compose
- Putting MVVM architecture principles into practice
- Creating a technical demonstration of a modern mobile application
- Implementing modern UI/UX patterns and localization best practices

Personal project in active development 🚧 | Developed with ❤️ to explore modern mobile development