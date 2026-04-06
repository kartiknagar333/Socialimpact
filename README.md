# Socialimpact Android App Analysis

## 🚀 Overview

Socialimpact is an Android application built using **Kotlin + Jetpack Compose** that enables users to:

- Create help request posts
- Donate money or items
- Manage profiles (Person / NGO / Corporation)
- Handle payments via Stripe
- Receive notifications via Firebase

The app follows **MVVM + Clean Architecture**, uses **Firebase as backend**, and **Dagger 2 for dependency injection**.

---
## 🎥 Live Demo

🚀 Try the app directly in your browser:

👉 **[Open Demo](https://appetize.io/app/b_3vx2off6nurzm3y5dkkg72amqm)**

---
## 🚀 Getting Started

Follow these steps to download, configure, and run the Socialimpact app with Firebase.

### 1. Clone the project

First, clone this repository:
```bash
git clone https://github.com/kartiknagar333/Socialimpact.git
cd Socialimpact
```

### 2. Set up a Firebase project

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Create a project** and enter a project name. Accept the terms and (optionally) enable Google Analytics.
3. Once the project is created, you'll land on the project overview page.

### 3. Register your Android app

1. On the Firebase Console's project overview page, click the **Android** icon to add a new app.
2. Enter your Android package name. You can find it in your module's `build.gradle` file (`applicationId`).
3. (Optional) Add an app nickname for your reference.
4. Click **Register app**.

### 4. Download `google-services.json`

Firebase generates a configuration file (`google-services.json`) during the registration process. Download this file and move it into the app module's root directory (usually `app/`). Ensure the filename isn't modified (no `(2)` suffix etc.).

### 5. Add the Firebase Gradle plugin

In your **project-level** `build.gradle` (or `build.gradle.kts`) file, add the Google Services plugin:
```gradle
plugins {
    id("com.android.application") version "7.3.0" apply false
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.4" apply false
}
```

Then in your **module-level** `build.gradle` (e.g. `app/build.gradle`):
```gradle
plugins {
    id("com.android.application")
    // Apply the Google services plugin
    id("com.google.gms.google-services")
}
```

This plugin makes the values from `google-services.json` available to the Firebase SDK.

### 6. Add Firebase SDK dependencies

Use the Firebase BoM (Bill of Materials) to manage versions automatically. In your **module-level** `build.gradle`:
```gradle
dependencies {
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))

    // Add the dependencies for the Firebase products you need
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics") // optional

    // Add other Firebase libraries as required
}
```

After adding these dependencies, sync your project with Gradle.

### 7. Run the app

Now you can build and run the app from Android Studio or via Gradle:
```bash
./gradlew installDebug
```

The app should launch and connect to your Firebase project.

---

## 🔒 Firestore Security Rules

Use these rules to secure your Firestore database. Place them under `Firestore Database` > `Rules` in the Firebase Console.
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ─── ACCOUNT ───────────────────────────────────────
    match /account/{userId} {
      allow get, create, update: if request.auth != null
                                 && request.auth.uid == userId;

      match /{allPaths=**} {
        allow read, write: if request.auth != null
                           && request.auth.uid == userId;
      }
    }

    // ─── PROFILE ───────────────────────────────────────
    match /profile/{document=**} {
      allow list: if false;
    }

    match /profile/person/{id}/{userId} {
      allow get: if request.auth != null;
      allow create, update, delete: if request.auth != null
                                    && request.auth.uid == userId;
    }

    match /profile/ngo/{id}/{userId} {
      allow get: if request.auth != null;
      allow create, update, delete: if request.auth != null
                                    && request.auth.uid == userId;
    }

    match /profile/corporation/{id}/{userId} {
      allow get: if request.auth != null;
      allow create, update, delete: if request.auth != null
                                    && request.auth.uid == userId;
    }

    // ─── POSTS & DONATIONS ─────────────────────────────
    match /posts/{postId} {
      allow read: if request.auth != null;

      allow create: if request.auth != null
                    && request.auth.uid == request.resource.data.userId;

      allow update: if request.auth != null && (
        request.auth.uid == resource.data.userId ||
        (
          request.resource.data.diff(resource.data).affectedKeys().hasOnly(['dynamicNeeds']) &&
          request.resource.data.userId == resource.data.userId
        )
      );

      allow delete: if request.auth != null
                    && request.auth.uid == resource.data.userId;

      match /donations/{userId} {
        allow read: if request.auth != null;

        allow create: if request.auth != null
                      && request.auth.uid == userId;

        allow update: if request.auth != null && (
          request.auth.uid == userId ||
          request.auth.uid == get(/databases/$(database)/documents/posts/$(postId)).data.userId
        );

        allow delete: if false;
      }
    }
  }
}
```

These rules ensure:

- **Account data** can only be read and written by the authenticated user.
- **Profiles** are publicly readable but only editable by the owner.
- **Posts** can be read by any authenticated user; only the owner can create or delete a post.
- **Donations** can be created by donors and updated by either the donor or the post owner.

---

💡 **Tip:** Always test your security rules using the Firebase Emulator Suite before deploying to production.

## 🏗️ Architecture

### Pattern
- MVVM (Model - View - ViewModel)
- Clean Architecture (Data / Domain / UI separation)

### Layers

#### 1. Data Layer
- Repository Implementations
- Firebase (Auth, Firestore, Messaging, Functions)
- SharedPreferences (local storage)

#### 2. Domain Layer
- Models:
  - `HelpRequestPost`
  - `Donation`
  - `UserProfile`
- Repository Interfaces
- Validation UseCases

#### 3. Presentation Layer (UI)
- Jetpack Compose Screens
- ViewModels
- State classes (`StateFlow`)

---

## 📂 Project Structure
Handles data sources (Firebase, local storage) and repository implementations.

#### 📁 local
- [`PreferenceManager.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/data/local/PreferenceManager.kt)  
  → Manages local storage using SharedPreferences (user profile caching)

#### 📁 remote
- [`SocialImpactMessagingService.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/data/remote/SocialImpactMessagingService.kt)  
  → Firebase Cloud Messaging service for push notifications

#### 📁 repository
- [`AuthRepositoryImpl.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/data/repository/AuthRepositoryImpl.kt)  
  → Handles authentication (Firebase Auth + Google Sign-In)

- [`HomeRepositoryImpl.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/data/repository/HomeRepositoryImpl.kt)  
  → Fetches home feed and manages profile data

- [`HomePagingSource.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/data/repository/HomePagingSource.kt)  
  → Paging 3 implementation for Firestore posts

- [`PostRepositoryImpl.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/data/repository/PostRepositoryImpl.kt)  
  → Handles post creation, donations, and Firestore transactions

---

### 📁 domain  
Contains business logic (pure Kotlin, no Android dependencies)

#### 📁 model
- [`HelpRequestPost.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/model/HelpRequestPost.kt)  
  → Main data model for help request posts

- [`Donation.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/model/Donation.kt)  
  → Represents donation data

- [`UserProfile.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/model/UserProfile.kt)  
  → User profile model (Person / NGO / Corporation)

#### 📁 repository
- [`AuthRepository.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/repository/AuthRepository.kt)  
  → Auth abstraction

- [`HomeRepository.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/repository/HomeRepository.kt)  
  → Home feed abstraction

- [`PostRepository.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/repository/PostRepository.kt)  
  → Post & donation abstraction

#### 📁 usecase
- [`ValidateEmail.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/usecase/ValidateEmail.kt)  
  → Email validation logic

- [`ValidatePassword.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/usecase/ValidatePassword.kt)  
  → Password validation

- [`ValidateConfirmPassword.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/usecase/ValidateConfirmPassword.kt)  
  → Confirm password validation

- [`ValidationResult.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/domain/usecase/ValidationResult.kt)  
  → Validation result wrapper

---

### 📁 di  
Dependency Injection using Dagger 2

#### 📁 component
- [`AppComponent.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/di/component/AppComponent.kt)  
  → Root Dagger component

- [`SocialImpactApp.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/di/component/SocialImpactApp.kt)  
  → Application class (initializes DI graph)

- Activity Components:
  - `MainActivityComponent.kt` → Injects MainActivity
  - `HomeActivityComponent.kt` → Injects HomeActivity
  - `ProfileActivityComponent.kt` → Injects ProfileActivity
  - `PaymentActivityComponent.kt` → Injects PaymentActivity
  - `UploadActivityComponent.kt` → Injects UploadActivity

#### 📁 module
- `AppModule.kt` → Provides application-level dependencies
- `FirebaseModule.kt` → Firebase services (Auth, Firestore, Functions)
- `AuthModule.kt` → Auth dependencies
- `HomeModule.kt` → Home feature dependencies
- `PostModule.kt` → Post feature dependencies
- `NetworkModule.kt` → (Placeholder for future APIs)
- `AnalyticsModule.kt` → Analytics dependencies
- `SubcomponentModule.kt` → Registers subcomponents

#### 📁 scope
- `ActivityScope.kt` → Activity lifecycle scope
- `FragmentScope.kt` → Fragment lifecycle scope

#### 📁 qualifier
- `EmailAuth.kt` → Qualifier for email auth
- `GoogleAuth.kt` → Qualifier for Google auth

---

### 📁 ui  
Handles UI (Jetpack Compose)

#### 📁 activity
- [`MainActivity.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/ui/activity/MainActivity.kt)  
  → Entry point, handles navigation setup

- [`HomeActivity.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/ui/activity/HomeActivity.kt)  
  → Main feed screen

- [`ProfileActivity.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/ui/activity/ProfileActivity.kt)  
  → User profile screen

- [`UploadActivity.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/ui/activity/UploadActivity.kt)  
  → Create help request post

- [`PaymentActivity.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/ui/activity/PaymentActivity.kt)  
  → Payment & Stripe integration

#### 📁 layouts (Compose Screens)
- `SplashLayout.kt` → App entry UI
- `SigninLayout.kt` → Login UI
- `SignupLayout.kt` → Registration UI
- `HomeLayout.kt` → Feed UI
- `PostDetailLayout.kt` → Post details
- `EditProfileLayout.kt` → Profile editing
- `PostHelpRequestLayout.kt` → Create post
- `PaymentLayout.kt` → Payment screen
- `ProfileLayout.kt` → Profile screen
- `DonationSheetContent.kt` → Donation bottom sheet
- `DonationsListSheet.kt` → Donation list UI

#### 📁 components
- `AppButtons.kt` → Custom buttons
- `AppMenus.kt` → Dropdown menus
- `AppBackgrounds.kt` → UI backgrounds

#### 📁 state
- `SigninUiState.kt` → Login state
- `SignupUiState.kt` → Signup state
- `DonationUiState.kt` → Donation state
- `ProfileUiState.kt` → Profile state
- `UploadPostUiState.kt` → Post creation state

#### 📁 viewmodel
- `AuthViewModel.kt` → Authentication logic
- `HomeViewModel.kt` → Home feed logic
- `ProfileViewModel.kt` → Profile data handling
- `UploadViewModel.kt` → Post creation logic
- `DonationViewModel.kt` → Donation flow
- `SenderPaymentViewModel.kt` → Sender payment
- `ReceiverOnboardingViewModel.kt` → Stripe onboarding

---

### 📁 util
- [`UrlOpener.kt`](https://github.com/kartiknagar333/Socialimpact/blob/main/app/src/main/java/com/example/socialimpact/util/UrlOpener.kt)  
  → Opens external links using Chrome Custom Tabs

---


## 🔄 Navigation

- Built using **Compose Navigation**
- Flow:


---

## 🧠 State Management

- `StateFlow` + `MutableStateFlow`
- UI observes state using:



---

## 🔗 Data Flow

1. UI → ViewModel
2. ViewModel → Repository
3. Repository → Firebase / Local
4. Response → StateFlow → UI

---

## 🧰 Tech Stack

### Core
- Kotlin
- Jetpack Compose
- Material 3

### Architecture
- MVVM
- Clean Architecture

### DI
- Dagger 2

### Backend
- Firebase Auth
- Firestore
- Firebase Functions
- Firebase Messaging

### Payments
- Stripe Android SDK

### Other
- Paging 3
- Coroutines
- Google Identity Services
- Chrome Custom Tabs

---

## ⚙️ Build Config

- Gradle (Kotlin DSL)
- Compile SDK: 36
- Min SDK: 24
- Target SDK: 35

---

## ✅ Strengths

- Clean architecture separation
- Modern UI (Jetpack Compose)
- Scalable state management (StateFlow)
- Firebase + Stripe integration
- Paging for performance

---

## ⚠️ Improvements

### 1. Replace Dagger with Hilt
- Reduce boilerplate

### 2. Error Handling
- Show user-friendly messages
- Add retry mechanism

### 3. Hardcoded Strings
- Move to `strings.xml`

### 4. Security
- Use Encrypted SharedPreferences

### 5. Testing
- Add:
- Unit tests
- Compose UI tests

### 6. Navigation
- Improve back stack handling

### 7. State Handling
- Use sealed classes instead of multiple flags

---

## 🏁 Conclusion

Socialimpact is a well-structured Android application that combines:

- Modern Android development (Compose)
- Scalable architecture (MVVM + Clean)
- Real-world integrations (Firebase + Stripe)

With improvements in DI, security, and testing, this project can reach production-level quality.

---
