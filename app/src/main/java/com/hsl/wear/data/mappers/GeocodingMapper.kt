package com.hsl.wear.data.mappers

import com.hsl.wear.data.models.AutocompleteResult
import com.hsl.wear.data.models.GeocodingFeature
import com.hsl.wear.data.models.LocationType

/**
 * Mapper for converting geocoding API responses to domain models.
 * Consolidates duplicate mapping logic from HslRepository.
 */
object GeocodingMapper {

    /**
     * Maps a list of GeocodingFeatures to AutocompleteResults.
     * @param features List of features from geocoding API
     * @param idPrefix Prefix for generating IDs (e.g., "search", "current_location")
     * @param defaultName Default name if feature has no label
     * @return List of AutocompleteResult objects
     */
    fun mapFeaturesToAutocompleteResults(
        features: List<GeocodingFeature>,
        idPrefix: String = "location",
        defaultName: String = "Unknown Location"
    ): List<AutocompleteResult> {
        return features.mapNotNull { feature ->
            mapFeatureToAutocompleteResult(feature, idPrefix, defaultName)
        }
    }

    /**
     * Maps a single GeocodingFeature to an AutocompleteResult.
     * @param feature Feature from geocoding API
     * @param idPrefix Prefix for generating ID
     * @param defaultName Default name if feature has no label
     * @return AutocompleteResult or null if coordinates are invalid
     */
    fun mapFeatureToAutocompleteResult(
        feature: GeocodingFeature,
        idPrefix: String = "location",
        defaultName: String = "Unknown Location"
    ): AutocompleteResult? {
        val coords = feature.geometry.coordinates
        if (coords.size < 2) return null

        val lon = coords[0]
        val lat = coords[1]
        val props = feature.properties

        // Determine the display name (full label for search)
        val name = props.label
            ?: props.name
            ?: buildString {
                props.street?.let { append(it) }
                props.housenumber?.let {
                    if (isNotEmpty()) append(" ")
                    append(it)
                }
            }.trim().ifEmpty { defaultName }

        // Determine the short name (for display)
        val shortName = props.name ?: props.street?.let { street ->
            props.housenumber?.let { "$street $it" } ?: street
        }

        // Determine location type based on layer
        val locationType = when (props.layer) {
            "stop", "station" -> LocationType.STOP
            else -> LocationType.ADDRESS
        }

        // Generate ID
        val id = when (idPrefix) {
            "search" -> "${lat},${lon}"
            "current_location" -> "current_location_${lat}_${lon}"
            else -> "${idPrefix}_${lat}_${lon}"
        }

        return AutocompleteResult(
            id = id,
            name = name,
            lat = lat,
            lon = lon,
            type = locationType,
            stopCode = null,
            lines = emptyList(),
            shortName = shortName
        )
    }
}
