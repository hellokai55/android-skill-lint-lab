package com.example.movielab.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.movielab.data.CatalogStatus
import com.example.movielab.data.movieById
import com.example.movielab.navigation.MovieDetailRoute
import com.example.movielab.navigation.MovieListRoute

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MovieApp() {
    val backStack = rememberNavBackStack(MovieListRoute)
    var statusName by rememberSaveable { mutableStateOf(CatalogStatus.Ready.name) }
    val status = CatalogStatus.valueOf(statusName)

    val windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    val directive = remember(windowAdaptiveInfo) {
        calculatePaneScaffoldDirective(windowAdaptiveInfo)
            .copy(horizontalPartitionSpacerSize = 0.dp)
    }
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(directive = directive)

    val onBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
    val selectedMovieId = (backStack.lastOrNull() as? MovieDetailRoute)?.movieId

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = onBack,
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider {
            entry<MovieListRoute>(
                clazzContentKey = { "movie-list" },
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = { MovieDetailPlaceholder() },
                ),
            ) {
                MovieListPane(
                    status = status,
                    onStatus = { statusName = it.name },
                    selectedMovieId = selectedMovieId,
                    onOpenMovie = { movieId -> openMovie(backStack, movieId) },
                )
            }
            entry<MovieDetailRoute>(
                clazzContentKey = { route -> "movie-detail:${route.movieId}" },
                metadata = ListDetailSceneStrategy.detailPane(),
            ) { route ->
                MovieDetailPane(
                    movie = movieById(route.movieId),
                    onBack = onBack,
                )
            }
        },
    )
}

private fun openMovie(
    backStack: NavBackStack<NavKey>,
    movieId: String,
) {
    val top = backStack.lastOrNull()
    when {
        top is MovieDetailRoute && top.movieId == movieId -> Unit
        top is MovieDetailRoute -> backStack[backStack.lastIndex] = MovieDetailRoute(movieId)
        else -> backStack.add(MovieDetailRoute(movieId))
    }
}
