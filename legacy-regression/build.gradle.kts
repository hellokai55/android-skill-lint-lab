plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.movielab.legacyregression"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.movielab.legacyregression"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    lint {
        lintConfig = file("lint.xml")
        abortOnError = true
        warningsAsErrors = false
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    lintChecks(project(":checks"))
}

// This module is a negative sample: :legacy-regression:lintDebug must fail.
// Keep that task out of the module check graph so ./gradlew :app:assembleDebug
// and qualityGate can succeed. Invoking lintDebug directly still enforces it.
afterEvaluate {
    tasks.named("check").configure {
        setDependsOn(
            dependsOn.filterNot { dependency ->
                dependency.toString().contains("lint", ignoreCase = true)
            },
        )
    }
}
