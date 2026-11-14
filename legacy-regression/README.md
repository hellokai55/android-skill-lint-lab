# 人工构造的新增违规

`NewDebtActivity` 只有一条映射规则违规：`remember { ScrollState(0) }`，源码里有标记 `NEW_DEBT_MARKER`。

这个模块没有 lint baseline。在 `lint.xml` 把该规则升为 error 之后，`./gradlew :legacy-regression:lintDebug` 应当以非 0 退出。

不要把这条违规补进 `:legacy` 的 baseline 来换取通过。那样做会掩盖新增问题，而不是偿还历史债务。
