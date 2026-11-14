@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class,
)

package com.example.movielab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.movielab.data.CatalogMode
import com.example.movielab.data.Movie
import com.example.movielab.data.Movies
import com.example.movielab.data.presentCatalog
import kotlinx.serialization.Serializable

/**
 * Hand-written reference UI. Phone vs expanded width follows
 * calculatePaneScaffoldDirective used by rememberListDetailSceneStrategy:
 * Compact and Medium stay one pane; Expanded and wider show list and detail.
 * That is the frozen Material recipe, not a custom breakpoint.
 */

@Serializable
private data object MovieList : NavKey

@Serializable
private data class MovieDetailKey(val movieId: String) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MovieApp() {
    val backStack = rememberNavBackStack(MovieList)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    val singlePane = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
        .maxHorizontalPartitions < 2

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetailStrategy),
        entryProvider = entryProvider {
            entry<MovieList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("选择一部电影")
                        }
                    },
                ),
            ) {
                MovieListPane(
                    onOpen = { id ->
                        while (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                        backStack.add(MovieDetailKey(id))
                    },
                )
            }
            entry<MovieDetailKey>(
                metadata = ListDetailSceneStrategy.detailPane(),
            ) { key ->
                MovieDetailPane(
                    movieId = key.movieId,
                    showBack = singlePane,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieListPane(onOpen: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var modeName by rememberSaveable { mutableStateOf(CatalogMode.Content.name) }
    val mode = CatalogMode.entries.firstOrNull { it.name == modeName } ?: CatalogMode.Content
    val presentation = presentCatalog(mode, query, Movies.all)
    val gridState = rememberLazyGridState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopAppBar(title = { Text("本地片单") }) },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 168.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .imePadding(),
            contentPadding = innerPadding,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "search", contentType = "chrome", span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    label = { Text("搜索标题、类型或简介") },
                    singleLine = true,
                )
            }
            item(key = "modes", contentType = "chrome", span = { GridItemSpan(maxLineSpan) }) {
                ModeRow(
                    mode = mode,
                    onMode = { modeName = it.name },
                )
            }
            if (mode == CatalogMode.Loading) {
                item(key = "loading", contentType = "status", span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Text(presentation.message.orEmpty(), modifier = Modifier.padding(top = 12.dp))
                    }
                }
            } else if (presentation.movies.isEmpty()) {
                item(key = "message", contentType = "status", span = { GridItemSpan(maxLineSpan) }) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                        Text(presentation.message.orEmpty(), style = MaterialTheme.typography.titleMedium)
                        if (mode == CatalogMode.Error) {
                            Button(onClick = { modeName = CatalogMode.Content.name }, modifier = Modifier.padding(top = 12.dp)) {
                                Text("重试")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = presentation.movies,
                    key = { it.id },
                    contentType = { "movie" },
                ) { movie ->
                    MovieCard(movie, onOpen)
                }
            }
        }
    }
}

@Composable
private fun ModeRow(mode: CatalogMode, onMode: (CatalogMode) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp)) {
        Text("演示状态（本地，无网络）", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            CatalogMode.entries.forEach { candidate ->
                FilterChip(
                    selected = mode == candidate,
                    onClick = { onMode(candidate) },
                    label = {
                        Text(
                            when (candidate) {
                                CatalogMode.Content -> "内容"
                                CatalogMode.Loading -> "加载"
                                CatalogMode.Empty -> "空"
                                CatalogMode.Error -> "错误"
                            },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun MovieCard(movie: Movie, onOpen: (String) -> Unit) {
    Card(onClick = { onOpen(movie.id) }, modifier = Modifier.padding(horizontal = 12.dp)) {
        Column {
            PlaceholderPoster(title = movie.title, hue = movie.hue, heightDp = 140)
            Column(Modifier.padding(12.dp)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                Text("${movie.year} · ${movie.genre} · ${movie.runtimeMinutes} 分钟", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailPane(movieId: String, showBack: Boolean, onBack: () -> Unit) {
    val movie = Movies.byId(movieId)
    val scroll = rememberScrollState()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(movie?.title ?: "未找到") },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (movie == null) {
                Text("片单里没有这个 id。")
            } else {
                PlaceholderPoster(title = movie.title, hue = movie.hue, heightDp = 240)
                Text("${movie.year} · ${movie.genre} · ${movie.runtimeMinutes} 分钟")
                Text(movie.synopsis, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "海报是按色相在本地绘制的占位图，不是电影海报，也没有版权图片。",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
