# Arm B（未执行）

与 Arm A 使用同一任务、同一三份 Skill、同一工具链。

第一轮生成并封存，和 A 一样不看 Lint。第二轮只允许阅读 `:app:lintDebug` 的原始输出并提交修复。

禁止：

- 修改 `checks/` 或问题严重级别
- 更新 baseline
- 添加 suppression
- 把检测器作者知道判据后手写的 demo 算进这个臂

performance skill 两臂都不加载。
