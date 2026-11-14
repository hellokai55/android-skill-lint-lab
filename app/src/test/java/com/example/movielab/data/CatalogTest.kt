package com.example.movielab.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Local data invariants. Not a model-generation sample. */
class CatalogTest {
    @Test
    fun atLeastThirtyLocalMovies() {
        assertTrue(Movies.all.size >= 30)
    }

    @Test
    fun idsAreUniqueAndStable() {
        assertEquals(Movies.all.size, Movies.all.map { it.id }.toSet().size)
        assertEquals("雾港夜车", Movies.byId("m01")?.title)
        assertNull(Movies.byId("missing"))
    }

    @Test
    fun contentFiltersByTitleGenreOrSynopsis() {
        val byTitle = presentCatalog(CatalogMode.Content, "灯塔", Movies.all)
        assertEquals(listOf("m05"), byTitle.movies.map { it.id })
        assertNull(byTitle.message)

        val byGenre = presentCatalog(CatalogMode.Content, "喜剧", Movies.all)
        assertTrue(byGenre.movies.all { it.genre == "喜剧" })
        assertTrue(byGenre.movies.size >= 3)

        val none = presentCatalog(CatalogMode.Content, "___nomatch___", Movies.all)
        assertTrue(none.movies.isEmpty())
        assertEquals("没有匹配「___nomatch___」的电影。", none.message)
    }

    @Test
    fun simulatedModesAreDeterministicAndOffline() {
        val loading = presentCatalog(CatalogMode.Loading, "灯塔", Movies.all)
        assertTrue(loading.movies.isEmpty())
        assertTrue(loading.message!!.contains("没有网络请求"))

        val empty = presentCatalog(CatalogMode.Empty, "", Movies.all)
        assertTrue(empty.movies.isEmpty())
        assertTrue(empty.message!!.contains("没有网络请求"))

        val error = presentCatalog(CatalogMode.Error, "", Movies.all)
        assertTrue(error.movies.isEmpty())
        assertTrue(error.message!!.contains("没有网络请求"))
    }
}
