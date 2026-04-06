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

### 📱 Embedded Preview (works in some platforms like docs/blogs, NOT GitHub)

<iframe 
    src="https://appetize.io/embed/b_3vx2off6nurzm3y5dkkg72amqm" 
    width="100%" 
    height="600px" 
    frameborder="0" 
    scrolling="no">
</iframe>


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
