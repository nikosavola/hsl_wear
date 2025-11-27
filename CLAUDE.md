# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Wear OS 3.0+ transit application** for Helsinki Regional Transport (HSL) built with modern Android development practices. The app provides real-time public transit information, route planning, and journey tracking optimized for wearable devices.

## Technologies & Architecture

- **Platform**: Wear OS 3.0+ (Android API 30+)
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose for Wear OS
- **Architecture**: Clean Architecture with MVVM pattern
- **Dependency Injection**: Hilt
- **Networking**: OkHttp with GraphQL and REST APIs
- **Data Persistence**: DataStore for offline route storage
- **Location**: Fused location provider with geocoding

## Core Architecture

### Data Layer (`data/`)
- **Repositories**: `HslRepository` (transit data), `TransitRepository` (local persistence)
- **Models**: `FavouriteRoute`, `TransitRoute`, `TransitLeg`, `TransitStop`, `TransitMode`
- **Stores**: `FavouriteRoutesStore`, `TransitRouteStore` using DataStore
- **API Clients**: `GraphQLClient`, `GeocodingClient` in `network/`

### UI Layer (`ui/`)
- **Screens**: Main screens in root `ui/screens/`, route-specific in `ui/screens/route/`
- **Components**: Reusable Wear OS components in `ui/components/`
- **Navigation**: `Navigation.kt` handles screen transitions
- **Theme**: Wear OS specific theming in `theme/`

### Domain Logic
- **Use Cases**: Domain logic in `HslRepository.kt` - search routes, navigate legs, manage favorites
- **Location Services**: `LocationManager` handles GPS and geocoding
- **Transit Tracking**: Real-time journey progress updates

## Key Directories

```
app/src/main/java/com/hsl/wear/
├── di/                    # Hilt dependency injection modules
├── data/                  # Data layer (repositories, models, stores)
├── network/               # HSL API clients and GraphQL queries
├── tiles/                 # Wear OS Tile implementation
├── ui/                    # UI components, screens, navigation
├── location/             # Location services and geocoding
└── utils/                 # Utilities, constants, error messages
```

## Development Workflow

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected Wear OS device/emulator
./gradlew installDebug

# Generate release build (requires signing config)
./gradlew assembleRelease
```

### Key Configuration Files
- `app/build.gradle.kts` - Main build configuration, dependencies
- `local.properties` - API keys and signing configuration (not in repo)
- `app/src/main/AndroidManifest.xml` - Permissions, services, tile declarations

## Core Features

### Route Planning & Navigation
- Location search with autocomplete suggestions
- Multi-modal transit routing (walk, bus, tram, metro, ferry, train)
- Real-time journey tracking with progress updates
- Offline route persistence and resume

### Wear OS Integration
- **Tiles**: `CurrentLegTileService.kt` provides quick access to current journey
- **Notifications**: Real-time updates during active navigation
- **Optimized UI**: Curved layouts, swipe actions, rotatable inputs

### Data Management
- **Favourite Routes**: Persistent storage of frequent routes
- **Offline Access**: Cached routes available without network
- **Real-time Updates**: Live transit data integration

## API Integration

### HSL GraphQL API
- Route planning queries in `GraphQLQueries.kt`
- Real-time vehicle positions and delay information
- Multi-language support (Finnish/Swedish/English)

### Geocoding API
- Address autocomplete and reverse geocoding
- Location search functionality
- Coordinate-based queries

## Important Patterns

### Repository Pattern
All data access goes through repositories which handle:
- Network calls with proper error handling
- Local caching via DataStore
- Offline-first functionality

### Compose for Wear OS
- Use `Scaffold`, `ScalingLazyColumn`, `CurvedText` components
- Implement proper swipe-to-dismiss and rotary input support
- Follow Wear OS design guidelines for interaction patterns

### Coroutines & Flow
- All repository operations return `Flow<T>` or suspend functions
- UI observes data via `collectAsStateWithLifecycle()`
- Proper exception handling and error state management

## Testing Strategy

- **Unit Tests**: Repository logic, utilities, data models
- **UI Tests**: Compose test assertions for Wear OS components
- **Integration Tests**: API client and data store functionality
- **Wear OS Testing**: Use `WearTestRule` for tile testing
