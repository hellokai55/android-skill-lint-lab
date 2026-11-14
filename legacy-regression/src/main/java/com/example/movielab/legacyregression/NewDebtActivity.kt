package com.example.movielab.legacyregression

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Hand-constructed NEW debt. NEW_DEBT_MARKER.
 * The activity itself calls enableEdgeToEdge before setContent and the manifest
 * does not lock orientation. The only mapped violation is remember { ScrollState(0) }.
 * There is no baseline in this module, so lintDebug is expected to exit non-zero.
 * This file is not model output.
 */
class NewDebtActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { NewDebtScreen() }
    }
}

@Composable
private fun NewDebtScreen() {
    val scroll = remember { ScrollState(0) }
    Text("新增违规样本，滚动初始值 ${scroll.value}。NEW_DEBT_MARKER")
}
