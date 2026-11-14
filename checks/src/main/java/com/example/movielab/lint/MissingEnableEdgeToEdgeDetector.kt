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
