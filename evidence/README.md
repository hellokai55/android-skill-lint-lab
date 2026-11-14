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
