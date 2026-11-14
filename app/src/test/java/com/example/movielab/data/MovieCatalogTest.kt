package com.example.movielab.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieCatalogTest {
    @Test
    fun catalogHasAtLeastThirtyMovies() {
        assertTrue(sampleMovies.size >= 30)
    }

    @Test
    fun movieIdsAreUniqueAndLookupWorks() {
        val ids = sampleMovies.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        sampleMovies.forEach { movie ->
            assertTrue(movie.title.isNotBlank())
            assertTrue(movie.director.isNotBlank())
            assertTrue(movie.genres.isNotBlank())
            assertTrue(movie.synopsis.isNotBlank())
            assertTrue(movie.rating.isNotBlank())
            assertTrue(movie.year in 1900..2100)
            assertTrue(movie.runtimeMinutes in 1..400)
            assertEquals(movie, movieById(movie.id))
        }
        assertEquals(null, movieById("missing"))
    }

    @Test
    fun catalogStatusNamesRoundTrip() {
        CatalogStatus.entries.forEach { status ->
            assertEquals(status, CatalogStatus.valueOf(status.name))
        }
        assertNotNull(CatalogStatus.Ready)
        assertNotNull(CatalogStatus.Loading)
        assertNotNull(CatalogStatus.Empty)
        assertNotNull(CatalogStatus.Error)
    }
}
