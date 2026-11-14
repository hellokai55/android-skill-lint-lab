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
