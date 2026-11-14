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
