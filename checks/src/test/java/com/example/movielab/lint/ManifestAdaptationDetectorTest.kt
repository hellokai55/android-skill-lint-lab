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
