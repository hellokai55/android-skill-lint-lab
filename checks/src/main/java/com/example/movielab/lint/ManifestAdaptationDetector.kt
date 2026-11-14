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
