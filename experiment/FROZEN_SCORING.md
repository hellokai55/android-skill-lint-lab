# 冻结评分标准

登记时间：生成样本开始之前。此后不得按样本观察结果修改检测器源码、问题 id、严重级别解释，或下面的适用性与抑制统计。检测器字节变了，这套分数就不能和登记时比较。

这不是模型结果。当前独立生成会话数仍是 0。

## 隔离限度

生成模板分支 `cursor/generation-template-9e09` 的起始提交是 `f7a580a50bdcc7a5d040a682107e3f8463a04560`。该分支历史只有 `main` 的初始提交和这个模板提交，工作树里没有参照应用、检测器、夹具、研究结论和评估日志。

这不是密码学隔离。同一远程上的其他 ref，包括本评估分支，仍然可以被 `git fetch` 和 `git log --all` 看见。统一任务已要求生成者不要读其他分支、tag 和历史。评分时不能把「任务里写了不要看」当成已经看不见。

## 不得当作自然样本的提交

| SHA | 原因 |
| --- | --- |
| `fe85943416ab8da24fc08c844b05c00a0e377c3f` | 空仓库初始提交 |
| `9441209aadafb1f33c3694643eed5880fa715177` | 手写参照应用与检测器 |
| `85d704ee50ffbac15cfd8ad9b4e07cd369814659` | 只追加 Notion 归档 |
| `f7a580a50bdcc7a5d040a682107e3f8463a04560` | 空模板，不是生成结果 |

参照应用这些文件的 sha256 若原样出现在样本的同一路径，样本整段拒绝，不计入 N：

| 路径 | sha256 |
| --- | --- |
| `app/src/main/java/com/example/movielab/data/Catalog.kt` | `c17750622572eb77759380ba13125cad9e934d1492a0e0c7cb807f4704b42237` |
| `app/src/main/java/com/example/movielab/ui/MovieApp.kt` | `78b4bedbcb901cca0ab07f72cb0687a3268b0f9c16b2d5db9902332a69970f60` |
| `app/src/main/java/com/example/movielab/MainActivity.kt` | `a9662fd7aa462639c476c7e97765f360de2ec45b7971ec6b68fc9daeab121b8e` |
| `app/src/main/java/com/example/movielab/ui/PlaceholderPoster.kt` | `55e8ca5265d9df5b6d8ae848438988c439d9fdeaa102ff435ba8895a6247ab59` |
| `app/src/main/java/com/example/movielab/ui/Theme.kt` | `2dbc78a54dc5b30c9f774d5676f9e300afca09c1882fdff8faf409dc18e303a3` |

## 检测器字节

评分只承认这些 sha256。文件在评估分支上，不在模板里。

| 文件 | sha256 |
| --- | --- |
| `checks/src/main/java/com/example/movielab/lint/ManifestAdaptationDetector.kt` | `6a021fcd3913b6dfb4566ad9bfd382254aa6e78f9a35654f6e826c697dc74f55` |
| `checks/src/main/java/com/example/movielab/lint/UnsaveableScrollRememberDetector.kt` | `5597ea1abac5026d47dcd54dce2e56c852f3ba4c485ddd69b94519c7254b7e50` |
| `checks/src/main/java/com/example/movielab/lint/AdaptivePaneScaffoldDetector.kt` | `b73aad2c22c0ed35de93d502079a4c20e3b66678a118402ce6c721fbfb5a8d80` |
| `checks/src/main/java/com/example/movielab/lint/MissingEnableEdgeToEdgeDetector.kt` | `9a6b6877f50bf25f6c7417f0fc02a62eb40e59d5829456bb2dda913d9ac14614` |
| `checks/src/main/java/com/example/movielab/lint/MovieLabIssues.kt` | `797e09eea5c50d4639b1cc0c11214e6a8febe2f9ee3f5122235be49f87076ccb` |
| `checks/src/main/java/com/example/movielab/lint/MovieLabIssueRegistry.kt` | `e68e711ec30f178a8fddd387943a19c352b76560be62454c3126bfc91729fda3` |

评分脚本 `scripts/score_sample_commit.py` 的 sha256 是 `c871e682162d856037f9aeaae63774cfc775385ef6a4f4d4d30cfefaa4890af3`。它只编排上述检测器，不改规则。

## 统一任务指纹

模板 `TASK.md` sha256：`a25588cccbc026c4e1e7bf1b7be297727e8395fe5edc5a6f9cc36ca333ad9a5c`。

模板 `gradle/libs.versions.toml` sha256：`7cc477319760cd072fa6976c3b163b23fd21f0fda5df3641e1fa8c43cdcfba82`。

模板 `app/src/main/java/com/example/movielab/MainActivity.kt` sha256：`411fadd8daf60ea1c16a9fa8b86adc5e44a8d9d3fd7cbf9c9677b2284b339026`。这个空骨架在 `setContent` 之前调用了 `enableEdgeToEdge()`，清单里没有方向锁，也没有滚动状态或旧双栏脚手架。这是可编译骨架，不是故意放进去的违规。若样本里该文件哈希仍是这个值，`MissingEnableEdgeToEdge` 的通过来自骨架，不能记成模型新写的行为。

版本：AGP 9.5.1，Gradle 9.6.0，Kotlin 2.2.10，Compose BOM 2026.09.00，Navigation 3 1.2.1，adaptive 1.3.0，activity 1.13.0，kotlinx-serialization 1.9.0，compileSdk/targetSdk 37，minSdk 24。

## 怎么评一个样本提交

在本评估分支的工作区执行，不要在模板分支上评：

```bash
python3 scripts/score_sample_commit.py \
  --sample <样本完整SHA> \
  --sample-kind model_arm_a \
  --out evidence/samples/<短SHA>.json
```

`sample-kind` 只能是 `model_arm_a`、`model_arm_b` 或 `model_arm_a_self_review`。

脚本会：

1. 核对检测器哈希，不一致就退出。
2. 拒绝上表里的非样本提交，以及含参照应用原文件哈希的提交。
3. 从评估分支 HEAD 做一个临时 worktree。评估分支上的 `app/src` 会先删掉，再换成样本提交里的 `app/src`。Gradle 与 `:checks` 仍用评估分支，这样样本自己的构建文件即使没挂上检测器，评分仍然用冻结检测器。
4. 若样本的 `gradle/libs.versions.toml` blob 与评估分支不同，JSON 里 `toolchain_drift` 为 true。不因此改规则。
5. 运行 `:app:assembleDebug`、`:app:testDebugUnitTest`、`:app:lintDebug`。编译失败、测试失败和 Lint 失败分开记在 `gradle_exit_code` 与规则状态里。基础设施失败不是代码通过。
6. 功能清单和视觉清单保持 `not_run_by_this_script`。Lint 通过不能写成功能或视觉通过。

## 适用性与状态

Lint 未抑制的报告是违规的权威计数。分母用下面的静态口径，登记后不改。

| id | 适用 | 分母 |
| --- | --- | --- |
| `AdaptiveScreenOrientationLock` | 主清单里至少有一个 `activity` 或 `activity-alias` | 这些元素的个数 |
| `AdaptiveConfigChangesOptOut` | 同上 | 同上。属性不存在算未违规，不是不适用 |
| `UnsaveableScrollRemember` | 主源码出现 `LazyColumn`、`LazyRow`、`LazyVerticalGrid`、`LazyHorizontalGrid`、`LazyVerticalStaggeredGrid`、`verticalScroll` 或 `horizontalScroll` | 有则 1，无则不适用 |
| `AdaptivePaneScaffoldBanned` | 主源码出现 `NavDisplay`、`ListDetailSceneStrategy`、`ListDetailPaneScaffold` 或 `SupportingPaneScaffold` | 有则 1，无则不适用 |
| `MissingEnableEdgeToEdge` | 主源码里非 `abstract` 的 `class`，且声明行的超类型包含 `ComponentActivity`、`AppCompatActivity`、`FragmentActivity` 或 `Activity` | 这些类的个数。正则会漏掉间接继承，漏报记在结果里，不回过头改检测器 |

状态：

- `not_applicable`：分母为 0。功能没做要另记功能失败，不能靠这个状态说功能通过。
- `fail`：未抑制的 Lint 条数大于 0。
- `suppressed`：未抑制条数为 0，但抑制记录大于 0。这不是通过。
- `pass`：适用，未抑制条数为 0，且抑制记录为 0。

## 函数级抑制

扫描样本 `app/src/main`：

- Kotlin/Java 的 `@Suppress`、`@SuppressLint`，以及 `@file:Suppress`。只认五个 id 的字符串字面量。
- `@file` 记为 `file`。注解后面的 `fun` 记为 `function` 并写下函数名；`class` 或 `object` 记为 `class`。
- 清单里 `activity` / `activity-alias` 的 `tools:ignore` 记为 `manifest_element`，名字用 `android:name`。
- 每条记录含 `issue_id`、路径、行号、`declaration_kind`、`declaration_name`。
- 抑制不从 Lint 的通过数里消失，单独计数。

`scripts/score_sample_commit.py` 的注解扫描不是完整 Kotlin 语法树。这是登记时锁定的统计口径。
