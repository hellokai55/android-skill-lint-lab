# Frozen upstream

These files are unmodified copies from the public repository
[android/skills](https://github.com/android/skills), used as the experimental
reference for this lab. They are not a team architecture standard, and they are
not Android Lint rules shipped by the Android Gradle Plugin.

| Item | Value |
| --- | --- |
| Tag | v1.0.21 |
| Commit | `5815d27fda564aa9f07ebdcb5e57ed3e8d904e0d` |
| Plugin version field | 1.0.21 (`meta/claude-plugin.json`) |
| License | Apache License 2.0, file `LICENSE.txt` |
| Copyright | Google LLC / The Android Open Source Project, as stated in the upstream files |
| Retrieved | 2026-11-14 |

## Files and upstream paths

| Vendored path | Upstream path at the frozen commit |
| --- | --- |
| `LICENSE.txt` | `LICENSE.txt` |
| `meta/claude-plugin.json` | `.claude-plugin/plugin.json` |
| `jetpack-compose/adaptive/SKILL.md` | `jetpack-compose/adaptive/SKILL.md` |
| `navigation/navigation-3/SKILL.md` | `navigation/navigation-3/SKILL.md` |
| `system/edge-to-edge/SKILL.md` | `system/edge-to-edge/SKILL.md` |
| `jetpack-compose/jetpack-compose-performance/SKILL.md` | `jetpack-compose/jetpack-compose-performance/SKILL.md` |
| `navigation/navigation-3/references/material-listdetail.md` | `navigation/navigation-3/references/android/guide/navigation/navigation-3/recipes/material-listdetail.md` |
| `navigation/navigation-3/references/basicsaveable.md` | `navigation/navigation-3/references/android/guide/navigation/navigation-3/recipes/basicsaveable.md` |
| `navigation/navigation-3/references/save-state.md` | `jetpack-compose/adaptive/references/android/guide/navigation/navigation-3/save-state.md` |

`save-state.md` is linked from the navigation-3 skill, but at this commit the blob is stored under the adaptive skill references. The copy above is that blob.

The performance skill is vendored because it was read while deciding scope. It is not loaded by either experimental arm and it is not enforced by the lab's Lint checks. See `docs/规则出处.md`.

Official documentation read alongside the snapshot:

- <https://developer.android.com/tools/agents/android-skills> (page fetched 2026-11-14; page footer on that fetch said "Last updated 2026-10-28 UTC")
- <https://developer.android.com/studio/write/lint>
- <https://googlesamples.github.io/android-custom-lint-rules/usage/baselines.md.html>
