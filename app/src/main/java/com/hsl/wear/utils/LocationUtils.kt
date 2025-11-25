package com.hsl.wear.utils

import com.hsl.wear.data.models.AutocompleteResult
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.LocationType

/**
 * Utility object for creating and manipulating Location objects.
 * Consolidates location creation logic from repositories.
 */
object LocationUtils {

    /**
     * Creates a Location from an AutocompleteResult.
     * @param autocompleteResult The autocomplete result to convert
     * @return Location object
     */
    fun createFromAutocompleteResult(autocompleteResult: AutocompleteResult): Location {
        return Location(
            id = autocompleteResult.id,
            name = autocompleteResult.name,
            lat = autocompleteResult.lat,
            lon = autocompleteResult.lon,
            type = autocompleteResult.type,
            stopCode = autocompleteResult.stopCode,
            shortName = autocompleteResult.shortName
        )
    }

    /**
     * Creates a Location representing the current location.
     * @param lat Latitude
     * @param lon Longitude
     * @param name Optional custom name (defaults to "Current Location")
     * @return Location object representing current location
     */
    fun createCurrentLocation(
        lat: Double,
        lon: Double,
        name: String = "Current Location"
    ): Location {
        return Location(
            id = "current_location",
            name = name,
            lat = lat,
            lon = lon,
            type = LocationType.CURRENT_LOCATION
        )
    }

    /**
     * Creates a Location from raw coordinates.
     * @param lat Latitude
     * @param lon Longitude
     * @param name Location name
     * @param id Optional ID (defaults to "custom_${lat}_${lon}")
     * @return Location object
     */
    fun createFromCoordinates(
        lat: Double,
        lon: Double,
        name: String,
        id: String = "custom_${lat}_${lon}"
    ): Location {
        return Location(
            id = id,
            name = name,
            lat = lat,
            lon = lon,
            type = LocationType.ADDRESS
        )
    }
}
