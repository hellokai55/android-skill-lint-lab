# legacy

源码快照 commit：`9441209aadafb1f33c3694643eed5880fa715177`

以下是该目录在该 commit 的完整文本，不是片段。二进制文件只出现在索引里。

## `legacy/README.md`

sha256 `246695149eac875d53f086ab64c2186e9839c05e88fa0d1a10dae2f887a404f9`

```md
# 人工构造的历史样本

这个模块是手写的旧片单，用来演示 lint baseline。它不是模型生成，也不是官方示例。

历史违规（应被 baseline 接纳，`lintDebug` 退出码 0）：

- `android:screenOrientation="portrait"`
- `android:configChanges` 含 orientation / screenSize / screenLayout / smallestScreenSize
- `onCreate` 里 `setContent` 之前没有 `enableEdgeToEdge`
- `remember { LazyListState() }`
- 直接调用 `ListDetailPaneScaffold`

`lint-baseline.xml` 由 `./gradlew :legacy:updateLintBaseline` 生成后提交。随意把新问题写进这个文件，会把新增违规藏起来。负例是 `:legacy-regression`，以及 `scripts/verify-legacy-gate.sh` 在临时文件上的检查。

baseline 只记录这些检测器已经见过的问题。它不证明架构正确，也不检查埋点、路由或 WebView。
```

## `legacy/build.gradle.kts`

sha256 `769fd7ff14e89ba72433d39a3b9166c4ff9af47036f4cf8d53c12f8f7f4c583f`

```kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.movielab.legacy"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.movielab.legacy"
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
        baseline = file("lint-baseline.xml")
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
    implementation(libs.adaptive)
    implementation(libs.adaptive.layout)
    lintChecks(project(":checks"))
}
```

## `legacy/lint-baseline.xml`

sha256 `db8dc2335a8e87b188b1f0ebdf33bb3a9c8aad8fc747b4acd6765afba6423881`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<issues format="6" by="lint 9.5.1" type="baseline" client="gradle" dependencies="false" name="AGP (9.5.1)" variant="all" version="9.5.1">

    <issue
        id="AdaptiveConfigChangesOptOut"
        message="android:configChanges includes orientation, screenSize, screenLayout, smallestScreenSize. Those tokens opt the activity out of recreation on size, orientation, or density changes."
        errorLine1="            android:configChanges=&quot;orientation|screenSize|screenLayout|smallestScreenSize&quot;>"
        errorLine2="                                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~">
        <location
            file="src/main/AndroidManifest.xml"
            line="13"
            column="36"/>
    </issue>

    <issue
        id="AdaptivePaneScaffoldBanned"
        message="ListDetailPaneScaffold is the pre-Navigation-3 adaptive scaffold. The frozen adaptive skill says to use a Navigation 3 SceneStrategy instead."
        errorLine1="    ListDetailPaneScaffold("
        errorLine2="    ^">
        <location
            file="src/main/java/com/example/movielab/legacy/LegacyActivity.kt"
            line="39"
            column="5"/>
    </issue>

    <issue
        id="AdaptiveScreenOrientationLock"
        message="android:screenOrientation=&quot;portrait&quot; locks or overrides orientation. The frozen adaptive skill says not to add this attribute to escape adaptive recreation."
        errorLine1="            android:screenOrientation=&quot;portrait&quot;"
        errorLine2="                                       ~~~~~~~~">
        <location
            file="src/main/AndroidManifest.xml"
            line="12"
            column="40"/>
    </issue>

    <issue
        id="MissingEnableEdgeToEdge"
        message="LegacyActivity never calls enableEdgeToEdge() from androidx.activity or WindowCompat. The frozen edge-to-edge skill requires that call in onCreate, before setContent."
        errorLine1="class LegacyActivity : ComponentActivity() {"
        errorLine2="      ~~~~~~~~~~~~~~">
        <location
            file="src/main/java/com/example/movielab/legacy/LegacyActivity.kt"
            line="27"
            column="7"/>
    </issue>

    <issue
        id="UnsaveableScrollRemember"
        message="remember of androidx.compose.foundation.lazy.LazyListState bypasses the scroll Saver. Use rememberLazyListState, rememberLazyGridState, rememberLazyStaggeredGridState, or rememberScrollState."
        errorLine1="    val listState = remember { LazyListState() }"
        errorLine2="                               ~~~~~~~~~~~~~~~">
        <location
            file="src/main/java/com/example/movielab/legacy/LegacyActivity.kt"
            line="37"
            column="32"/>
    </issue>

    <issue
        id="LockedOrientationActivity"
        message="Expecting `android:screenOrientation=&quot;unspecified&quot;` or `&quot;fullSensor&quot;` for this activity so the user can use the application in any orientation and provide a great experience on Chrome OS devices"
        errorLine1="            android:screenOrientation=&quot;portrait&quot;"
        errorLine2="            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~">
        <location
            file="src/main/AndroidManifest.xml"
            line="12"
            column="13"/>
    </issue>

    <issue
        id="DiscouragedApi"
        message="Fixed screen orientations will be ignored in most cases, starting from Android 16. Android is moving toward a model where apps are expected to adapt to various orientations, display sizes, and aspect ratios."
        errorLine1="            android:screenOrientation=&quot;portrait&quot;"
        errorLine2="            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~">
        <location
            file="src/main/AndroidManifest.xml"
            line="12"
            column="13"/>
    </issue>

</issues>
```

## `legacy/lint.xml`

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

## `legacy/src/main/AndroidManifest.xml`

sha256 `cd6c613fd504c33edaef9a2f31f428a2d402cf60b9a67716890be42fb44c60f0`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Hand-built historical debt. Not model output. -->
    <application
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.Legacy">
        <activity
            android:name=".LegacyActivity"
            android:exported="true"
            android:screenOrientation="portrait"
            android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

## `legacy/src/main/java/com/example/movielab/legacy/LegacyActivity.kt`

sha256 `50ec2ba923d1097a05077878e6eccc39ea7e21407671910aeddf711356ce6873`

```kt
package com.example.movielab.legacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Hand-constructed legacy sample. It is not model output and it is not an
 * official example. The checked-in baseline is generated from this file;
 * new violations outside that baseline are supposed to fail the build.
 */
class LegacyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LegacyScreen() }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun LegacyScreen() {
    val listState = remember { LazyListState() }
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
    ListDetailPaneScaffold(
        directive = directive,
        value = ThreePaneScaffoldValue(
            PaneAdaptedValue.Expanded,
            PaneAdaptedValue.Expanded,
            PaneAdaptedValue.Hidden,
        ),
        listPane = {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                items(listOf("人工构造甲", "人工构造乙", "人工构造丙")) { title ->
                    Text(title)
                }
            }
        },
        detailPane = {
            Column {
                Text("人工构造的旧双栏写法")
                Text("历史债务应留在 baseline 里，而不是被改检测器抹掉。")
            }
        },
    )
}
```

## `legacy/src/main/res/drawable/ic_launcher.xml`

sha256 `165f695f59952545cf18c0ca87cfb8d2112cf1c16c37d6fe8d65d7ddf3efcf9b`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:fillColor="#6E4B1F" android:pathData="M0,0h108v108h-108z" />
</vector>
```

## `legacy/src/main/res/values/strings.xml`

sha256 `1f415ad1c86a9e6f33e232a1f51b2b7091095f8cbada9815554ead2b2481354e`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">人工构造旧片单</string>
</resources>
```

## `legacy/src/main/res/values/themes.xml`

sha256 `0680908ad44b51fc43c5fc0cf14ef64e65d2b2b6550499eb0c1cc2015a829d30`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Legacy" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```
