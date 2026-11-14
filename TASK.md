# 任务

在这个仓库里实现一个可安装的 Kotlin / Jetpack Compose 电影列表。只阅读下面三份已经放进仓库的说明，以及它们点名的同目录参考文档：

- `third_party/android-skills/jetpack-compose/adaptive/SKILL.md`
- `third_party/android-skills/navigation/navigation-3/SKILL.md`
- `third_party/android-skills/system/edge-to-edge/SKILL.md`

不要阅读其他分支、tag、`git log` 或其他提交。不要从本提交之外把文件拷进工作区。工具链和依赖版本已经在 Gradle 里钉死，不要改版本号。

产品要求：

- 至少 30 条电影，数据放在应用里，不访问网络，不使用密钥
- 海报用本地绘制的占位图，并标明这不是真实海报
- 有列表和详情
- 加载、空、错误三种状态可以稳定复现，而且不靠网络
- 窄窗口是单栏；更宽的窗口按所附 Navigation 3 文档做列表-详情
- 界面按所附 edge-to-edge 说明处理系统栏
- 旋转或调整窗口后，滚动位置和当前选中的电影还在
- 不把界面锁死在一个方向上
- 应用要能 `./gradlew :app:assembleDebug` 编过

做完后把改动留在当前分支的工作区，不要推到 `main`。
