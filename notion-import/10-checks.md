# checks

源码快照 commit：`9441209aadafb1f33c3694643eed5880fa715177`

以下是该目录在该 commit 的完整文本，不是片段。二进制文件只出现在索引里。

## `checks/build.gradle.kts`

sha256 `a01972ef599b9879b4c7d4e7a4a76edf80167e0c3abec55497f05e6a076cf1cb`

```kts
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)
    compileOnly("com.android.tools.external.org-jetbrains:uast:32.5.1")
    compileOnly("com.android.tools.external.com-intellij:kotlin-compiler:32.5.1")
    testImplementation(libs.lint.api)
    testImplementation(libs.lint.checks)
    testImplementation(libs.lint.tests)
    testImplementation(libs.junit)
}

tasks.withType<Test>().configureEach {
    useJUnit()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    val sdk = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
    if (sdk != null) {
        environment("ANDROID_HOME", sdk)
        environment("ANDROID_SDK_ROOT", sdk)
    }
}
```

## `checks/src/main/java/com/example/movielab/lint/AdaptivePaneScaffoldDetector.kt`

sha256 `b73aad2c22c0ed35de93d502079a4c20e3b66678a118402ce6c721fbfb5a8d80`

```kt
package com.example.movielab.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement

class AdaptivePaneScaffoldDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                val name = node.methodName ?: return
                if (name !in BANNED_NAMES) return
                val method = node.resolve() ?: return
                if (method.isConstructor) return
                val owner = method.containingClass?.qualifiedName ?: return
                val pkg = owner.substringBeforeLast('.')
                if (pkg != BANNED_PACKAGE) return
                context.report(
                    MovieLabIssues.bannedPaneScaffold,
                    node,
                    context.getLocation(node),
                    "$name is the pre-Navigation-3 adaptive scaffold. " +
                        "The frozen adaptive skill says to use a Navigation 3 SceneStrategy instead.",
                )
            }
        }
    }

    companion object {
        private const val BANNED_PACKAGE = "androidx.compose.material3.adaptive.layout"
        private val BANNED_NAMES = setOf("ListDetailPaneScaffold", "SupportingPaneScaffold")
    }
}
```

## `checks/src/main/java/com/example/movielab/lint/ManifestAdaptationDetector.kt`

sha256 `6a021fcd3913b6dfb4566ad9bfd382254aa6e78f9a35654f6e826c697dc74f55`

```kt
package com.example.movielab.lint

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import org.w3c.dom.Element

class ManifestAdaptationDetector : Detector(), XmlScanner {
    override fun getApplicableElements(): Collection<String> = listOf("activity", "activity-alias")

    override fun visitElement(context: XmlContext, element: Element) {
        val orientation = element.getAttributeNodeNS(ANDROID_URI, "screenOrientation")
        if (orientation != null && orientation.value.isNotBlank()) {
            context.report(
                MovieLabIssues.screenOrientation,
                element,
                context.getValueLocation(orientation),
                "android:screenOrientation=\"${orientation.value}\" locks or overrides orientation. " +
                    "The frozen adaptive skill says not to add this attribute to escape adaptive recreation.",
            )
        }

        val configChanges = element.getAttributeNodeNS(ANDROID_URI, "configChanges")
        if (configChanges != null && configChanges.value.isNotBlank()) {
            val hits = adaptationTokens(configChanges.value)
            if (hits.isNotEmpty()) {
                context.report(
                    MovieLabIssues.configChanges,
                    element,
                    context.getValueLocation(configChanges),
                    "android:configChanges includes ${hits.joinToString()}. " +
                        "Those tokens opt the activity out of recreation on size, orientation, or density changes.",
                )
            }
        }
    }

    private fun adaptationTokens(raw: String): List<String> {
        return raw.split('|')
            .map { it.trim() }
            .filter { it in ADAPTATION_TOKENS }
    }

    companion object {
        private const val ANDROID_URI = "http://schemas.android.com/apk/res/android"

        val ADAPTATION_TOKENS: Set<String> = setOf(
            "orientation",
            "screenSize",
            "screenLayout",
            "smallestScreenSize",
            "density",
        )
    }
}
```

## `checks/src/main/java/com/example/movielab/lint/MissingEnableEdgeToEdgeDetector.kt`

sha256 `9a6b6877f50bf25f6c7417f0fc02a62eb40e59d5829456bb2dda913d9ac14614`

```kt
package com.example.movielab.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.USuperExpression
import org.jetbrains.uast.UThisExpression
import org.jetbrains.uast.UastFacade
import org.jetbrains.uast.visitor.AbstractUastVisitor

class MissingEnableEdgeToEdgeDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                if (context.isTestSource) return
                val psi = node.javaPsi
                if (psi.isInterface || psi.isEnum || psi.isAnnotationType) return
                if (psi.hasModifierProperty(PsiModifier.ABSTRACT)) return
                val qualified = psi.qualifiedName ?: return
                if (SKIP_PREFIXES.any { qualified.startsWith(it) }) return
                if (!context.evaluator.inheritsFrom(psi, "android.app.Activity", false)) return
                inspect(context, node, mutableSetOf())
            }
        }
    }

    private fun inspect(context: JavaContext, cls: UClass, seen: MutableSet<String>) {
        val qualified = cls.qualifiedName ?: return
        if (!seen.add(qualified)) return
        val onCreate = findOnCreate(cls)
        val selfEnables = onCreate != null && enableOffsets(context, cls, onCreate).isNotEmpty()
        val superEnables = superclassEnables(context, cls, seen)
        val callsSuper = onCreate != null && callsSuperOnCreate(onCreate)
        val enabled = when {
            onCreate == null -> superEnables
            selfEnables -> true
            callsSuper && superEnables -> true
            else -> false
        }
        if (!enabled) {
            context.report(
                MovieLabIssues.missingEdgeToEdge,
                cls,
                context.getNameLocation(cls),
                "${cls.name} never calls enableEdgeToEdge() from androidx.activity or WindowCompat. " +
                    "The frozen edge-to-edge skill requires that call in onCreate, before setContent.",
            )
            return
        }
        if (onCreate == null) return
        val setContentAt = setContentOffsets(onCreate)
        if (setContentAt.isEmpty()) return
        val firstSetContent = setContentAt.min()
        val enables = enableOffsets(context, cls, onCreate).toMutableList()
        if (callsSuper && superEnables) {
            superOnCreateOffset(onCreate)?.let(enables::add)
        }
        if (enables.isEmpty() || enables.none { it < firstSetContent }) {
            context.report(
                MovieLabIssues.missingEdgeToEdge,
                onCreate,
                context.getLocation(onCreate),
                "enableEdgeToEdge() must run before setContent. " +
                    "A later call does not match the frozen edge-to-edge skill.",
            )
        }
    }

    private fun superclassEnables(context: JavaContext, cls: UClass, seen: MutableSet<String>): Boolean {
        val superPsi = cls.javaPsi.superClass ?: return false
        val superName = superPsi.qualifiedName ?: return false
        if (superName in FRAMEWORK_ACTIVITIES || superName == "java.lang.Object") return false
        if (SKIP_PREFIXES.any { superName.startsWith(it) }) return false
        val superU = toUClass(superPsi) ?: return false
        val onCreate = findOnCreate(superU)
        if (onCreate != null && enableOffsets(context, superU, onCreate).isNotEmpty()) return true
        if (onCreate != null && !callsSuperOnCreate(onCreate)) return false
        return superclassEnables(context, superU, seen)
    }

    private fun toUClass(psi: PsiClass): UClass? {
        // JavaContext.uastContext is deprecated and hidden. UastFacade is the replacement.
        return UastFacade.convertElementWithParent(psi, UClass::class.java) as? UClass
    }

    private fun findOnCreate(cls: UClass): UMethod? {
        return cls.methods.firstOrNull { method ->
            method.name == "onCreate" && method.uastParameters.size == 1
        }
    }

    private fun enableOffsets(context: JavaContext, cls: UClass, method: UMethod): List<Int> {
        val direct = callsIn(method).filter { isEnableEdgeToEdge(it) }.mapNotNull(::offset)
        val viaHelper = callsIn(method).mapNotNull { call ->
            val name = call.methodName ?: return@mapNotNull null
            if (call.receiver != null && call.receiver !is UThisExpression) return@mapNotNull null
            val helper = cls.methods.firstOrNull { it.name == name && it != method } ?: return@mapNotNull null
            if (callsIn(helper).none { isEnableEdgeToEdge(it) }) return@mapNotNull null
            offset(call)
        }
        return direct + viaHelper
    }

    private fun setContentOffsets(method: UMethod): List<Int> {
        return callsIn(method).filter { isComposeSetContent(it) }.mapNotNull(::offset)
    }

    private fun callsSuperOnCreate(method: UMethod): Boolean {
        return callsIn(method).any { it.methodName == "onCreate" && it.receiver is USuperExpression }
    }

    private fun superOnCreateOffset(method: UMethod): Int? {
        val call = callsIn(method).firstOrNull {
            it.methodName == "onCreate" && it.receiver is USuperExpression
        } ?: return null
        return offset(call)
    }

    private fun callsIn(method: UMethod): List<UCallExpression> {
        val calls = mutableListOf<UCallExpression>()
        method.accept(object : AbstractUastVisitor() {
            override fun visitCallExpression(node: UCallExpression): Boolean {
                calls += node
                return super.visitCallExpression(node)
            }
        })
        return calls
    }

    private fun isEnableEdgeToEdge(call: UCallExpression): Boolean {
        if (call.methodName != "enableEdgeToEdge") return false
        val method: PsiMethod = call.resolve() ?: return false
        val owner = method.containingClass?.qualifiedName ?: return false
        if (owner == "androidx.core.view.WindowCompat") return true
        if (owner.startsWith("androidx.activity.")) {
            val receiver = call.receiver
            return receiver == null || receiver is UThisExpression
        }
        return false
    }

    private fun isComposeSetContent(call: UCallExpression): Boolean {
        if (call.methodName != "setContent") return false
        val owner = call.resolve()?.containingClass?.qualifiedName ?: return false
        return owner.startsWith("androidx.activity.")
    }

    private fun offset(call: UCallExpression): Int? = call.sourcePsi?.textRange?.startOffset

    companion object {
        private val FRAMEWORK_ACTIVITIES = setOf(
            "android.app.Activity",
            "androidx.activity.ComponentActivity",
            "androidx.fragment.app.FragmentActivity",
            "androidx.appcompat.app.AppCompatActivity",
        )
        private val SKIP_PREFIXES = listOf(
            "android.app.",
            "androidx.activity.",
            "androidx.appcompat.",
            "androidx.fragment.",
            "androidx.core.",
        )
    }
}
```

## `checks/src/main/java/com/example/movielab/lint/MovieLabIssueRegistry.kt`

sha256 `e68e711ec30f178a8fddd387943a19c352b76560be62454c3126bfc91729fda3`

```kt
package com.example.movielab.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class MovieLabIssueRegistry : IssueRegistry() {
    override val issues: List<Issue> = MovieLabIssues.all

    override val api: Int = CURRENT_API

    override val minApi: Int = 14

    override val vendor: Vendor = Vendor(
        vendorName = "android-skill-lint-lab",
        identifier = "com.example.movielab:checks",
        feedbackUrl = "https://github.com/hellokai55/android-skill-lint-lab",
    )
}
```

## `checks/src/main/java/com/example/movielab/lint/MovieLabIssues.kt`

sha256 `797e09eea5c50d4639b1cc0c11214e6a8febe2f9ee3f5122235be49f87076ccb`

```kt
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
```

## `checks/src/main/java/com/example/movielab/lint/UnsaveableScrollRememberDetector.kt`

sha256 `5597ea1abac5026d47dcd54dce2e56c852f3ba4c485ddd69b94519c7254b7e50`

```kt
package com.example.movielab.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.ULambdaExpression
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.visitor.AbstractUastVisitor

class UnsaveableScrollRememberDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitCallExpression(node: UCallExpression) {
                val kind = rememberKind(node) ?: return
                if (kind == RememberKind.SAVEABLE && hasExplicitSaver(node)) return
                val lambda = node.valueArguments.filterIsInstance<ULambdaExpression>().lastOrNull() ?: return
                val constructor = firstScrollConstructor(lambda) ?: return
                val typeName = constructor.resolve()?.containingClass?.qualifiedName ?: constructor.methodName
                context.report(
                    MovieLabIssues.unsaveableScroll,
                    constructor,
                    context.getLocation(constructor),
                    "$kind of $typeName bypasses the scroll Saver. " +
                        "Use rememberLazyListState, rememberLazyGridState, " +
                        "rememberLazyStaggeredGridState, or rememberScrollState.",
                )
            }
        }
    }

    private enum class RememberKind(val label: String) {
        REMEMBER("remember"),
        SAVEABLE("rememberSaveable"),
        ;

        override fun toString(): String = label
    }

    private fun rememberKind(call: UCallExpression): RememberKind? {
        val name = call.methodName ?: return null
        val kind = when (name) {
            "remember" -> RememberKind.REMEMBER
            "rememberSaveable" -> RememberKind.SAVEABLE
            else -> return null
        }
        val method = call.resolve() ?: return null
        if (method.name != name) return null
        val owner = method.containingClass?.qualifiedName ?: return null
        if (!owner.startsWith("androidx.compose.runtime")) return null
        return kind
    }

    private fun hasExplicitSaver(call: UCallExpression): Boolean {
        if (call.valueArguments.any { (it as? UNamedExpression)?.name == "saver" }) return true
        val ktCall = generateSequence(call.sourcePsi) { it.parent }
            .filterIsInstance<KtCallExpression>()
            .firstOrNull()
            ?: return false
        return ktCall.valueArguments.any { argument ->
            argument.getArgumentName()?.asName?.asString() == "saver"
        }
    }

    private fun firstScrollConstructor(scope: UElement): UCallExpression? {
        var found: UCallExpression? = null
        scope.accept(object : AbstractUastVisitor() {
            override fun visitCallExpression(node: UCallExpression): Boolean {
                if (found == null && isScrollConstructor(node)) {
                    found = node
                }
                return super.visitCallExpression(node)
            }
        })
        return found
    }

    private fun isScrollConstructor(call: UCallExpression): Boolean {
        val method = call.resolve() ?: return false
        if (!method.isConstructor) return false
        val qualified = method.containingClass?.qualifiedName ?: return false
        return qualified in SCROLL_TYPES
    }

    companion object {
        private val SCROLL_TYPES = setOf(
            "androidx.compose.foundation.lazy.LazyListState",
            "androidx.compose.foundation.lazy.grid.LazyGridState",
            "androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState",
            "androidx.compose.foundation.ScrollState",
        )
    }
}
```

## `checks/src/main/resources/META-INF/services/com.android.tools.lint.client.api.IssueRegistry`

sha256 `13c8e837c1c522fb8031d981ff468f74ee167675813a992793c85be13aeb7f4a`

```IssueRegistry
com.example.movielab.lint.MovieLabIssueRegistry
```

## `checks/src/test/java/com/example/movielab/lint/AdaptivePaneScaffoldDetectorTest.kt`

sha256 `6685856121e35297a15ae16b8a3fa7a0f9105ceb8f9a059933215b0035be774b`

```kt
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
```

## `checks/src/test/java/com/example/movielab/lint/ManifestAdaptationDetectorTest.kt`

sha256 `c4f8fd9717f4bae2c7bb507d1adf122d9aca8b39a312f2f7fbab28f6b34a694c`

```kt
package com.example.movielab.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.manifest
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/** Gold corpus for manifest rules. Not a model sample. */
class ManifestAdaptationDetectorTest {
    @Test
    fun portraitLockIsReported() {
        lint().files(manifest(activity("""android:screenOrientation="portrait"""")))
            .issues(MovieLabIssues.screenOrientation)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
            .expectContains("screenOrientation")
    }

    @Test
    fun fullSensorIsStillReported() {
        lint().files(manifest(activity("""android:screenOrientation="fullSensor"""")))
            .issues(MovieLabIssues.screenOrientation)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun activityAliasIsReported() {
        lint().files(
            manifest(
                """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="com.example">
                    <application>
                        <activity-alias android:name=".Alias" android:targetActivity=".Main"
                            android:screenOrientation="landscape" />
                    </application>
                </manifest>
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.screenOrientation)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
    }

    @Test
    fun absentOrientationIsClean() {
        lint().files(manifest(activity("android:exported=\"true\"")))
            .issues(MovieLabIssues.screenOrientation)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun toolsIgnoreSuppressesOrientation() {
        lint().files(
            manifest(
                """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:tools="http://schemas.android.com/tools"
                    package="com.example">
                    <application>
                        <activity android:name=".Main" android:screenOrientation="portrait"
                            tools:ignore="AdaptiveScreenOrientationLock" />
                    </application>
                </manifest>
                """.trimIndent(),
            ),
        )
            .issues(MovieLabIssues.screenOrientation)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun sizeAndOrientationConfigChangesAreReported() {
        lint().files(
            manifest(
                activity(
                    """android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize|density"""",
                ),
            ),
        )
            .issues(MovieLabIssues.configChanges)
            .allowMissingSdk()
            .run()
            .expectWarningCount(1)
            .expectContains("orientation")
    }

    @Test
    fun keyboardOnlyConfigChangesAreClean() {
        lint().files(manifest(activity("""android:configChanges="keyboard|keyboardHidden|locale"""")))
            .issues(MovieLabIssues.configChanges)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun uiModeAloneIsNotThisRule() {
        lint().files(manifest(activity("""android:configChanges="uiMode"""")))
            .issues(MovieLabIssues.configChanges)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    @Test
    fun adjustResizeIsNotConfigChanges() {
        lint().files(manifest(activity("""android:windowSoftInputMode="adjustResize"""")))
            .issues(MovieLabIssues.configChanges, MovieLabIssues.screenOrientation)
            .allowMissingSdk()
            .run()
            .expectClean()
    }

    private fun activity(attributes: String): String {
        return """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.example">
                <application>
                    <activity android:name=".Main" $attributes />
                </application>
            </manifest>
        """.trimIndent()
    }
}
```

## `checks/src/test/java/com/example/movielab/lint/MissingEnableEdgeToEdgeDetectorTest.kt`

sha256 `986fe960a27f3b171a226fd87c18719f1a3ea2c36973e04e3d30fe9c785d5072`

```kt
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
```

## `checks/src/test/java/com/example/movielab/lint/UnsaveableScrollRememberDetectorTest.kt`

sha256 `2fc4e263bb3169439f644e2bd9716d2bc2f47b438d9a27446b95602f8d5e3f2a`

```kt
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
```
