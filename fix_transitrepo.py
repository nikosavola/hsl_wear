import re

with open("app/src/main/java/com/hsl/wear/data/repository/TransitRepository.kt", "r") as f:
    content = f.read()

content = content.replace("suspend fun saveRouteState(routeState: RouteState) {", "suspend fun saveRouteState(routeState: RouteState): Result<Unit> {")
content = content.replace("routeStore.saveRouteState(routeState)", "return routeStore.saveRouteState(routeState)")
content = content.replace("suspend fun clearRouteState() {", "suspend fun clearRouteState(): Result<Unit> {")
content = content.replace("routeStore.clearRouteState()", "return routeStore.clearRouteState()")

content = content.replace("suspend fun addFavoriteLocation(location: Location) {", "suspend fun addFavoriteLocation(location: Location): Result<Unit> {")
content = content.replace("routeStore.addFavoriteLocation(location)", "return routeStore.addFavoriteLocation(location)")
content = content.replace("suspend fun removeFavoriteLocation(locationId: String) {", "suspend fun removeFavoriteLocation(locationId: String): Result<Unit> {")
content = content.replace("routeStore.removeFavoriteLocation(locationId)", "return routeStore.removeFavoriteLocation(locationId)")

content = content.replace("suspend fun addFavoriteRoute(favoriteRoute: FavoriteRoute) {", "suspend fun addFavoriteRoute(favoriteRoute: FavoriteRoute): Result<Unit> {")
content = content.replace("routeStore.addFavoriteRoute(favoriteRoute)", "return routeStore.addFavoriteRoute(favoriteRoute)")
content = content.replace("suspend fun removeFavoriteRoute(routeId: String) {", "suspend fun removeFavoriteRoute(routeId: String): Result<Unit> {")
content = content.replace("routeStore.removeFavoriteRoute(routeId)", "return routeStore.removeFavoriteRoute(routeId)")

content = content.replace("suspend fun addRecentLocation(location: Location) {", "suspend fun addRecentLocation(location: Location): Result<Unit> {")
content = content.replace("routeStore.addRecentLocation(location)", "return routeStore.addRecentLocation(location)")

content = content.replace("suspend fun saveLastFromLocation(location: Location) {", "suspend fun saveLastFromLocation(location: Location): Result<Unit> {")
content = content.replace("routeStore.saveLastFromLocation(location)", "return routeStore.saveLastFromLocation(location)")
content = content.replace("suspend fun saveLastToLocation(location: Location) {", "suspend fun saveLastToLocation(location: Location): Result<Unit> {")
content = content.replace("routeStore.saveLastToLocation(location)", "return routeStore.saveLastToLocation(location)")

content = content.replace("suspend fun clearAllData() {", "suspend fun clearAllData(): Result<Unit> {")
content = content.replace("routeStore.clearAllData()", "return routeStore.clearAllData()")

with open("app/src/main/java/com/hsl/wear/data/repository/TransitRepository.kt", "w") as f:
    f.write(content)

