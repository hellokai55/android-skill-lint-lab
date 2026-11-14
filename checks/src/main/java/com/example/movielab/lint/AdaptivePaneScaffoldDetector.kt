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
