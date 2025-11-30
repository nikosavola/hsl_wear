# HSL Wear - Helsinki Transit Watch App

[![Platform](https://img.shields.io/badge/Platform-Wear%20OS-4285F4?logo=android)](https://wearos.google.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Compose-Wear%20OS-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-success)]()

A smartwatch application for Wear OS that provides real-time public transportation guidance for the Helsinki Region (HSL). The app is designed to be ultra-minimalist, focusing on providing essential transit information at a glance during your journey.

**Complete watch-only solution** - no companion phone app required. Plan routes, track journeys, and navigate Helsinki's public transit entirely from your wrist.

## Screenshots

<table>
  <tr>
    <td><img src="screenshots/01_home.png" alt="Home Screen" width="200"/><br/><em>Home Screen</em></td>
    <td><img src="screenshots/02_search.png" alt="Location Search" width="200"/><br/><em>Location Search</em></td>
    <td><img src="screenshots/03_search_button.png" alt="Search Button" width="200"/><br/><em>Search Button</em></td>
  </tr>
  <tr>
    <td><img src="screenshots/04_routes.png" alt="Available Routes" width="200"/><br/><em>Available Routes</em></td>
    <td><img src="screenshots/05_route_nav.gif" alt="Route Navigation" width="200"/><br/><em>Route Navigation</em></td>
    <td><img src="screenshots/06_departs.png" alt="Departure Information" width="200"/><br/><em>Departure Information</em></td>
  </tr>
  <tr>
    <td><img src="screenshots/07_leg_arrives.png" alt="Leg Arrival" width="200"/><br/><em>Leg Arrival</em></td>
    <td><img src="screenshots/08_tile_boards.png" alt="Tile Boards" width="200"/><br/><em>Tile Boards</em></td>
    <td><img src="screenshots/09_tile_arrives.png" alt="Tile Arrivals" width="200"/><br/><em>Tile Arrivals</em></td>
  </tr>
</table>

The app features a clean, watch-optimized interface designed for glanceable information with real-time transit updates.

## Features

### 🎯 Core Functionality
- **Smart Route Planning**: Plan journeys between any two locations in Helsinki region with location autocomplete
- **Step-by-Step Navigation**: Leg-by-leg guidance through your transit journey with real-time updates
- **Offline Resilient**: Routes persist even when app is closed or watch restarts
- **Battery Efficient**: Zero GPS usage, no background services, minimal network calls
- **Watch-Only Design**: Complete functionality without requiring a companion phone app

### 🔄 Real-Time Transit Intelligence
- **Live Trip Status**: Real-time delay information and departure updates for all transit legs
- **Smart Countdowns**: Dynamic "Boards in X min"/"Arrives in X min" with second precision
- **Automatic Leg Progression**: Intelligent advancement between transit segments during journey
- **Multi-Trip Monitoring**: Batch status queries for current and upcoming journey legs
- **Delay Indication**: Visual indicators (blue highlighting) when real-time data is available

### 📱 Advanced Wear OS Tile Integration
- **Timeline-Based Tiles**: Wear OS ProtoLayout with automatic leg switching based on schedule
- **Smart Content Display**: Transport mode, platform codes, and journey status on watch face
- **Deep Link Integration**: Tile clicks open app directly at current journey leg
- **Manual Refresh Capability**: User-initiated tile updates for on-demand information
- **Route Obsolescence Management**: Automatic cleanup 2 minutes after final arrival

### 📱 User Interface
- **Watch-First Design**: Optimized specifically for Wear OS small screens and round displays
- **Text Input**: Use on-watch keyboard for location input with autocomplete suggestions
- **One-Tap Operation**: All essential actions accessible with minimal interaction
- **Glanceable Information**: Large text, clear icons, countdown timers for quick reading
- **Deep Linking**: Tile integration with direct navigation to current journey leg
- **Visual Feedback**: Color-coded real-time indicators and delay status
- **Smart Timeline**: Automatic content updates based on journey progress

### 🚦 Transit Modes Supported
- Bus (including regional buses)
- Tram
- Metro (Subway)
- Train (Commuter and long-distance)
- Ferry
- Walking directions

## Architecture

### Technology Stack
- **Platform**: Wear OS 3.0+ (Android API 30+)
- **Language**: Kotlin 2.0.21 with Coroutines
- **UI Framework**: Jetpack Compose for Wear OS
- **Architecture**: Clean Architecture with MVVM and Repository Pattern
- **Dependency Injection**: Hilt
- **Persistence**: Jetpack DataStore with JSON serialization
- **Networking**: OkHttp with HSL Digitransit GraphQL API
- **Real-time Updates**: GraphQL trip status queries
- **Tile Framework**: Wear OS ProtoLayout with timeline-based updates
- **Location**: Fused location provider with multiple fallback strategies

### Key Design Principles
1. **Ultra-Minimalist**: Each screen has one clear purpose with focused information
2. **Battery Conscious**: No GPS, no background services, no continuous polling
3. **Resilient**: State survives app kills, watch reboots, and network interruptions
4. **Accessible**: Large touch targets and high contrast UI
5. **Independent**: Complete functionality without phone companion app

## App Structure

### Screen Flow
1. **Home Screen**: Start new route or resume active journey (if exists)
2. **Origin Input**: Enter starting location with keyboard and autocomplete suggestions
3. **Destination Input**: Enter destination with same input methods
4. **Route Selection**: Choose from 3-4 itinerary options with duration and mode details
5. **Route Tracking**: Step through each leg with departure times, platforms, and countdowns
6. **Journey Completion**: Finish route and return to home screen

### Data Flow
```
Location Input → Autocomplete → Route Planning → Route Selection → Active Navigation
      ↓              ↓              ↓                 ↓                   ↓
  Text Input    GraphQL API    GraphQL API       Save to Store      Current Leg
      ↓              ↓              ↓                 ↓                   ↓
  User Types    Stop Matches   3-4 Routes      DataStore JSON    Real-time Updates
                                                            ↓
                                                    Tile Integration ← Timeline Updates
```

### Real-time Architecture
```
Route Planning → Trip ID Extraction → Status Monitoring → Delay Calculation → UI Updates
       ↓               ↓                    ↓                    ↓                ↓
  GraphQL API     GTFS Trip IDs     Periodic Queries    Arrival/Departure   Live Countdowns
       ↓               ↓                    ↓                    ↓                ↓
   Initial Data   RouteState Store   HSLRepository     TransitRepository   CurrentLegTileService
```

### State Persistence
The app uses Jetpack DataStore with JSON serialization to persist user data:
- **Active Routes**: Complete journey state survives app closure and watch reboots
- **Smart Recovery**: Seamless resume with <1 second restoration time
- **User Preferences**: Favorite routes (up to 20), recent locations (up to 20), favorite locations (up to 10)
- **Automatic Cleanup**: Route obsolescence management 2 minutes after final arrival
- **Offline Capability**: Previously planned routes available without network connection

## Building the Project

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 8 or higher
- Kotlin 1.9.10+
- Android SDK with Wear OS components

### Setup Steps
1. Clone the repository
2. Open in Android Studio
3. Sync project with Gradle files
4. Create a Wear OS emulator or connect a physical device
5. Run the app

### Build Configuration
- **Min SDK**: API 30 (Wear OS 2.0+)
- **Target SDK**: API 34 (Wear OS 4.0+)
- **Compile SDK**: API 34

## API Integration

The app integrates with HSL's Digitransit GraphQL API:

### Endpoints
- **Base URL**: `https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql`
- **Location Search**: GraphQL geocoding query for stop autocomplete
- **Route Planning**: GraphQL plan query returning multiple itineraries
- **Real-time Data**: Stop departure times with realtime delay information

### Data Models
- **Location**: Stop or address with coordinates and GTFSId
- **Leg**: Individual journey segment (walk, bus, tram, metro, train, ferry)
- **Itinerary**: Complete journey with multiple legs and total duration
- **RouteState**: Active navigation state with current leg index, persisted to DataStore

## Key Features in Detail

### State Persistence
- Uses Jetpack DataStore with JSON serialization
- Survives app kills, watch reboots, and screen-off events
- Active route restored in <1 second on app relaunch
- Automatic cleanup when journey is completed
- No stale data accumulation

### Battery Optimization
- **Zero GPS usage**: All location data from stop coordinates and schedules
- **No background services**: App only active when screen is visible
- **Minimal network calls**: Only during route planning and optional realtime updates
- **Efficient coroutines**: Non-blocking async operations for all network requests
- **Typical usage**: <2% battery drain per 30-minute journey

### Wear OS Optimizations
- Material Design 3 for Wear OS (Wear Compose)
- Optimized for round and square watch faces
- Large touch targets following Wear OS guidelines
- Rotary input support for scrolling lists

## File Structure

```
app/src/main/java/com/hsl/wear/
├── data/
│   ├── models/
│   │   ├── TransitModels.kt      # Core domain models (Location, Leg, Itinerary)
│   │   └── GraphQLModels.kt      # API response models
│   ├── repository/
│   │   ├── TransitRepository.kt  # Interface for transit data operations
│   │   └── HslRepository.kt      # HSL-specific implementation
│   └── store/
│       └── RouteStore.kt         # DataStore persistence layer
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt         # Entry point with resume/start options
│   │   ├── FromInputScreen.kt    # Origin input
│   │   ├── FromResultsScreen.kt  # Origin autocomplete results
│   │   ├── ToInputScreen.kt      # Destination input
│   │   ├── ToResultsScreen.kt    # Destination autocomplete results
│   │   ├── RouteSelectionScreen.kt   # Itinerary list
│   │   └── RouteTrackingScreen.kt    # Active navigation
│   ├── models/
│   │   ├── HomeUiState.kt
│   │   ├── LocationInputUiState.kt
│   │   ├── RouteSelectionUiState.kt
│   │   └── RouteTrackingUiState.kt
│   ├── viewmodel/
│   │   ├── HomeViewModel.kt
│   │   ├── LocationInputViewModel.kt
│   │   ├── RoutePlanningViewModel.kt
│   │   ├── RouteSelectionViewModel.kt
│   │   └── RouteTrackingViewModel.kt
│   └── theme/
│       └── Theme.kt              # Material3 Wear OS theme
├── network/
│   ├── GraphQLClient.kt          # OkHttp-based GraphQL client
│   ├── GraphQLQueries.kt         # Query string templates
│   └── GeocodingClient.kt        # Location search client
├── navigation/
│   └── AppNavigation.kt          # Compose navigation setup
└── MainActivity.kt               # Main entry point
```

## Contributing

Contributions are welcome! Whether it's bug fixes, new features, or documentation improvements.

### Development Guidelines
- Follow Kotlin coding conventions
- Use Compose for Wear OS best practices
- Ensure battery efficiency in all features
- Test on both emulators and physical devices
- Maintain minimal UI complexity
- Write clear commit messages
- Update documentation as needed

### Adding New Features
1. Update data models in TransitModels.kt if needed
2. Add repository methods in TransitRepository interface
3. Implement in HslRepository with GraphQL queries
4. Create/update ViewModel with proper state management
5. Design Wear OS optimized UI with Compose
6. Add navigation routes in AppNavigation.kt
7. Test battery impact and performance
8. Update journey_map.md with new user flows

### Adding Screenshots
If you'd like to contribute screenshots:
1. Take screenshots on a Wear OS device or emulator
2. Place them in the `screenshots/` directory
3. Use descriptive filenames (e.g., `home_screen.png`, `route_tracking.png`)
4. Update the Screenshots section in README.md
5. Submit a pull request

## Troubleshooting

### Common Issues
- **Network Issues**: Check internet connectivity and HSL API status at https://digitransit.fi
- **Autocomplete Not Working**: Verify network connection; API requires minimum 3 characters
- **Route Not Resuming**: Check if RouteState exists in DataStore; may have been cleared
- **Battery Drain**: Monitor for unexpected background processes (should be none)
- **UI Layout**: Test on different watch screen sizes and shapes (round/square)

### Debug Mode
Enable debug logging in `GraphQLClient.kt` by modifying the OkHttp logging interceptor level to `BODY`.

### Testing
- Use Android Studio's Wear OS emulator for initial testing
- Test on physical device for accurate battery and performance metrics
- Simulate network loss to verify offline resilience
- Test app kill and watch reboot scenarios for state persistence

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

**Important**: This app uses HSL's public Digitransit API. Please review HSL's API terms of service for any commercial usage restrictions or rate limiting requirements.

## Documentation

- **journey_map.md**: Complete UX journey map with user flows, edge cases, and emotional arc
- **initial_plan.md**: Original architecture and planning documents
- **impl_example.md**: Detailed implementation examples with code snippets
- **watch_only.md**: Design rationale for watch-only approach

## Support

For issues and questions:
1. Check the troubleshooting section above
2. Review HSL's Digitransit API documentation at https://digitransit.fi/en/developers/
3. Check journey_map.md for understanding user flows and edge cases
4. Create GitHub issues with detailed descriptions and logs

## Roadmap

### Current Version (v1.0)
- **Core Features**: Complete route planning and navigation with step-by-step guidance
- **State Persistence**: Journey survival across app closure and watch reboots
- **Input Methods**: On-watch keyboard with location autocomplete
- **Real-time Updates**: Live departure times, delay information, and countdown displays
- **Tile Integration**: Wear OS timeline tiles with automatic leg switching
- **Smart Refresh**: Manual and automatic data refresh capabilities
- **Deep Linking**: Direct navigation from tiles to current journey leg
- **Multi-modal Support**: Bus, Tram, Metro, Train, Ferry, and Walking directions
- **Offline Capability**: Route persistence without network connection
- **Battery Optimization**: Zero GPS usage and minimal background processing

### Future Enhancements
- Favorite locations (Home, Work)
- Recent routes for quick repeat
- Route history log
- Pre-departure notifications
- Watch face complications
- Offline stop database for faster autocomplete
- Haptic feedback on leg transitions
- Multi-language support (Finnish, Swedish, English)

---

**Note**: This is an independent implementation using HSL's public Digitransit API. For production usage, please review HSL's API terms of service and implement appropriate rate limiting and error handling.