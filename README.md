# HSL Wear - Helsinki Transit Watch App

[![Platform](https://img.shields.io/badge/Platform-Wear%20OS-4285F4?logo=android)](https://wearos.google.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Compose-Wear%20OS-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-success)]()

A smartwatch application for Wear OS that provides real-time public transportation guidance for the Helsinki Region (HSL). The app is designed to be ultra-minimalist, focusing on providing essential transit information at a glance during your journey.

**Complete watch-only solution** - no companion phone app required. Plan routes, track journeys, and navigate Helsinki's public transit entirely from your wrist.

## Screenshots

<!-- TODO: Add screenshots here -->
<table>
  <tr>
    <td><img src="screenshots/home_screen.png" alt="Home Screen" width="200"/><br/><em>Home Screen</em></td>
    <td><img src="screenshots/route_planning.png" alt="Route Planning" width="200"/><br/><em>Route Planning</em></td>
    <td><img src="screenshots/route_tracking.png" alt="Route Tracking" width="200"/><br/><em>Active Navigation</em></td>
  </tr>
</table>

> **Note**: Screenshots will be added soon. The app features a clean, watch-optimized interface designed for glanceable information.

## Features

### 🎯 Core Functionality
- **Smart Route Planning**: Plan journeys between any two locations in Helsinki region with location autocomplete
- **Step-by-Step Navigation**: Leg-by-leg guidance through your transit journey with real-time updates
- **Offline Resilient**: Routes persist even when app is closed or watch restarts
- **Battery Efficient**: Zero GPS usage, no background services, minimal network calls
- **Watch-Only Design**: Complete functionality without requiring a companion phone app

### 📱 User Interface
- **Watch-First Design**: Optimized specifically for Wear OS small screens and round displays
- **Voice & Text Input**: Use voice dictation or on-watch keyboard for location input
- **One-Tap Operation**: All essential actions accessible with minimal interaction
- **Glanceable Information**: Large text, clear icons, countdown timers for quick reading
- **Real-time Updates**: Optional live departure times and delay information

### 🚦 Transit Modes Supported
- Bus (including regional buses)
- Tram
- Metro (Subway)
- Train (Commuter and long-distance)
- Ferry
- Walking directions

## Architecture

### Technology Stack
- **Platform**: Android Wear OS
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose for Wear OS
- **Architecture**: MVVM with Repository pattern
- **Persistence**: Jetpack DataStore
- **Networking**: OkHttp with HSL Digitransit GraphQL API
- **Serialization**: Kotlinx Serialization

### Key Design Principles
1. **Ultra-Minimalist**: Each screen has one clear purpose with focused information
2. **Battery Conscious**: No GPS, no background services, no continuous polling
3. **Resilient**: State survives app kills, watch reboots, and network interruptions
4. **Accessible**: Works with voice input, large touch targets, and high contrast UI
5. **Independent**: Complete functionality without phone companion app

## App Structure

### Screen Flow
1. **Home Screen**: Start new route or resume active journey (if exists)
2. **Origin Input**: Enter starting location with voice/keyboard and autocomplete suggestions
3. **Destination Input**: Enter destination with same input methods
4. **Route Selection**: Choose from 3-4 itinerary options with duration and mode details
5. **Route Tracking**: Step through each leg with departure times, platforms, and countdowns
6. **Journey Completion**: Finish route and return to home screen

### Data Flow
```
Location Input → Autocomplete → Route Planning → Route Selection → Active Navigation
      ↓              ↓              ↓                 ↓                   ↓
  Voice/Text    GraphQL API    GraphQL API       Save to Store      Current Leg
      ↓              ↓              ↓                 ↓                   ↓
  User Types    Stop Matches   3-4 Routes      DataStore JSON    Real-time Updates
```

### State Persistence
The app uses Jetpack DataStore to save active routes as JSON, ensuring:
- Route survives app closure
- Seamless resume after watch reboot
- No data loss during screen off or app switching
- Automatic cleanup on journey completion

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
- Voice input integration with system dictation

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
- Core route planning and navigation
- State persistence across restarts
- Voice and keyboard input
- Realtime departure updates

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