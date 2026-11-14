package com.example.movielab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.movielab.R
import com.example.movielab.data.CatalogStatus
import com.example.movielab.data.Movie
import com.example.movielab.data.sampleMovies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListPane(
    status: CatalogStatus,
    onStatus: (CatalogStatus) -> Unit,
    selectedMovieId: String?,
    onOpenMovie: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(stringResource(R.string.movie_list_title))
                            Text(
                                text = stringResource(R.string.movie_list_subtitle),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    },
                )
                CatalogModeRow(
                    status = status,
                    onStatus = onStatus,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                    ),
                )
            }
        },
    ) { innerPadding ->
        when (status) {
            CatalogStatus.Loading -> StatusMessage(
                title = stringResource(R.string.loading_message),
                body = stringResource(R.string.offline_note),
                innerPadding = innerPadding,
                indicator = true,
            )
            CatalogStatus.Empty -> StatusMessage(
                title = stringResource(R.string.empty_message),
                body = stringResource(R.string.offline_note),
                innerPadding = innerPadding,
            )
            CatalogStatus.Error -> StatusMessage(
                title = stringResource(R.string.error_message),
                body = stringResource(R.string.error_detail),
                innerPadding = innerPadding,
                onRetry = { onStatus(CatalogStatus.Ready) },
            )
            CatalogStatus.Ready -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
                state = listState,
                contentPadding = innerPadding,
            ) {
                items(items = sampleMovies, key = { it.id }) { movie ->
                    MovieRow(
                        movie = movie,
                        selected = movie.id == selectedMovieId,
                        onOpenMovie = onOpenMovie,
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogModeRow(
    status: CatalogStatus,
    onStatus: (CatalogStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CatalogStatus.entries.forEach { mode ->
            FilterChip(
                selected = status == mode,
                onClick = { onStatus(mode) },
                label = { Text(mode.label()) },
            )
        }
    }
}

@Composable
private fun CatalogStatus.label(): String = when (this) {
    CatalogStatus.Ready -> stringResource(R.string.status_ready)
    CatalogStatus.Loading -> stringResource(R.string.status_loading)
    CatalogStatus.Empty -> stringResource(R.string.status_empty)
    CatalogStatus.Error -> stringResource(R.string.status_error)
}

@Composable
private fun MovieRow(
    movie: Movie,
    selected: Boolean,
    onOpenMovie: (String) -> Unit,
) {
    ListItem(
        modifier = Modifier
            .clickable { onOpenMovie(movie.id) }
            .semantics { this.selected = selected },
        colors = ListItemDefaults.colors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        leadingContent = {
            PosterPlaceholder(
                seed = movie.id,
                compact = true,
                modifier = Modifier.size(width = 56.dp, height = 84.dp),
            )
        },
        headlineContent = { Text(movie.title) },
        supportingContent = {
            Text("${movie.year} · ${movie.director} · ${movie.genres}")
        },
        trailingContent = { Text(movie.rating) },
    )
}

@Composable
private fun StatusMessage(
    title: String,
    body: String,
    innerPadding: PaddingValues,
    indicator: Boolean = false,
    onRetry: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (indicator) {
                CircularProgressIndicator(Modifier.padding(bottom = 16.dp))
            }
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Text(
                text = body,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}
