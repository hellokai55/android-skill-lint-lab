# 电影列表

用仓库里已经装好的三份 Android skills 实现 `TASK.md`。

- `third_party/android-skills/jetpack-compose/adaptive/SKILL.md`
- `third_party/android-skills/navigation/navigation-3/SKILL.md`
- `third_party/android-skills/system/edge-to-edge/SKILL.md`

版本来源是 android/skills v1.0.21，commit `5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d`。许可证见 `third_party/android-skills/LICENSE.txt`。

构建使用仓库里的 Gradle Wrapper。需要 Android SDK，平台 `android-37.0`，以及 JDK 17 或更高。本机 SDK 写在不提交的 `local.properties` 的 `sdk.dir`。

```bash
./gradlew :app:assembleDebug
```

只按 `TASK.md` 工作。不要看其他分支或 git 历史。
