with open("app/src/main/java/com/hsl/wear/data/repository/HslRepository.kt", "r") as f:
    content = f.read()

import re

# Fix planRoute
content = re.sub(
r"""    suspend fun planRoute\(
        fromLocation: Location,
        toLocation: Location
    \): Result<List<Itinerary>> \{
        return ErrorMessages.safeExecute\(
            tag = "HslRepository",
            operation = "planRoute from \(\$\{fromLocation\.lat\}, \$\{fromLocation\.lon\}\) to \(\$\{toLocation\.lat\}, \$\{toLocation\.lon\}\)"
        \) \{
            val \(date, time\) = GraphQLQueries.getCurrentLocalTime\(\)
            val query = GraphQLQueries\.planRoute\(
                fromLat = fromLocation\.lat,
                fromLon = fromLocation\.lon,
                toLat = toLocation\.lat,
                toLon = toLocation\.lon
            \)

            val result = graphQLClient\.executeQuery<com\.hsl\.wear\.data\.models\.PlanResponse>\(
                endpoint = NetworkConstants\.HSL_ENDPOINT,
                query = query\s*
            \)""", 
r"""    suspend fun planRoute(
        fromLocation: Location,
        toLocation: Location
    ): Result<List<Itinerary>> {
        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "planRoute from (${fromLocation.lat}, ${fromLocation.lon}) to (${toLocation.lat}, ${toLocation.lon})"
        ) {
            val query = GraphQLQueries.planRoute(
                fromLat = fromLocation.lat,
                fromLon = fromLocation.lon,
                toLat = toLocation.lat,
                toLon = toLocation.lon
            )

            val (date, time) = GraphQLQueries.getCurrentLocalTime()

            val result = graphQLClient.executeQuery<com.hsl.wear.data.models.PlanResponse>(
                endpoint = NetworkConstants.HSL_ENDPOINT,
                query = query,
                variables = mapOf("date" to date, "time" to time)
            )""", content)

with open("app/src/main/java/com/hsl/wear/data/repository/HslRepository.kt", "w") as f:
    f.write(content)
