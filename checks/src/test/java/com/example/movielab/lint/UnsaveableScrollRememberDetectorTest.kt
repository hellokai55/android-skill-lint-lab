package com.example.movielab.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/** Gold corpus for scroll remember. Not a model sample. */
class UnsaveableScrollRememberDetectorTest {
    @Test
    fun rememberLazyListStateConstructorIsReported() {
        lint().files(*stubs(), sample("val state = remember { LazyListState() }"))
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
            .expectContains("LazyListState")
    }

    @Test
    fun rememberWithKeyStillReported() {
        lint().files(*stubs(), sample("val state = remember(1) { LazyGridState() }"))
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun scrollStateConstructorIsReported() {
        lint().files(*stubs(), sample("val state = remember { ScrollState(0) }"))
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun staggeredConstructorIsReported() {
        lint().files(*stubs(), sample("val state = remember { LazyStaggeredGridState() }"))
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun rememberSaveableWithoutSaverIsReported() {
        lint().files(*stubs(), sample("val state = rememberSaveable { LazyListState() }"))
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun rememberSaveableWithExplicitSaverIsClean() {
        lint().files(*stubs(), sample("val state = rememberSaveable(saver = Any()) { LazyListState() }"))
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun officialFactoriesAreClean() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/Factories.kt",
                """
                package test.pkg
                fun rememberLazyListState() = Any()
                fun rememberLazyGridState() = Any()
                fun rememberLazyStaggeredGridState() = Any()
                fun rememberScrollState() = Any()
                fun demo() {
                    rememberLazyListState()
                    rememberLazyGridState()
                    rememberLazyStaggeredGridState()
                    rememberScrollState()
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun rememberOfOtherStateIsClean() {
        lint().files(*stubs(), sample("val state = remember { mutableStateOf(0) }"))
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun importedAliasOfRememberIsReported() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/Alias.kt",
                """
                package test.pkg
                import androidx.compose.runtime.remember as keep
                import androidx.compose.foundation.lazy.LazyListState
                fun demo() {
                    val state = keep { LazyListState() }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun localRememberAndLocalStateAreClean() {
        lint().files(
            kotlin(
                "src/main/java/test/pkg/Local.kt",
                """
                package test.pkg
                class LazyListState
                fun remember(block: () -> Any) = block()
                fun demo() = remember { LazyListState() }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun composeRememberOfUnrelatedScrollStateIsClean() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/OtherState.kt",
                """
                package test.pkg
                import androidx.compose.runtime.remember
                class ScrollState
                fun demo() = remember { ScrollState() }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun suppressOnTheFunctionIsClean() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/Suppressed.kt",
                """
                package test.pkg
                import androidx.compose.runtime.remember
                import androidx.compose.foundation.lazy.LazyListState
                @Suppress("UnsaveableScrollRemember")
                fun demo() {
                    val state = remember { LazyListState() }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.unsaveableScroll)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    private fun sample(body: String): TestFile {
        return kotlin(
            "src/main/java/test/pkg/Sample.kt",
            """
            package test.pkg
            import androidx.compose.runtime.remember
            import androidx.compose.runtime.saveable.rememberSaveable
            import androidx.compose.foundation.lazy.LazyListState
            import androidx.compose.foundation.lazy.grid.LazyGridState
            import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
            import androidx.compose.foundation.ScrollState
            fun mutableStateOf(value: Int) = value
            fun demo() {
                $body
            }
            """.trimIndent(),
        )
    }

    private fun stubs(): Array<TestFile> = arrayOf(
        kotlin(
            "src/main/java/androidx/compose/runtime/RememberStub.kt",
            """
            package androidx.compose.runtime
            fun remember(vararg keys: Any?, calculation: () -> Any): Any = calculation()
            """.trimIndent(),
        ),
        kotlin(
            "src/main/java/androidx/compose/runtime/saveable/RememberSaveableStub.kt",
            """
            package androidx.compose.runtime.saveable
            fun rememberSaveable(
                vararg inputs: Any?,
                saver: Any? = null,
                key: String? = null,
                init: () -> Any,
            ): Any = init()
            """.trimIndent(),
        ),
        kotlin(
            "src/main/java/androidx/compose/foundation/lazy/LazyListState.kt",
            """
            package androidx.compose.foundation.lazy
            class LazyListState
            """.trimIndent(),
        ),
        kotlin(
            "src/main/java/androidx/compose/foundation/lazy/grid/LazyGridState.kt",
            """
            package androidx.compose.foundation.lazy.grid
            class LazyGridState
            """.trimIndent(),
        ),
        kotlin(
            "src/main/java/androidx/compose/foundation/lazy/staggeredgrid/LazyStaggeredGridState.kt",
            """
            package androidx.compose.foundation.lazy.staggeredgrid
            class LazyStaggeredGridState
            """.trimIndent(),
        ),
        kotlin(
            "src/main/java/androidx/compose/foundation/ScrollState.kt",
            """
            package androidx.compose.foundation
            class ScrollState(val initial: Int = 0)
            """.trimIndent(),
        ),
    )
}
