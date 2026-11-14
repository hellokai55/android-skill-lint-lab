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
