plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// One command for the checks this repo has actually wired.
// It does not cover UI, visual, security, or rules that were not implemented.
tasks.register("qualityGate") {
    group = "verification"
    description =
        "Detector gold tests, demo unit tests, demo lint, and legacy baseline lint."
    dependsOn(
        ":checks:test",
        ":app:testDebugUnitTest",
        ":app:lintDebug",
        ":legacy:lintDebug",
    )
}
