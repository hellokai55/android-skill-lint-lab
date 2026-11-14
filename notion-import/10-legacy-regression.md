# legacy-regression

源码快照 commit：`9441209aadafb1f33c3694643eed5880fa715177`

以下是该目录在该 commit 的完整文本，不是片段。二进制文件只出现在索引里。

## `legacy-regression/README.md`

sha256 `e02690a2823ff1219279e7c4a86fc321cf80f51ad0a3d3f20f595887734c1276`

```md
# 人工构造的新增违规

`NewDebtActivity` 只有一条映射规则违规：`remember { ScrollState(0) }`，源码里有标记 `NEW_DEBT_MARKER`。

这个模块没有 lint baseline。在 `lint.xml` 把该规则升为 error 之后，`./gradlew :legacy-regression:lintDebug` 应当以非 0 退出。

不要把这条违规补进 `:legacy` 的 baseline 来换取通过。那样做会掩盖新增问题，而不是偿还历史债务。
```

## `legacy-regression/build.gradle.kts`

sha256 `685bdd07f18ab3f46dcf682a5f5bddbd28a97d22c24a484b91049f132f7a7228`

```kts
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
```

## `legacy-regression/lint.xml`

sha256 `bcf25b6b83fd1ca98137f1481b6cf0d771836d83d35e9d8daa7fc76544910485`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- lifecycle-runtime ships a lint jar this AGP cannot load. Ignoring the meta-warning does not enable those checks. -->
    <issue id="ObsoleteLintCustomCheck" severity="ignore" />
    <issue id="AdaptiveScreenOrientationLock" severity="error" />
    <issue id="AdaptiveConfigChangesOptOut" severity="error" />
    <issue id="UnsaveableScrollRemember" severity="error" />
    <issue id="AdaptivePaneScaffoldBanned" severity="error" />
    <issue id="MissingEnableEdgeToEdge" severity="error" />
</lint>
```

## `legacy-regression/src/main/AndroidManifest.xml`

sha256 `52f574050b458e4f1a7c037b5b31057084a2f677e563d0ed8aeffd512acc04db`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.LegacyRegression">
        <activity
            android:name=".NewDebtActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

## `legacy-regression/src/main/java/com/example/movielab/legacyregression/NewDebtActivity.kt`

sha256 `2c9a94efde5b09026ea1d9f484e654f3c1140a4c60c7d08661ff65672e5ebcb4`

```kt
package com.example.movielab.legacyregression

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Hand-constructed NEW debt. NEW_DEBT_MARKER.
 * The activity itself calls enableEdgeToEdge before setContent and the manifest
 * does not lock orientation. The only mapped violation is remember { ScrollState(0) }.
 * There is no baseline in this module, so lintDebug is expected to exit non-zero.
 * This file is not model output.
 */
class NewDebtActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { NewDebtScreen() }
    }
}

@Composable
private fun NewDebtScreen() {
    val scroll = remember { ScrollState(0) }
    Text("新增违规样本，滚动初始值 ${scroll.value}。NEW_DEBT_MARKER")
}
```

## `legacy-regression/src/main/res/drawable/ic_launcher.xml`

sha256 `7865a4f52be12b377ff22bdf8efc2740de026db8a0ca72c618a21dac468b8c1a`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#6E1F1F" android:pathData="M0,0h108v108h-108z" />
</vector>
```

## `legacy-regression/src/main/res/values/strings.xml`

sha256 `c3f4e4718b6765046ef88cc58081484b4185572bcf016134313f5197535e8640`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">新增违规样本</string>
</resources>
```

## `legacy-regression/src/main/res/values/themes.xml`

sha256 `06d4856a99411372dbc95336b29c989bdacfd1bc6deec484f96a61533d7d0f5c`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.LegacyRegression" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```
