# HouseMeter Android App

Android application for the HouseMeter multi-tenant apartment management system.

## Features

### Multi-User System
- **Super Admin**: Platform-wide administration
- **House Admin**: Building-specific management
- **Regular Users**: Apartment-level access

### Authentication
- User login with role-based navigation
- House admin self-registration
- JWT token authentication with automatic refresh
- Secure DataStore for user session persistence

### Registration Flow
- Public registration for new house admins
- Complete house information collection (name, address, description)
- Automatic house creation with admin assignment
- Data validation and error handling

### Core Functionality
- Apartment management
- Debt tracking and payment processing
- Receipt upload with Cloudinary integration
- Push notifications via Firebase
- Payment history and metrics
- User profile management

## Technical Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Repository pattern
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Local Storage**: DataStore (encrypted preferences)
- **Push Notifications**: Firebase Cloud Messaging
- **File Upload**: Multipart with receipt management

## Project Structure

```
app/src/main/java/com/jarrod/house/
├── data/
│   ├── api/           # REST API interfaces
│   ├── datastore/     # Local data persistence
│   ├── model/         # Data models
│   └── repository/    # Data layer abstraction
├── service/           # Background services
├── ui/
│   ├── screens/       # Compose UI screens
│   └── viewmodel/     # ViewModels for UI state
└── utils/             # Utility classes
```

## Key Components

### Authentication
- `LoginScreen.kt` - User login with registration option
- `RegisterScreen.kt` - House admin registration form
- `AuthViewModel.kt` - Authentication state management
- `AuthRepository.kt` - API integration for auth

### Data Management
- `DataStoreManager.kt` - Encrypted local storage
- `ApiService.kt` - REST API definitions
- `User.kt` - User and house models

### Multi-Tenancy Support
- House-based data isolation
- Role-based feature access
- Dynamic API filtering by house_id

## Setup

1. Clone the repository
2. Open in Android Studio
3. Configure backend API URL in `RetrofitClient.kt`
4. Add Firebase configuration file (`google-services.json`)
5. Build and run

## Backend Integration

This app works with the HouseMeter backend:
- **Repository**: https://github.com/Quirrod/housemeter-backend
- **API Base URL**: Configure in build settings
- **Authentication**: JWT tokens with automatic refresh

## Multi-House Architecture

The system supports multiple independent buildings:
- Each house has its own admin
- Data isolation between houses
- Shared infrastructure for platform management
- Scalable for property management companies

## Dependencies

Key dependencies include:
- Jetpack Compose BOM
- Material Design 3
- Retrofit & OkHttp
- Coil for image loading
- DataStore preferences
- Firebase messaging
- Lifecycle ViewModels

See `build.gradle.kts` for complete dependency list.