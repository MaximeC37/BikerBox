# BikerBox 🏍

## ⚠️ Statut du Projet : En Développement Actif
Ce projet est actuellement en cours de développement et n'est pas encore finalisé. Certaines fonctionnalités peuvent être incomplètes ou sujettes à modification. Version actuelle : Alpha 0.1

## À propos
BikerBox est un projet personnel d'application mobile en cours de développement, destiné aux motards. Cette application est un prototype/démonstrateur technique développé à des fins d'apprentissage et d'expérimentation avec Kotlin Multiplatform et Jetpack Compose. Elle simule un système de gestion de casiers sécurisés permettant aux motards de stocker leurs équipements (casques, blousons, etc.).

⚠️ **Notes importantes** :
- Projet en développement actif : des changements majeurs peuvent survenir
- Ceci est un projet personnel de démonstration et n'est pas destiné à une utilisation en production
- Certaines fonctionnalités sont encore en cours d'implémentation

## 🌟 Fonctionnalités

### Implémentées ✅
- Système d'authentification basique
- Interface utilisateur principale
- Navigation entre les écrans
- Configuration Firebase de base

### En Cours de Développement 🚧
- Système complet de gestion des casiers
- Profil utilisateur avancé
- Historique des réservations
- Système de notifications

### Planifiées 📋
- Mode hors ligne
- Système de paiement (simulation)
- Interface administrateur
- Support multilingue complet

## 🔧 Prérequis Techniques

- Android Studio Hedgehog | 2023.1.1 ou plus récent
- JDK 23
- Kotlin 2.1.20
- Android SDK 34
- Un compte Firebase (pour le développement)
- Git

## 📥 Installation

### 1. Cloner le Repository

bash git clone [https://github.com/votre-username/BikerBox.git](https://github.com/votre-username/BikerBox.git) cd BikerBox

### 2. Configuration Firebase

1. Créez un projet dans la [Console Firebase](https://console.firebase.google.com/)
2. Ajoutez une application Android avec le package `org.perso.bikerbox`
3. Téléchargez le fichier `google-services.json`
4. Placez-le dans le dossier `composeApp/`

Note : Un fichier `google-services.json.example` est fourni dans le dépôt comme exemple de structure. **Ne pas l'utiliser en production.**

Les étapes supplémentaires nécessaires :
- Activez Authentication (Email/Password)
- Configurez Firestore Database
- Configurez Storage si nécessaire


### 3. Configuration du Projet

1. Ouvrez le projet dans Android Studio
2. Synchronisez le projet avec Gradle
3. Vérifiez que toutes les dépendances sont téléchargées
4. Configurez votre fichier `local.properties` avec:
```properties
sdk.dir=CHEMIN_VERS_VOTRE_SDK_ANDROID
```

## ⚡ Problèmes Connus
- L'application peut être instable sur certains appareils
- Certaines fonctionnalités sont simulées ou partiellement implémentées
- Les performances peuvent ne pas être optimales pendant la phase de développement

## 🚀 Compilation et Exécution
### Android
1. Sélectionnez la configuration 'composeApp'
2. Choisissez un appareil ou émulateur Android
3. Cliquez sur 'Run'

### Web (Version de développement)
``` bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```
## 🏗 Architecture
Ce projet personnel suit l'architecture MVVM (Model-View-ViewModel) et est structuré en plusieurs couches:
- **data**: Repositories et sources de données
- **domain**: Modèles et cas d'utilisation
- **ui**: Écrans et composants d'interface utilisateur
- **di**: Injection de dépendances
- **utils**: Utilitaires et extensions

## 🛠 Technologies Utilisées
- **Kotlin Multiplatform** - Pour le partage de code entre plateformes
- **Compose Multiplatform** - Pour l'interface utilisateur
- **Firebase** - Pour l'authentification et le stockage de données
- **Koin** - Pour l'injection de dépendances
- **Kotlin Coroutines** - Pour la programmation asynchrone
- **Kotlin Flow** - Pour la programmation réactive

## 🎯 Objectifs du Projet
Ce projet personnel a été développé dans le but de :
- Explorer les capacités de Kotlin Multiplatform
- Expérimenter avec Jetpack Compose
- Mettre en pratique les principes de l'architecture MVVM
- Créer une démonstration technique d'une application mobile moderne

Projet personnel en développement actif 🚧 | Développé avec ❤️ pour explorer le développement mobile moderne
