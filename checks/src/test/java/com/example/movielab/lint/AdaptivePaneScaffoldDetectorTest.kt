package com.example.movielab.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/** Gold corpus for the pane-scaffold ban. Not a model sample. */
class AdaptivePaneScaffoldDetectorTest {
    @Test
    fun listDetailPaneScaffoldCallIsReported() {
        lint().files(*stubs(), caller("ListDetailPaneScaffold()"))
            .issues(MovieLabIssues.bannedPaneScaffold)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
            .expectContains("ListDetailPaneScaffold")
    }

    @Test
    fun supportingPaneScaffoldCallIsReported() {
        lint().files(*stubs(), caller("SupportingPaneScaffold()"))
            .issues(MovieLabIssues.bannedPaneScaffold)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun importedAliasIsReported() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/Alias.kt",
                """
                package test.pkg
                import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold as OldScaffold
                fun demo() { OldScaffold() }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.bannedPaneScaffold)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun sceneStrategyApisAreClean() {
        lint().files(
            kotlin(
                "src/main/java/androidx/compose/material3/adaptive/navigation3/Strategy.kt",
                """
                package androidx.compose.material3.adaptive.navigation3
                class ListDetailSceneStrategy {
                    companion object {
                        fun listPane() = mapOf("role" to "list")
                        fun detailPane() = mapOf("role" to "detail")
                    }
                }
                fun rememberListDetailSceneStrategy() = ListDetailSceneStrategy()
                fun rememberSupportingPaneSceneStrategy() = Any()
                """.trimIndent(),
            ),
            kotlin(
                "src/main/java/test/pkg/SceneUser.kt",
                """
                package test.pkg
                import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
                import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
                import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
                fun demo() {
                    rememberListDetailSceneStrategy()
                    rememberSupportingPaneSceneStrategy()
                    ListDetailSceneStrategy.listPane()
                    ListDetailSceneStrategy.detailPane()
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.bannedPaneScaffold)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun defaultsHelperIsClean() {
        lint().files(
            kotlin(
                "src/main/java/androidx/compose/material3/adaptive/layout/Defaults.kt",
                """
                package androidx.compose.material3.adaptive.layout
                object ListDetailPaneScaffoldDefaults {
                    fun adaptStrategies() = Any()
                }
                """.trimIndent(),
            ),
            kotlin(
                "src/main/java/test/pkg/DefaultsUser.kt",
                """
                package test.pkg
                import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
                fun demo() = ListDetailPaneScaffoldDefaults.adaptStrategies()
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.bannedPaneScaffold)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun sameNameInAnotherPackageIsClean() {
        lint().files(
            kotlin(
                "src/main/java/test/pkg/Homonym.kt",
                """
                package test.pkg
                fun ListDetailPaneScaffold() = Unit
                fun SupportingPaneScaffold() = Unit
                fun demo() {
                    ListDetailPaneScaffold()
                    SupportingPaneScaffold()
                    val label = "ListDetailPaneScaffold"
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.bannedPaneScaffold)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun suppressIsClean() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/Suppressed.kt",
                """
                package test.pkg
                import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
                @Suppress("AdaptivePaneScaffoldBanned")
                fun demo() { SupportingPaneScaffold() }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.bannedPaneScaffold)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    private fun caller(expression: String): TestFile {
        return kotlin(
            "src/main/java/test/pkg/Caller.kt",
            """
            package test.pkg
            import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
            import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
            fun demo() { $expression }
            """.trimIndent(),
        )
    }

    private fun stubs(): Array<TestFile> = arrayOf(
        kotlin(
            "src/main/java/androidx/compose/material3/adaptive/layout/Scaffolds.kt",
            """
            package androidx.compose.material3.adaptive.layout
            fun ListDetailPaneScaffold() = Unit
            fun SupportingPaneScaffold() = Unit
            """.trimIndent(),
        ),
    )
}
