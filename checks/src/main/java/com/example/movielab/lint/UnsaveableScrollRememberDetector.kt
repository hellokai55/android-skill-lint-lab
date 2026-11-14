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
