# CI 建议

`.github/workflows/quality-gate.yml` 定义了两步：

1. `./gradlew qualityGate`，期望退出码 0
2. `./gradlew :legacy-regression:lintDebug`，期望退出码非 0

工作流文件在仓库里，不表示 GitHub 已经把它设成 protected branch 的 required check。本次没有仓库管理员权限去打开分支保护，因此不能声称合并会被这条检查拦住。

本地或代理文件里的命令也拦不住跳过 hook 的提交。

要挡住「改检测器或刷新 baseline 放行」，还需要这些路径的评审，例如 `checks/**`、`**/lint.xml`、`**/lint-baseline.xml`。同一变更集里如果同时放松断言，CI 退出码仍然会是 0。这是门禁的已知缺口，不是已经解决的问题。

下面只是分支保护的配置草稿，没有被执行：

- 保护 `main`
- 要求 `quality-gate` 这个 job 的 check 通过才能合并
- 要求对检测器和 baseline 路径有指定评审人
- 不允许管理员用刷新 baseline 代替修复，除非评审记录写明偿还了哪些历史问题

Lint 通过不等于功能、视觉或安全通过。
