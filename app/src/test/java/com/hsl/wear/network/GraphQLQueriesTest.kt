package com.hsl.wear.network

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class GraphQLQueriesTest {
    @Test
    fun getCurrentLocalTime_usesAsciiDigitsInArabicLocale() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale("ar"))
            val (date, time) = GraphQLQueries.getCurrentLocalTime()
            
            // check that digits inside the date/time strings are ASCII
            // Specifically, there shouldn't be Arabic digits (١, ٢, ٣, etc)
            val arabicDigits = listOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
            arabicDigits.forEach { digit ->
                assertTrue("Date contains non-ASCII digit $digit: $date", !date.contains(digit))
                assertTrue("Time contains non-ASCII digit $digit: $time", !time.contains(digit))
            }
            
            assertTrue("Date doesn't match expected ASCII format", date.matches(Regex("""\d{4}-\d{2}-\d{2}""")))
            assertTrue("Time doesn't match expected ASCII format", time.matches(Regex("""\d{2}:\d{2}:\d{2}""")))
            
        } finally {
            Locale.setDefault(originalLocale)
        }
    }
}
