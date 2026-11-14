# experiment

源码快照 commit：`9441209aadafb1f33c3694643eed5880fa715177`

以下是该目录在该 commit 的完整文本，不是片段。二进制文件只出现在索引里。

## `experiment/fixtures/parser-smoke-lint.xml`

sha256 `c16b7ef61107171c284ba79f6c67a422f136bdd4f4cbce781bc3baa4d1e51a9d`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!-- PARSER FIXTURE. Not a lint run, not a model sample, not a result. -->
<issues format="6" by="lint">
    <issue id="UnsaveableScrollRemember" severity="Error" message="fixture only" />
</issues>
```

## `experiment/prompts/arm-a-skill-only.md`

sha256 `4a3bab752273a24871e14f3995ec7b4230e0545455f42300f85a8d356ffb7923`

```md
# Arm A（未执行）

在隔离会话中只提供 `experiment/prompts/task.md` 和那三份冻结 Skill。

会话看不到 `checks/` 的检测器实现，也不运行 `:app:lintDebug`，因此看不到自定义 Lint 反馈。

如果要声称 Lint 的独立效果，必须另有一轮等预算的 Skill 自审控制（Arm A2）：同一会话在没有 Lint 输出的情况下再改一轮，轮次或预算与 Arm B 的修复轮相同。没有这轮控制时，只能把「首轮未修」和「看到反馈后修」写成前后试验，不能写成严格因果 A/B。

首轮产物只读封存。不要回写。记录补丁、原始报告、命令退出码和墙钟时间。
```

## `experiment/prompts/arm-b-skill-plus-lint.md`

sha256 `d88758fbc1bb8d7e490e2737b0833a06ef712ac87123c29a882cc01063a4af00`

```md
# Arm B（未执行）

与 Arm A 使用同一任务、同一三份 Skill、同一工具链。

第一轮生成并封存，和 A 一样不看 Lint。第二轮只允许阅读 `:app:lintDebug` 的原始输出并提交修复。

禁止：

- 修改 `checks/` 或问题严重级别
- 更新 baseline
- 添加 suppression
- 把检测器作者知道判据后手写的 demo 算进这个臂

performance skill 两臂都不加载。
```

## `experiment/prompts/task.md`

sha256 `a271e1f13d978ada9a734c20726b2fcacfcb779768e3e6c16793b095f556d73a`

```md
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
```

## `experiment/runs/NOT_RUN.md`

sha256 `184c071174c1d15c711720133e034d28483608bc327a43e018a3b45d4b76b804`

```md
# 模型对照还没有跑

本环境没有被授权去抓取平台凭据或自行调用隔离的生成模型。撰写参考实现的代理会话不是盲测样本。

因此：

- 独立模型会话 N = 0
- 没有 Arm A / Arm B 产物
- `app/` 是手写参照实现
- `legacy/` 与 `legacy-regression/` 是手写夹具
- `:checks:test` 是检测器金标准，规则作者知道判据

这些都不能写成模型通过率、生成能力提升或严格因果 A/B。
```
