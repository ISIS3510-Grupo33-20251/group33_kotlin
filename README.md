# group33_kotlin
# Universe - Android Organization App

Universe is an organization app for Android that helps users manage their schedules, connect with friends, create teams, and collaborate on projects.

### Prerequisites

- Android Studio Arctic Fox (2021.3.1) or newer
- JDK 11
- Kotlin 1.8.10 or newer

### Setup

1. Clone the repository
2. Open the project in Android Studio.
3. Update the API base URL in `di/NetworkModule.kt` to point to your FastAPI backend:
.baseUrl("https://your-api-url.com
4. Sync Gradle and build the project.
5. Run on an emulator or physical device.

### Architecture
Universe is built using MVVM (Model-View-ViewModel) with Clean Architecture principles.

com.example.universe/
├── data/               # Data layer
│   ├── api/            # API interfaces, Retrofit services
│   ├── db/             # Local database (Room)
│   ├── models/         # Data transfer objects
│   └── repositories/   # Repository implementations
├── domain/             # Domain layer
│   ├── models/         # Domain entities
│   ├── repositories/   # Repository interfaces
│   └── usecases/       # Business logic
├── presentation/       # Presentation layer
│   ├── auth/           # Login screens
│   ├── common/         # Shared UI components
│   ├── location/       # Location features
│   └── profile/        # Profile management
└── utils/              # Utility classes

### Key Components

Jetpack Compose: Modern UI toolkit for building native UI
Hilt: Dependency injection framework
Retrofit: Type-safe HTTP client for API communication
Room: Local database for offline caching
Coroutines & Flow: For asynchronous operations
Google Maps: For location tracking features

