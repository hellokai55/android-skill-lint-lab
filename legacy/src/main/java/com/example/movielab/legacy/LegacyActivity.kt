package com.example.movielab.legacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Hand-constructed legacy sample. It is not model output and it is not an
 * official example. The checked-in baseline is generated from this file;
 * new violations outside that baseline are supposed to fail the build.
 */
class LegacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LegacyScreen() }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun LegacyScreen() {
    val listState = remember { LazyListState() }
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
    ListDetailPaneScaffold(
        directive = directive,
        value = ThreePaneScaffoldValue(
            PaneAdaptedValue.Expanded,
            PaneAdaptedValue.Expanded,
            PaneAdaptedValue.Hidden,
        ),
        listPane = {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                items(listOf("人工构造甲", "人工构造乙", "人工构造丙")) { title ->
                    Text(title)
                }
            }
        },
        detailPane = {
            Column {
                Text("人工构造的旧双栏写法")
                Text("历史债务应留在 baseline 里，而不是被改检测器抹掉。")
            }
        },
    )
}
