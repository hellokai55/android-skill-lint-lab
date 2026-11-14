package com.example.movielab.lint

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

/**
 * Lab issues mapped from a frozen Android skills snapshot.
 *
 * These are not built-in Android Lint checks and they are not official
 * Google rules packaged by the Android Gradle Plugin. Team conventions
 * (analytics, routing style, WebView bridges) are intentionally absent.
 *
 * Registry severity is warning. App modules in this repo raise the same ids
 * to error in their own lint.xml once a module is ready for that gate.
 */
object MovieLabIssues {
    private const val REF =
        "Frozen reference: android/skills v1.0.21, commit 5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d. " +
            "Vendored under third_party/android-skills. This check is a lab mapping, not an upstream lint rule."

    val screenOrientation: Issue = Issue.create(
        id = "AdaptiveScreenOrientationLock",
        briefDescription = "screenOrientation locks out adaptive layout",
        explanation = """
            The frozen adaptive skill says: "Never add android:configChanges or android:screenOrientation to AndroidManifest.xml to suppress Activity recreation or lock orientation."

            This check reports every android:screenOrientation value, including values that do not pin a single orientation. The skill forbids adding the attribute. Camera or video activities that intentionally lock orientation need an explicit suppression and a review, not a silent allowlist.

            $REF
            Source: jetpack-compose/adaptive/SKILL.md, Step 6.
        """.trimIndent(),
        category = Category.CORRECTNESS,
        priority = 8,
        severity = Severity.WARNING,
        implementation = Implementation(
            ManifestAdaptationDetector::class.java,
            Scope.MANIFEST_SCOPE,
        ),
    )

    val configChanges: Issue = Issue.create(
        id = "AdaptiveConfigChangesOptOut",
        briefDescription = "configChanges opts out of adaptive recreation",
        explanation = """
            The frozen adaptive skill says never to add android:configChanges to suppress Activity recreation. Opting out hides state-loss bugs and skips adaptive resource reload.

            To limit false positives, only these tokens are reported: orientation, screenSize, screenLayout, smallestScreenSize, density. Other tokens such as keyboard, locale, fontScale, and uiMode are not reported. That is narrower than the skill's absolute wording. Unknown tokens are not reported either.

            $REF
            Source: jetpack-compose/adaptive/SKILL.md, Step 6.
        """.trimIndent(),
        category = Category.CORRECTNESS,
        priority = 8,
        severity = Severity.WARNING,
        implementation = Implementation(
            ManifestAdaptationDetector::class.java,
            Scope.MANIFEST_SCOPE,
        ),
    )

    val unsaveableScroll: Issue = Issue.create(
        id = "UnsaveableScrollRemember",
        briefDescription = "remember bypasses the scroll state Saver",
        explanation = """
            The frozen adaptive skill says plain remember { LazyListState() }, remember { LazyGridState() }, and remember { ScrollState(...) } do not save scroll position across configuration changes, because they bypass the state class Saver.

            Replacements named by the skill: rememberLazyListState(), rememberLazyGridState(), rememberLazyStaggeredGridState(), rememberScrollState().

            rememberSaveable { LazyListState() } without an explicit saver argument is also reported. An explicit saver = argument is not reported; this check does not validate that the saver is correct. A factory that hides the constructor, such as remember { freshListState() }, is not reported.

            Same simple names in another package are not reported. Resolution is required; an unresolved call is not reported.

            $REF
            Source: jetpack-compose/adaptive/SKILL.md, Step 6.2.
        """.trimIndent(),
        category = Category.CORRECTNESS,
        priority = 8,
        severity = Severity.WARNING,
        implementation = Implementation(
            UnsaveableScrollRememberDetector::class.java,
            Scope.JAVA_FILE_SCOPE,
        ),
    )

    val bannedPaneScaffold: Issue = Issue.create(
        id = "AdaptivePaneScaffoldBanned",
        briefDescription = "Use a Navigation 3 scene strategy for multi-pane layouts",
        explanation = """
            The frozen adaptive skill says: "You must use the Navigation 3 SceneStrategy approach to implement multi-pane layouts. Do not use ListDetailPaneScaffold or SupportingPaneScaffold."

            This check reports calls whose resolved function is the composable ListDetailPaneScaffold or SupportingPaneScaffold in package androidx.compose.material3.adaptive.layout. It does not report ListDetailSceneStrategy, rememberListDetailSceneStrategy, SupportingPaneSceneStrategy, or *Defaults helpers. A same-named function in another package is not reported. Unresolved simple names are not reported. The edge-to-edge skill mentions ListDetailPaneScaffold only as an inset example; the ban comes from the adaptive skill.

            $REF
            Source: jetpack-compose/adaptive/SKILL.md, Step 3.
        """.trimIndent(),
        category = Category.CORRECTNESS,
        priority = 8,
        severity = Severity.WARNING,
        implementation = Implementation(
            AdaptivePaneScaffoldDetector::class.java,
            Scope.JAVA_FILE_SCOPE,
        ),
    )

    val missingEdgeToEdge: Issue = Issue.create(
        id = "MissingEnableEdgeToEdge",
        briefDescription = "Activity does not call enableEdgeToEdge before setContent",
        explanation = """
            The frozen edge-to-edge skill says to add enableEdgeToEdge before setContent in onCreate for each Activity that does not already call it. The checklist asks whether every Activity calls enableEdgeToEdge().

            Both of these count: androidx.activity.enableEdgeToEdge (ComponentActivity extension) and androidx.core.view.WindowCompat.enableEdgeToEdge. A same-named function in the app does not count. WindowCompat.setDecorFitsSystemWindows does not count; the skill names enableEdgeToEdge.

            A one-level private helper in the same class counts. A source superclass counts only when this class calls super.onCreate() or does not override onCreate. Abstract classes are skipped. Classes in android.app, androidx.activity, androidx.appcompat, androidx.fragment, and androidx.core are skipped so SDK stubs are not treated as app activities. Test sources are skipped. Classes outside this module whose bytecode body is invisible are not credited; that can false-positive a subclass of a library base activity.

            The skill also discusses icon contrast when enableEdgeToEdge comes from WindowCompat. This check does not enforce that contrast rule.

            $REF
            Source: system/edge-to-edge/SKILL.md, Step 2 and the checklist.
        """.trimIndent(),
        category = Category.CORRECTNESS,
        priority = 8,
        severity = Severity.WARNING,
        implementation = Implementation(
            MissingEnableEdgeToEdgeDetector::class.java,
            Scope.JAVA_FILE_SCOPE,
        ),
    )

    val all: List<Issue> = listOf(
        screenOrientation,
        configChanges,
        unsaveableScroll,
        bannedPaneScaffold,
        missingEdgeToEdge,
    )
}
