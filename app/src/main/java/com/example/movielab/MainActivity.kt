package com.example.movielab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.movielab.ui.MovieApp
import com.example.movielab.ui.MovieLabTheme

/**
 * Hand-written reference activity, not a model sample.
 * enableEdgeToEdge from ComponentActivity runs before super.onCreate and before setContent.
 * The manifest does not set screenOrientation or configChanges.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MovieLabTheme {
                MovieApp()
            }
        }
    }
}
