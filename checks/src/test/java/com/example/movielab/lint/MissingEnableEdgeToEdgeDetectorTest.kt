package com.example.movielab.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/** Gold corpus for enableEdgeToEdge. Not a model sample. */
class MissingEnableEdgeToEdgeDetectorTest {
    @Test
    fun componentActivityExtensionBeforeSetContentIsClean() {
        lint().files(
            *stubs(),
            activity(
                """
                class Host : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        enableEdgeToEdge()
                        super.onCreate(savedInstanceState)
                        setContent { }
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun missingCallIsReported() {
        lint().files(
            *stubs(),
            activity(
                """
                class Host : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContent { }
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
            .expectContains("enableEdgeToEdge")
    }

    @Test
    fun callAfterSetContentIsReported() {
        lint().files(
            *stubs(),
            activity(
                """
                class Host : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContent { }
                        enableEdgeToEdge()
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
            .expectContains("before setContent")
    }

    @Test
    fun windowCompatCallIsClean() {
        lint().files(
            *stubs(),
            activity(
                """
                class Host : android.app.Activity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        androidx.core.view.WindowCompat.enableEdgeToEdge(window)
                        super.onCreate(savedInstanceState)
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun setDecorFitsSystemWindowsDoesNotCount() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/androidx/core/view/WindowCompatExtra.kt",
                """
                package androidx.core.view
                fun setDecorFitsSystemWindows(window: Any?, fits: Boolean) = Unit
                """.trimIndent(),
            ),
            activity(
                """
                class Host : android.app.Activity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        androidx.core.view.setDecorFitsSystemWindows(window, false)
                        super.onCreate(savedInstanceState)
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun localFunctionWithTheSameNameDoesNotCount() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/Host.kt",
                """
                package test.pkg
                import androidx.activity.ComponentActivity
                import androidx.activity.compose.setContent
                fun enableEdgeToEdge() = Unit
                class Host : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        enableEdgeToEdge()
                        super.onCreate(savedInstanceState)
                        setContent { }
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun sameClassHelperCalledBeforeSetContentIsClean() {
        lint().files(
            *stubs(),
            activity(
                """
                class Host : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        install()
                        super.onCreate(savedInstanceState)
                        setContent { }
                    }
                    private fun install() { enableEdgeToEdge() }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun superclassSourceCountsWhenSuperIsCalled() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/Base.kt",
                """
                package test.pkg
                import androidx.activity.ComponentActivity
                import androidx.activity.enableEdgeToEdge
                open class BaseActivity : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        enableEdgeToEdge()
                        super.onCreate(savedInstanceState)
                    }
                }
                """.trimIndent(),
            ),
            kotlin(
                "src/main/java/test/pkg/Child.kt",
                """
                package test.pkg
                import androidx.activity.compose.setContent
                class ChildActivity : BaseActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContent { }
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun skippingSuperDoesNotInheritTheCall() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/Base.kt",
                """
                package test.pkg
                import androidx.activity.ComponentActivity
                import androidx.activity.enableEdgeToEdge
                open class BaseActivity : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        enableEdgeToEdge()
                        super.onCreate(savedInstanceState)
                    }
                }
                """.trimIndent(),
            ),
            kotlin(
                "src/main/java/test/pkg/Child.kt",
                """
                package test.pkg
                import androidx.activity.compose.setContent
                class ChildActivity : BaseActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        setContent { }
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun abstractActivityIsNotReported() {
        lint().files(
            *stubs(),
            activity(
                """
                abstract class BaseHost : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        super.onCreate(savedInstanceState)
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun importedAliasOfEnableIsClean() {
        lint().files(
            *stubs(),
            kotlin(
                "src/main/java/test/pkg/AliasHost.kt",
                """
                package test.pkg
                import androidx.activity.ComponentActivity
                import androidx.activity.enableEdgeToEdge as edge
                import androidx.activity.compose.setContent
                class Host : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        edge()
                        super.onCreate(savedInstanceState)
                        setContent { }
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun suppressIsClean() {
        lint().files(
            *stubs(),
            activity(
                """
                @Suppress("MissingEnableEdgeToEdge")
                class Host : ComponentActivity() {
                    override fun onCreate(savedInstanceState: android.os.Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContent { }
                    }
                }
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.missingEdgeToEdge)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    private fun activity(body: String): TestFile {
        return kotlin(
            "src/main/java/test/pkg/Host.kt",
            """
            package test.pkg
            import androidx.activity.ComponentActivity
            import androidx.activity.enableEdgeToEdge
            import androidx.activity.compose.setContent
            $body
            """.trimIndent(),
        )
    }

    private fun stubs(): Array<TestFile> = arrayOf(
        kotlin(
            "src/main/java/androidx/activity/ComponentActivity.kt",
            """
            package androidx.activity
            import android.app.Activity
            open class ComponentActivity : Activity()
            fun ComponentActivity.enableEdgeToEdge() = Unit
            """.trimIndent(),
        ),
        kotlin(
            "src/main/java/androidx/activity/compose/Content.kt",
            """
            package androidx.activity.compose
            import androidx.activity.ComponentActivity
            fun ComponentActivity.setContent(content: () -> Unit) = Unit
            """.trimIndent(),
        ),
        kotlin(
            "src/main/java/androidx/core/view/WindowCompat.kt",
            """
            package androidx.core.view
            object WindowCompat {
                fun enableEdgeToEdge(window: Any?) = Unit
            }
            """.trimIndent(),
        ),
    )
}
