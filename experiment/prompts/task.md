# 统一任务（尚未执行）

你在既有 Android 工程上改一个本地电影列表，而不是从零介绍 Skill。工程约束以仓库里冻结的官方参照为准，团队自己的埋点、路由风格、WebView 桥不在这次任务里。

## 冻结参照

只阅读这些文件，不要加载仓库以外的 Skill，也不要加载 performance skill：

- `third_party/android-skills/jetpack-compose/adaptive/SKILL.md`
- `third_party/android-skills/navigation/navigation-3/SKILL.md`
- `third_party/android-skills/system/edge-to-edge/SKILL.md`

上游是 android/skills v1.0.21，commit `5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d`。

## 任务

在 `app/` 模块里保持一个可编译的 Kotlin / Jetpack Compose 电影列表：

- 至少 30 条本地数据，不访问网络，不使用 key
- 海报只能是标明占位的本地图形
- 列表与详情；加载、空、错误三种状态可以稳定复现
- 窄窗口单栏，Expanded 宽度按 Navigation 3 `ListDetailSceneStrategy` 双栏
- `enableEdgeToEdge()` 在 `setContent` 之前
- 滚动和选中经过配置变化仍在；不锁方向，不用 `configChanges` 逃避重建
- 多栏用 SceneStrategy，不调用 `ListDetailPaneScaffold` 或 `SupportingPaneScaffold`

## 明确不做

- 不改 `checks/` 里的检测器
- 不新增或刷新 lint baseline 来消掉问题
- 不用 `@Suppress` 或 `tools:ignore` 放过上述规则
- 不把编译失败记成规则通过

## 样本

目标独立会话最终 N≥5，资源允许则 8。当前已执行会话数是 0。缺几个就记几个。
