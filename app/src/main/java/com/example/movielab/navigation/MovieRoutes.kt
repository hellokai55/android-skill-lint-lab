package com.example.movielab.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object MovieListRoute : NavKey

@Serializable
data class MovieDetailRoute(val movieId: String) : NavKey
