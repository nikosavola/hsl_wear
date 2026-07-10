package com.hsl.wear.network

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Locale

class GraphQLQueriesTest {

    private var originalLocale: Locale = Locale.getDefault()

    @Before
    fun setup() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun teardown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun planRoute_withDifferentLocales_generatesCorrectEnglishFormat() {
        // Set to a locale that might use non-ASCII digits, e.g., Thai
        val thLocale = Locale("th", "TH")
        Locale.setDefault(thLocale)

        val query = GraphQLQueries.planRoute(60.1, 24.9, 60.2, 24.8)
        
        // Assert that the generated date and time are in standard ASCII digits
        // i.e., date: "202..." instead of Thai numerals
        val regex = Regex("date: \"\\d{4}-\\d{2}-\\d{2}\"")
        assertTrue(query.contains(regex))
    }

    @Test
    fun getTripStatus_returnsCorrectQuery() {
        val query = GraphQLQueries.getTripStatus()
        assertTrue(query.contains("query GetTripStatus("))
        assertTrue(query.contains("\$tripId: String!"))
    }
}
