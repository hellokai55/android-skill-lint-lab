# config-ci

源码快照 commit：`9441209aadafb1f33c3694643eed5880fa715177`

以下是该目录在该 commit 的完整文本，不是片段。二进制文件只出现在索引里。

## `.github/workflows/quality-gate.yml`

sha256 `f24add181fd3cd71a63a78ae6062509934f0566f263c124d6bd4ed8544df56ba`

```yml
# 建议中的 CI。文件存在不等于 protected branch required check 已启用。
name: quality-gate

on:
  pull_request:
  push:
    branches: [main]

permissions:
  contents: read

jobs:
  gate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
      - name: Install Android SDK packages
        run: |
          set -euo pipefail
          mkdir -p "$HOME/android-sdk/cmdline-tools"
          curl -fsSL -o /tmp/cmdtools.zip "https://dl.google.com/android/repository/commandlinetools-linux-16111833_latest.zip"
          unzip -q /tmp/cmdtools.zip -d "$HOME/android-sdk/cmdline-tools"
          mv "$HOME/android-sdk/cmdline-tools/cmdline-tools" "$HOME/android-sdk/cmdline-tools/latest"
          export ANDROID_HOME="$HOME/android-sdk"
          export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
          yes | sdkmanager --licenses >/tmp/licenses.log || true
          sdkmanager "platforms;android-37.0" "build-tools;36.0.0" "platform-tools"
          echo "sdk.dir=$ANDROID_HOME" > local.properties
      - name: qualityGate
        run: ./gradlew qualityGate --stacktrace --no-daemon
      - name: new debt must fail lint
        run: |
          set +e
          ./gradlew :legacy-regression:lintDebug --stacktrace --no-daemon
          code=$?
          if [ "$code" -eq 0 ]; then
            echo "legacy-regression lint was expected to fail"
            exit 1
          fi
          exit 0
```

## `AGENTS.md`

sha256 `7ac8375e1b5d9284423d2ffe6531bf617d5e4c77fcdfd80303857034878855e5`

````md
# 给代理的说明

这条说明只是提示。它不会在提交时执行，本地 hook 也可以不跑。

对已经接入本仓库的检查，提交前运行：

```bash
./gradlew qualityGate
```

新增违规样本应当失败：

```bash
./gradlew :legacy-regression:lintDebug
```

不要修改 `checks/`、`lint.xml` 或 `lint-baseline.xml` 来让任务变绿。不要用 `@Suppress` 或 `tools:ignore` 绕过 `AdaptiveScreenOrientationLock`、`AdaptiveConfigChangesOptOut`、`UnsaveableScrollRemember`、`AdaptivePaneScaffoldBanned`、`MissingEnableEdgeToEdge`。

这条命令不覆盖功能、视觉、安全，也不覆盖还没写成检测器的团队规范。官方 Skills 文本在 `third_party/android-skills/`，不是团队规范。

合入是否强制，取决于受保护分支的 required check。本仓库的工作流文件不等于已经打开的 required check。
````

## `CLAUDE.md`

sha256 `282b141bd5aea88a80b9696eab92277eca7cdfdedba2e2260c40bf2939193e55`

````md
# 给代理的说明

与 `AGENTS.md` 相同。把命令写在这里不会让命令执行。

```bash
./gradlew qualityGate
```

期望退出码 0。它不是全部最佳实践的证明。
````

## `LICENSE`

sha256 `58d1e17ffe5109a7ae296caafcadfdbe6a7d176f0bc4ab01e12a689b0499d8bd`

```text

                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."

      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or
          Derivative Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.

      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.

   9. Accepting Warranty or Additional Liability. While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   APPENDIX: How to apply the Apache License to your work.

      To apply the Apache License to your work, attach the following
      boilerplate notice, with the fields enclosed by brackets "[]"
      replaced with your own identifying information. (Don't include
      the brackets!)  The text should be enclosed in the appropriate
      comment syntax for the file format. We also recommend that a
      file or class name and description of purpose be included on the
      same "printed page" as the copyright notice for easier
      identification within third-party archives.

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

## `NOTICE`

sha256 `55d5484e9d212ca632ac6f4539c0dde9cc31e22fdfb7e1f7b3c7d8c007d9ee07`

```text
android-skill-lint-lab

Original source in this repository, other than the directory named below,
is available under the Apache License, Version 2.0. See LICENSE.

third_party/android-skills/ contains unmodified files from
https://github.com/android/skills tag v1.0.21
commit 5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d
Copyright Google LLC and the Android Open Source Project, as stated upstream.
Those files remain under the Apache License, Version 2.0.
See third_party/android-skills/LICENSE.txt and SOURCES.md.
```

## `README.md`

sha256 `0db64f3d530b9c37ee6a85d40080853e5fa1bfd993a8e2619e1a76fd984ea1a7`

````md
# 既有工程里的质量门禁

这个仓库回答一个工程问题：Android 项目已经有人用不同模型交代码，团队也曾经用文字写下架构、埋点、路由和 WebView 的做法，但继续补句子并不能让这些做法稳定地留在合并结果里。能静态判定、并且误报可接受的条款，应该变成提交前会失败的检查。官方 Android Skills 只是这次实验的外部参照，不是团队规范，也没有在我们读到的版本里被打包成强制 Lint。

还不是终稿。模型对照没有跑，N = 0。手写的电影列表、检测器金标准和 legacy 夹具不能当成模型通过率。后续要跑的统一任务、两臂 prompt 和评分脚本在 `experiment/`。

本轮已经跑通的是 `./gradlew qualityGate`（退出码 0）和 legacy 正负门禁（见 `evidence/legacy-gate.json`）。电影页在模拟器里安装并启动，无障碍树能读到列表和占位海报字样；帧缓冲截图全黑，视觉未验收。细节在 `docs/研究笔记.md`。

## 提交前命令

```bash
./gradlew qualityGate
```

这条命令覆盖检测器金标准、电影列表的本地数据测试、参照应用的 Lint，以及带 baseline 的历史样本。它不覆盖功能以外的视觉和安全，也不覆盖还没写成检测器的团队规范。

新增违规应当失败：

```bash
./gradlew :legacy-regression:lintDebug
```

`AGENTS.md` 与 `CLAUDE.md` 只是把命令写出来。本地 hook 可以跳过。合入强制需要受保护分支上的 required check。本仓库没有打开这项保护。见 `docs/ci-建议.md`。

## 布局

| 路径 | 是什么 |
| --- | --- |
| `docs/实验协议.md` | 预先登记的任务、分母、臂和评分 |
| `docs/研究笔记.md` | 问题推导与已测/未测。非终稿 |
| `docs/规则出处.md` | 五条检测器各自对应的冻结原文 |
| `docs/架构.md` | 可编辑的 Mermaid |
| `third_party/android-skills/` | v1.0.21 / `5815d27` 的原文副本 |
| `checks/` | 自定义 Lint 与金标准 |
| `app/` | 手写电影参照，不是盲测样本 |
| `legacy/` | 人工构造的历史债务，baseline 只接纳这些 |
| `legacy-regression/` | 人工构造的新增债务，Lint 应当失败 |
| `evidence/` | 命令跑完之后的退出码和报告 |

工具链见 `docs/工具链.md`。需要本机 Android SDK，路径写在不提交的 `local.properties`。
````

## `build.gradle.kts`

sha256 `e7b208ed991018ca184fd68537ad5cb459025940cee4e47f929dd3b727d4193e`

```kts
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
```

## `evidence/README.md`

sha256 `8481ed16d32473dcf22cfc211bb13310b2a351055c63ee31fbcdfae80af368bc`

```md
# 证据

这些文件是 2026-11-14 在本工作区跑出来的。没有模型会话。

| 文件 | 是什么 |
| --- | --- |
| `toolchain.txt` | `java -version`、`./gradlew --version`、已安装的 platform 与 build-tools、`qualityGate` 退出码 |
| `quality-gate.log` | `./gradlew qualityGate` 的控制台输出 |
| `durations.txt` | 从那些输出里抄下来的 `BUILD SUCCESSFUL` 行和门禁秒数 |
| `legacy-gate.json` | baseline 正负门禁的退出码。`gate_ok` 为 true 表示五次期望都对上 |
| `legacy-gate.log` | 门禁脚本的 Gradle 输出 |
| `verify-legacy-gate.stdout` | 脚本打印的 JSON |
| `verify-legacy-gate.stderr` | 每次命令的退出码和秒数 |
| `detector-cases.csv` / `.svg` | 从 JUnit XML 画出的金标准用例数。不是 A/B |
| `TEST-*.xml` | JUnit 结果副本 |
| `score-app-lint.json` | 参照应用 lint XML 的五个 id 计数，`sample_kind=reference_implementation_not_model` |
| `score-regression-lint.json` | 新增违规样本的计数，`sample_kind=hand_constructed_new_debt` |
| `score-parser-smoke.json` | 解析器对一份假 XML 的自检。不是实验结果 |
| `screenshots/` | 只有模拟器真实截到图才会出现 |

参照应用 lint 里还有 5 条 warning，id 是 `NewerVersionAvailable`（4）和 `AndroidGradlePluginVersion`（1）。它们不是这五个映射规则，也没有让 `lintDebug` 失败。
```

## `evidence/TEST-com.example.movielab.data.CatalogTest.xml`

sha256 `fa396cfd4042c07a5a339206eeb7a9198cafd6773a4174e2ef45f268c9ce0466`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.movielab.data.CatalogTest" tests="4" skipped="0" failures="0" errors="0" timestamp="2026-11-14T15:03:38.614Z" hostname="cursor" time="0.032">
  <properties/>
  <testcase name="idsAreUniqueAndStable" classname="com.example.movielab.data.CatalogTest" time="0.011"/>
  <testcase name="atLeastThirtyLocalMovies" classname="com.example.movielab.data.CatalogTest" time="0.0"/>
  <testcase name="contentFiltersByTitleGenreOrSynopsis" classname="com.example.movielab.data.CatalogTest" time="0.013"/>
  <testcase name="simulatedModesAreDeterministicAndOffline" classname="com.example.movielab.data.CatalogTest" time="0.002"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

## `evidence/TEST-com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest.xml`

sha256 `89c6bbc75fbe4a24c1695fee33ae5030f3e57afdb07dd1fbc36cde20097cec03`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest" tests="7" skipped="0" failures="0" errors="0" timestamp="2026-11-14T15:00:14.026Z" hostname="cursor" time="2.4">
  <properties/>
  <testcase name="listDetailPaneScaffoldCallIsReported" classname="com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest" time="0.433"/>
  <testcase name="supportingPaneScaffoldCallIsReported" classname="com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest" time="0.38"/>
  <testcase name="sceneStrategyApisAreClean" classname="com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest" time="0.7"/>
  <testcase name="sameNameInAnotherPackageIsClean" classname="com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest" time="0.126"/>
  <testcase name="suppressIsClean" classname="com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest" time="0.213"/>
  <testcase name="importedAliasIsReported" classname="com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest" time="0.272"/>
  <testcase name="defaultsHelperIsClean" classname="com.example.movielab.lint.AdaptivePaneScaffoldDetectorTest" time="0.276"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

## `evidence/TEST-com.example.movielab.lint.ManifestAdaptationDetectorTest.xml`

sha256 `5c8daf0c6d8515ad6ff13ac1c7e61ea4047ee064d81732f0b548d07701222ec7`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.movielab.lint.ManifestAdaptationDetectorTest" tests="9" skipped="0" failures="0" errors="0" timestamp="2026-11-14T15:00:16.427Z" hostname="cursor" time="0.153">
  <properties/>
  <testcase name="keyboardOnlyConfigChangesAreClean" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.018"/>
  <testcase name="adjustResizeIsNotConfigChanges" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.015"/>
  <testcase name="portraitLockIsReported" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.023"/>
  <testcase name="uiModeAloneIsNotThisRule" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.016"/>
  <testcase name="fullSensorIsStillReported" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.021"/>
  <testcase name="absentOrientationIsClean" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.006"/>
  <testcase name="toolsIgnoreSuppressesOrientation" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.015"/>
  <testcase name="sizeAndOrientationConfigChangesAreReported" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.02"/>
  <testcase name="activityAliasIsReported" classname="com.example.movielab.lint.ManifestAdaptationDetectorTest" time="0.019"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

## `evidence/TEST-com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest.xml`

sha256 `447e87a088b07b3b6341e7486a8601365b517938958f42749ef0318a48c038dc`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" tests="12" skipped="0" failures="0" errors="0" timestamp="2026-11-14T15:00:16.581Z" hostname="cursor" time="9.51">
  <properties/>
  <testcase name="callAfterSetContentIsReported" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="1.573"/>
  <testcase name="windowCompatCallIsClean" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.63"/>
  <testcase name="superclassSourceCountsWhenSuperIsCalled" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.611"/>
  <testcase name="componentActivityExtensionBeforeSetContentIsClean" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.629"/>
  <testcase name="importedAliasOfEnableIsClean" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.615"/>
  <testcase name="missingCallIsReported" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="1.176"/>
  <testcase name="localFunctionWithTheSameNameDoesNotCount" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="1.04"/>
  <testcase name="abstractActivityIsNotReported" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.532"/>
  <testcase name="skippingSuperDoesNotInheritTheCall" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.975"/>
  <testcase name="suppressIsClean" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.555"/>
  <testcase name="setDecorFitsSystemWindowsDoesNotCount" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.59"/>
  <testcase name="sameClassHelperCalledBeforeSetContentIsClean" classname="com.example.movielab.lint.MissingEnableEdgeToEdgeDetectorTest" time="0.551"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

## `evidence/TEST-com.example.movielab.lint.UnsaveableScrollRememberDetectorTest.xml`

sha256 `b4e7afbf7cffc2544b2d06828d05ee766dfb6ebd23c45ee86fbbac527ffe4fb6`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" tests="12" skipped="0" failures="0" errors="0" timestamp="2026-11-14T15:00:04.872Z" hostname="cursor" time="9.153">
  <properties/>
  <testcase name="importedAliasOfRememberIsReported" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="3.127"/>
  <testcase name="localRememberAndLocalStateAreClean" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.244"/>
  <testcase name="composeRememberOfUnrelatedScrollStateIsClean" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.451"/>
  <testcase name="scrollStateConstructorIsReported" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.94"/>
  <testcase name="rememberSaveableWithoutSaverIsReported" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.874"/>
  <testcase name="rememberWithKeyStillReported" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.761"/>
  <testcase name="staggeredConstructorIsReported" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.726"/>
  <testcase name="rememberLazyListStateConstructorIsReported" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.688"/>
  <testcase name="rememberSaveableWithExplicitSaverIsClean" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.4"/>
  <testcase name="officialFactoriesAreClean" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.306"/>
  <testcase name="suppressOnTheFunctionIsClean" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.3"/>
  <testcase name="rememberOfOtherStateIsClean" classname="com.example.movielab.lint.UnsaveableScrollRememberDetectorTest" time="0.334"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
```

## `evidence/app-run-meta.json`

sha256 `be5cafd59e1adb17326293a8a77b0eaffac3beb29be59f99a23c106ccaac7cd3`

```json
{"sample_kind":"reference_implementation_not_model","note":"hand-written demo, not a model session"}
```

## `evidence/detector-cases.csv`

sha256 `bd616bdda8bdfe838314478f3dd85a4f5d41eacf90d5deb068a0fdf8eaa8bcb5`

```csv
suite,tests,failures,errors,skipped
AdaptivePaneScaffoldDetectorTest,7,0,0,0
ManifestAdaptationDetectorTest,9,0,0,0
MissingEnableEdgeToEdgeDetectorTest,12,0,0,0
UnsaveableScrollRememberDetectorTest,12,0,0,0
```

## `evidence/detector-cases.svg`

sha256 `f5586d050b2ef8ed471812d539dd5faaa2977116a0575d722c3f8510e0d8785d`

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="760" height="272" viewBox="0 0 760 272">
<rect width="100%" height="100%" fill="#f7f4ee"/>
<text x="24" y="32" font-family="sans-serif" font-size="18" fill="#1c1915">检测器金标准用例（JUnit，不是模型通过率）</text>
<text x="16" y="76" font-family="sans-serif" font-size="13" fill="#1c1915">AdaptivePaneScaffoldDetectorTest</text>
<rect x="280" y="56" width="256.7" height="28" fill="#2f6f4e"/>
<text x="288" y="75" font-family="sans-serif" font-size="12" fill="#f7f4ee">7 passed / 7 tests</text>
<text x="16" y="120" font-family="sans-serif" font-size="13" fill="#1c1915">ManifestAdaptationDetectorTest</text>
<rect x="280" y="100" width="330.0" height="28" fill="#2f6f4e"/>
<text x="288" y="119" font-family="sans-serif" font-size="12" fill="#f7f4ee">9 passed / 9 tests</text>
<text x="16" y="164" font-family="sans-serif" font-size="13" fill="#1c1915">MissingEnableEdgeToEdgeDetectorTest</text>
<rect x="280" y="144" width="440.0" height="28" fill="#2f6f4e"/>
<text x="288" y="163" font-family="sans-serif" font-size="12" fill="#f7f4ee">12 passed / 12 tests</text>
<text x="16" y="208" font-family="sans-serif" font-size="13" fill="#1c1915">UnsaveableScrollRememberDetectorTest</text>
<rect x="280" y="188" width="440.0" height="28" fill="#2f6f4e"/>
<text x="288" y="207" font-family="sans-serif" font-size="12" fill="#f7f4ee">12 passed / 12 tests</text>
</svg>
```

## `evidence/durations.txt`

sha256 `4be662d32481ec9996775f5b38e3f2e908c0dd0b9425d8349a9bec13f86472c9`

```txt
Observed Gradle lines from this workspace on 2026-11-14. Not estimated.

checks:test after the detector sources compiled:
BUILD SUCCESSFUL in 23s
JUnit XML totals: AdaptivePaneScaffoldDetectorTest 7, ManifestAdaptationDetectorTest 9,
MissingEnableEdgeToEdgeDetectorTest 12, UnsaveableScrollRememberDetectorTest 12.
Sum 40, failures 0, errors 0.

app assembleDebug + testDebugUnitTest + lintDebug, plus legacy and legacy-regression assembleDebug:
BUILD SUCCESSFUL in 24s

Later aggregate, with those outputs already up to date:
./gradlew qualityGate
BUILD SUCCESSFUL in 793ms
qualityGate_exit=0

legacy gate wall seconds printed by scripts/verify-legacy-gate.sh:
legacy_with_baseline exit=0 seconds=1
legacy_regression exit=1 seconds=2
legacy_without_baseline exit=1 seconds=1
legacy_with_temp_new_debt exit=1 seconds=2
legacy_restored exit=0 seconds=1
gate_ok=true
```

## `evidence/legacy-gate.json`

sha256 `1396c235dc515c033052aa3f5dad2f54d7309d68fa17206eccc46d2dfee1e50a`

```json
{
  "sample_kind": "hand_constructed_baseline_gate",
  "not_a_model_ab": true,
  "commands": [
    {
      "name": "legacy_with_baseline",
      "exit_code": 0,
      "expect": 0
    },
    {
      "name": "legacy_regression",
      "exit_code": 1,
      "expect": "nonzero"
    },
    {
      "name": "legacy_without_baseline",
      "exit_code": 1,
      "expect": "nonzero"
    },
    {
      "name": "legacy_with_temp_new_debt",
      "exit_code": 1,
      "expect": "nonzero"
    },
    {
      "name": "legacy_restored",
      "exit_code": 0,
      "expect": 0
    }
  ],
  "gate_ok": true
}
```

## `evidence/regression-run-meta.json`

sha256 `12f88d82dc95044cee3d6215a8effcd6e4996aa413c83d731d51d1c87d67f768`

```json
{"sample_kind":"hand_constructed_new_debt","note":"expected lint failure, not a model session"}
```

## `evidence/score-app-lint.json`

sha256 `d0584b336e7b63b8a5347cd4b951a38995aa13c368d676b3fbd5915b1a0ffbd2`

```json
{
  "sample_kind": "reference_implementation_not_model",
  "is_model_ab": false,
  "counts": {
    "lint_xml": "app/build/reports/lint-results-debug.xml",
    "tracked_issue_counts": {
      "AdaptiveScreenOrientationLock": 0,
      "AdaptiveConfigChangesOptOut": 0,
      "UnsaveableScrollRemember": 0,
      "AdaptivePaneScaffoldBanned": 0,
      "MissingEnableEdgeToEdge": 0
    },
    "tracked_total": 0,
    "other_issue_count": 5,
    "severity_counts": {
      "Warning": 5
    }
  },
  "meta": {
    "sample_kind": "reference_implementation_not_model",
    "note": "hand-written demo, not a model session"
  },
  "limit": "Functional, visual, and security results are not inferred from lint XML."
}
```

## `evidence/score-parser-smoke.json`

sha256 `58cd9f8d3f2ab44d5004c86d7c14f5a572306b3c81b4378c928b0b8e83844675`

```json
{
  "sample_kind": "unknown",
  "is_model_ab": false,
  "counts": {
    "lint_xml": "experiment/fixtures/parser-smoke-lint.xml",
    "tracked_issue_counts": {
      "AdaptiveScreenOrientationLock": 0,
      "AdaptiveConfigChangesOptOut": 0,
      "UnsaveableScrollRemember": 1,
      "AdaptivePaneScaffoldBanned": 0,
      "MissingEnableEdgeToEdge": 0
    },
    "tracked_total": 1,
    "other_issue_count": 0,
    "severity_counts": {
      "Error": 1
    }
  },
  "meta": {},
  "limit": "Functional, visual, and security results are not inferred from lint XML.",
  "warning": "sample_kind missing; do not cite this file as a model result"
}
```

## `evidence/score-regression-lint.json`

sha256 `1a00a03b78800d5d43d0998a70c76d17f504b4c0f76d99a77aec119d2caf764a`

```json
{
  "sample_kind": "hand_constructed_new_debt",
  "is_model_ab": false,
  "counts": {
    "lint_xml": "legacy-regression/build/reports/lint-results-debug.xml",
    "tracked_issue_counts": {
      "AdaptiveScreenOrientationLock": 0,
      "AdaptiveConfigChangesOptOut": 0,
      "UnsaveableScrollRemember": 1,
      "AdaptivePaneScaffoldBanned": 0,
      "MissingEnableEdgeToEdge": 0
    },
    "tracked_total": 1,
    "other_issue_count": 1,
    "severity_counts": {
      "Error": 1,
      "Warning": 1
    }
  },
  "meta": {
    "sample_kind": "hand_constructed_new_debt",
    "note": "expected lint failure, not a model session"
  },
  "limit": "Functional, visual, and security results are not inferred from lint XML."
}
```

## `evidence/screenshots/capture-log.txt`

sha256 `f078f415071821900cc3120a225e0efd1aee8b2841b72af60557bff1dde283dd`

```txt
2026-11-14 emulator capture. Not a visual acceptance.

Phone AVD movielab_phone: 1080x2400, density 420, system image android-36 aosp_atd, KVM after chmod 666 /dev/kvm.
Boot completed in 27005 ms (first -no-window run) and 22409 ms (Xvfb run). log line: "Boot completed".
adb install of app/build/outputs/apk/debug/app-debug.apk: Success.
am start com.example.movielab/.MainActivity: Starting Intent.

uiautomator dump /sdcard/phone-content.xml (27396 bytes) lists, among other nodes:
- 本地片单
- 搜索标题、类型或简介
- 演示状态（本地，无网络）
- 内容 / 加载 / 空 / 错误
- 占位海报, PLACEHOLDER, 雾港夜车, 纸月亮信差, 盐湖来信, 静音教室, 北纬灯塔, 铜钥匙

Framebuffer:
- screencap with -no-window -gpu swiftshader_indirect: PNG 1080x2400, 15580 bytes, ImageMagick mean=0
- same after KEYCODE_WAKEUP: still mean=0, 15580 bytes
- reboot -gpu guest: still mean=0, 15580 bytes
- Xvfb :99 without -no-window, import of the root window: center crop mean about 0.09

These frames are not usable pictures of the app. Visual layout, edge-to-edge, dual-pane, rotation, and scroll restoration were not accepted.
Tablet AVD was created (2560x1600, density 240, android-36 google_apis_tablet) and not booted, because the phone framebuffer was already black.
```

## `evidence/screenshots/phone-content-hierarchy.xml`

sha256 `ab806961dd5f7ffe8b0cd17c26366d270af88b371da82fbdaa3bd519cfcaab89`

```xml
<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><hierarchy rotation="0"><node index="0" text="" resource-id="" class="android.widget.FrameLayout" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2400]" drawing-order="0" hint=""><node index="0" text="" resource-id="" class="android.widget.LinearLayout" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2400]" drawing-order="1" hint=""><node index="0" text="" resource-id="android:id/content" class="android.widget.FrameLayout" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2400]" drawing-order="2" hint=""><node index="0" text="" resource-id="" class="androidx.compose.ui.platform.ComposeView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2400]" drawing-order="1" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2400]" drawing-order="0" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2400]" drawing-order="0" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2400]" drawing-order="0" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="true" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,2400]" drawing-order="0" hint=""><node index="0" text="" resource-id="" class="android.widget.EditText" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="true" password="false" selected="false" bounds="[32,168][1048,336]" drawing-order="0" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,189][1048,336]" drawing-order="0" hint="" /><node index="1" text="搜索标题、类型或简介" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[74,231][507,294]" drawing-order="1" hint="" /><node index="2" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[74,200][1006,326]" drawing-order="2" hint="" /></node><node index="1" text="演示状态（本地，无网络）" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,368][468,421]" drawing-order="1" hint="" /><node index="2" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="true" checked="true" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,442][190,568]" drawing-order="2" hint=""><node index="0" text="内容" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[74,479][148,532]" drawing-order="0" hint="" /><node index="1" text="" resource-id="" class="android.widget.CheckBox" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,463][190,547]" drawing-order="1" hint="" /></node><node index="3" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="true" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[211,442][369,568]" drawing-order="3" hint=""><node index="0" text="加载" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[253,479][327,532]" drawing-order="0" hint="" /><node index="1" text="" resource-id="" class="android.widget.CheckBox" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[211,463][369,547]" drawing-order="1" hint="" /></node><node index="4" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="true" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[391,442][517,568]" drawing-order="4" hint=""><node index="0" text="空" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[435,479][472,532]" drawing-order="0" hint="" /><node index="1" text="" resource-id="" class="android.widget.CheckBox" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[393,463][514,547]" drawing-order="1" hint="" /></node><node index="5" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="true" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[537,442][695,568]" drawing-order="5" hint=""><node index="0" text="错误" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[579,479][653,532]" drawing-order="0" hint="" /><node index="1" text="" resource-id="" class="android.widget.CheckBox" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[537,463][695,547]" drawing-order="1" hint="" /></node><node index="6" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,600][492,1141]" drawing-order="6" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="占位海报，非真实电影海报：雾港夜车" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,600][492,968]" drawing-order="0" hint=""><node index="0" text="占位海报" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,778][210,831]" drawing-order="0" hint="" /><node index="1" text="PLACEHOLDER" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,831][292,873]" drawing-order="1" hint="" /><node index="2" text="雾港夜车" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,873][235,936]" drawing-order="2" hint="" /></node><node index="1" text="雾港夜车" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1000][235,1063]" drawing-order="1" hint="" /><node index="2" text="1998 · 公路 · 104 分钟" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1063][382,1109]" drawing-order="2" hint="" /></node><node index="7" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[588,600][1048,1141]" drawing-order="7" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="占位海报，非真实电影海报：纸月亮信差" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[588,600][1048,968]" drawing-order="0" hint=""><node index="0" text="占位海报" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,778][766,831]" drawing-order="0" hint="" /><node index="1" text="PLACEHOLDER" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,831][848,873]" drawing-order="1" hint="" /><node index="2" text="纸月亮信差" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,873][834,936]" drawing-order="2" hint="" /></node><node index="1" text="纸月亮信差" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1000][834,1063]" drawing-order="1" hint="" /><node index="2" text="2004 · 剧情 · 96 分钟" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1063][920,1109]" drawing-order="2" hint="" /></node><node index="8" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,1173][492,1714]" drawing-order="8" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="占位海报，非真实电影海报：盐湖来信" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,1173][492,1541]" drawing-order="0" hint=""><node index="0" text="占位海报" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1351][210,1404]" drawing-order="0" hint="" /><node index="1" text="PLACEHOLDER" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1404][292,1446]" drawing-order="1" hint="" /><node index="2" text="盐湖来信" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1446][235,1509]" drawing-order="2" hint="" /></node><node index="1" text="盐湖来信" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1573][235,1636]" drawing-order="1" hint="" /><node index="2" text="1987 · 家庭 · 118 分钟" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1636][382,1682]" drawing-order="2" hint="" /></node><node index="9" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[588,1173][1048,1714]" drawing-order="9" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="占位海报，非真实电影海报：静音教室" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[588,1173][1048,1541]" drawing-order="0" hint=""><node index="0" text="占位海报" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1351][766,1404]" drawing-order="0" hint="" /><node index="1" text="PLACEHOLDER" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1404][848,1446]" drawing-order="1" hint="" /><node index="2" text="静音教室" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1446][791,1509]" drawing-order="2" hint="" /></node><node index="1" text="静音教室" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1573][791,1636]" drawing-order="1" hint="" /><node index="2" text="2012 · 校园 · 91 分钟" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1636][920,1682]" drawing-order="2" hint="" /></node><node index="10" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,1746][492,2287]" drawing-order="10" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="占位海报，非真实电影海报：北纬灯塔" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,1746][492,2114]" drawing-order="0" hint=""><node index="0" text="占位海报" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1924][210,1977]" drawing-order="0" hint="" /><node index="1" text="PLACEHOLDER" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,1977][292,2019]" drawing-order="1" hint="" /><node index="2" text="北纬灯塔" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,2019][235,2082]" drawing-order="2" hint="" /></node><node index="1" text="北纬灯塔" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,2146][235,2209]" drawing-order="1" hint="" /><node index="2" text="1976 · 冒险 · 128 分钟" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[64,2209][382,2255]" drawing-order="2" hint="" /></node><node index="11" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[588,1746][1048,2287]" drawing-order="11" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="占位海报，非真实电影海报：铜钥匙" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[588,1746][1048,2114]" drawing-order="0" hint=""><node index="0" text="占位海报" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1924][766,1977]" drawing-order="0" hint="" /><node index="1" text="PLACEHOLDER" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,1977][848,2019]" drawing-order="1" hint="" /><node index="2" text="铜钥匙" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,2019][749,2082]" drawing-order="2" hint="" /></node><node index="1" text="铜钥匙" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,2146][749,2209]" drawing-order="1" hint="" /><node index="2" text="2001 · 悬疑 · 102 分钟" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[620,2209][938,2255]" drawing-order="2" hint="" /></node><node index="12" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,2319][492,2400]" drawing-order="12" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="占位海报，非真实电影海报：雨季邮差" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[32,2319][492,2400]" drawing-order="0" hint="" /></node><node index="13" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="true" enabled="true" focusable="true" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[588,2319][1048,2400]" drawing-order="13" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="占位海报，非真实电影海报：玻璃温室" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[588,2319][1048,2400]" drawing-order="0" hint="" /></node></node><node index="1" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,168]" drawing-order="1" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[0,0][1080,168]" drawing-order="0" hint=""><node index="0" text="" resource-id="" class="android.view.View" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[43,21][271,147]" drawing-order="0" hint=""><node index="0" text="本地片单" resource-id="" class="android.widget.TextView" package="com.example.movielab" content-desc="" checkable="false" checked="false" clickable="false" enabled="true" focusable="false" focused="false" scrollable="false" long-clickable="false" password="false" selected="false" bounds="[43,43][271,125]" drawing-order="0" hint="" /></node></node></node></node></node></node></node></node></node></node></hierarchy>
```

## `evidence/toolchain.txt`

sha256 `cca20925f3aeea6d92ff85f346428823ad9afc52076760f43895b0c38f8ccf4c`

```txt
date=2026-11-14T15:08:20Z
openjdk version "21.0.13.0.3" 2026-10-24
OpenJDK Runtime Environment (build 21.0.13.0.3+1-1-24.04.1-Ubuntu)
OpenJDK 64-Bit Server VM (build 21.0.13.0.3+1-1-24.04.1-Ubuntu, mixed mode, sharing)

------------------------------------------------------------
Gradle 9.6.0
------------------------------------------------------------

Build time:    2026-06-18 23:19:03 UTC
Revision:      3f750f03d77e42327c5f9fcb9992110088330a32

Kotlin:        2.3.21
Groovy:        4.0.32
Ant:           Apache Ant(TM) version 1.10.17 compiled on April 6 2026
Launcher JVM:  21.0.13.0.3 (Ubuntu 21.0.13.0.3+1-1-24.04.1-Ubuntu)
Daemon JVM:    /usr/lib/jvm/java-21-openjdk-amd64 (no Daemon JVM specified, using current Java home)
OS:            Linux 6.12.113+ amd64

ANDROID_HOME set=yes
platforms=android-36 android-37.0 
build_tools=36.0.0 
qualityGate_exit=0
qualityGate_seconds=0
ALL_DONE qualityGate=0
```

## `gradle.properties`

sha256 `8770fd1a13a3dedb348751d088449fedc57c23f264392e2f28a3008053ad09c6`

```properties
org.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
android.builder.sdkDownload=false
```

## `gradle/libs.versions.toml`

sha256 `2f748ea0aadaedc54c4cc5c37cf19682c0a72dae8a12dc1262bfbb49dc09e4ba`

```toml
[versions]
agp = "9.5.1"
kotlin = "2.2.10"
composeBom = "2026.09.00"
navigation3 = "1.2.1"
adaptive = "1.3.0"
activity = "1.13.0"
serialization = "1.9.0"
lint = "32.5.1"
junit = "4.13.2"

[libraries]
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-foundation = { module = "androidx.compose.foundation:foundation" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
material-icons-core = { module = "androidx.compose.material:material-icons-core" }
activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity" }
adaptive = { module = "androidx.compose.material3.adaptive:adaptive", version.ref = "adaptive" }
adaptive-layout = { module = "androidx.compose.material3.adaptive:adaptive-layout", version.ref = "adaptive" }
adaptive-navigation3 = { module = "androidx.compose.material3.adaptive:adaptive-navigation3", version.ref = "adaptive" }
navigation3-runtime = { module = "androidx.navigation3:navigation3-runtime", version.ref = "navigation3" }
navigation3-ui = { module = "androidx.navigation3:navigation3-ui", version.ref = "navigation3" }
serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "serialization" }
junit = { module = "junit:junit", version.ref = "junit" }
lint-api = { module = "com.android.tools.lint:lint-api", version.ref = "lint" }
lint-checks = { module = "com.android.tools.lint:lint-checks", version.ref = "lint" }
lint-tests = { module = "com.android.tools.lint:lint-tests", version.ref = "lint" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

## `gradle/wrapper/gradle-wrapper.properties`

sha256 `e74d4f107d2feab989b9a041a02e2d2b85567ff48415c9c7add5ecc19abd19f9`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.6.0-bin.zip
networkTimeout=10000
retries=0
retryBackOffMs=500
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

## `gradlew`

sha256 `a5a5c199ba02189ae8c46a334223371a20599d9c298ef65e7540ede4a3f72d59`

```text
#!/bin/sh

#
# Copyright © 2015 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
#

##############################################################################
#
#   gradlew start up script for POSIX generated by Gradle.
#
#   Important for running:
#
#   (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
#       noncompliant, but you have some other compliant shell such as ksh or
#       bash, then to run this script, type that shell name before the whole
#       command line, like:
#
#           ksh gradlew
#
#       Busybox and similar reduced shells will NOT work, because this script
#       requires all of these POSIX shell features:
#         * functions;
#         * expansions «$var», «${var}», «${var:-default}», «${var+SET}»,
#           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
#         * compound commands having a testable exit status, especially «case»;
#         * various built-in commands including «command», «set», and «ulimit».
#
#   Important for patching:
#
#   (2) This script targets any POSIX shell, so it avoids extensions provided
#       by Bash, Ksh, etc; in particular arrays are avoided.
#
#       The "traditional" practice of packing multiple parameters into a
#       space-separated string is a well documented source of bugs and security
#       problems, so this is (mostly) avoided, by progressively accumulating
#       options in "$@", and eventually passing that to Java.
#
#       Where the inherited environment variables (DEFAULT_JVM_OPTS, JAVA_OPTS,
#       and GRADLE_OPTS) rely on word-splitting, this is performed explicitly;
#       see the in-line comments for details.
#
#       There are tweaks for specific operating systems such as AIX, CygWin,
#       Darwin, MinGW, and NonStop.
#
#   (3) This script is generated from the Groovy template
#       https://github.com/gradle/gradle/blob/3d91ce3b8caaf77ad09f381f43615b715b53f72c/platforms/jvm/plugins-application/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
#       within the Gradle project.
#
#       You can find Gradle at https://github.com/gradle/gradle/.
#
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
# Discard cd standard output in case $CDPATH is set (https://github.com/gradle/gradle/issues/25036)
APP_HOME=$( cd -P "${APP_HOME:-./}" > /dev/null && printf '%s\n' "$PWD" ) || exit

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac



# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi
        # Roll the args list around exactly as many times as the number of
        # args, so each arg winds up back in the position where it started, but
        # possibly modified.
        #
        # NB: a `for` loop captures its iteration list before it begins, so
        # changing the positional parameters here affects neither the number of
        # iterations, nor the values presented in `arg`.
        shift                   # remove old arg
        set -- "$@" "$arg"      # push replacement arg
    done
fi


# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Collect all arguments for the java command:
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and optsEnvironmentVar are not allowed to contain shell fragments,
#     and any embedded shellness will be escaped.
#   * For example: A user cannot expect ${Hostname} to be expanded, as it is an environment variable and will be
#     treated as '${Hostname}' itself on the command line.

set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
        "$@"

# Stop when "xargs" is not available.
if ! command -v xargs >/dev/null 2>&1
then
    die "xargs is not available"
fi

# Use "xargs" to parse quoted args.
#
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
#
# In Bash we could simply go:
#
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#   set -- "${ARGS[@]}" "$@"
#
# but POSIX shell has neither arrays nor command substitution, so instead we
# post-process each arg (as a line of input to sed) to backslash-escape any
# character that might be a shell metacharacter, then use eval to reverse
# that process (while maintaining the separation between arguments), and wrap
# the whole thing up as a single "set" statement.
#
# This will of course break if any of these variables contains a newline or
# an unmatched quote.
#

eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"
```

## `settings.gradle.kts`

sha256 `52a73a9bbe477b142ffc0743f129547e848e552358d5dc2a92e6c64336148461`

```kts
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "android-skill-lint-lab"
include(":app", ":checks", ":legacy", ":legacy-regression")
```
