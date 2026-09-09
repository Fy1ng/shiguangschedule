# 桌面小组件：时间段开关与列表滚动

## 源码与改动

- 上游：https://github.com/XingHeYuZhuan/shiguangschedule
- 基于 main 提交：`b137e35`。
- 本地分支：`feat/widget-time-toggle-scroll`。
- 设置入口：设置 → 通用设置 → **桌面小组件显示课程时间段**。
- 默认开启；关闭后隐藏课程起止时间，重新开启恢复。通过 DataStore 保存，沿用现有设置同步流程刷新小组件。

| 小组件 | 隐藏时间段 | 上下滚动 |
| --- | --- | --- |
| 超小 | 支持 | 仍只显示最近一节课 |
| 紧凑 | 支持 | 课程列表滚动，标题固定 |
| 双日 | 支持 | 今日、明日独立滚动，日期与数量固定 |
| 纵向列表 | 支持 | 课程列表滚动，标题固定 |

只改变小组件的显示方式，课程时间数据、提醒时间和应用内课表不受此设置影响。

## 实现说明

原课程容器是 `LinearLayout`，超出可用高度的课程被裁切。现使用有明确可用高度的 `ListView`，通过 `androidx.core:core-remoteviews:1.1.0` 的 `RemoteViewsCompat.setRemoteAdapter` 绑定课程。

- 按小组件 ID 和列表 ID 分别绑定，兼容多个小组件实例及双日两栏。
- 在 Manifest 声明带 `BIND_REMOTEVIEWS` 权限的兼容服务，支持旧 Android 系统。
- 列表行通过 PendingIntent 模板和 fill-in intent 保留点击打开应用的行为。
- 通用课程行使用权重布局，隐藏时间后地点获得剩余宽度；纵向课程行隐藏整个时间容器。浅色、深色资源均处理。
- 快照增加 `hide_course_time` 字段，默认 false，旧快照仍显示时间。
- 三种列表共享课程行渲染代码，避免开关行为不一致。

官方参考：

- https://developer.android.com/develop/ui/views/appwidgets/collections
- https://developer.android.com/jetpack/androidx/releases/core
- https://github.com/androidx/androidx/blob/androidx-main/core/core-remoteviews/src/main/java/androidx/core/widget/RemoteViewsCompat.kt

## 验证状态（2026-09-09）

已完成静态检查：13 个新增/修改 XML 文件解析通过；四个列表均使用受限高度；新增字符串各语言唯一且导入完整；依赖目录引用有效；移除列表上的直接子视图增删操作；`git diff --check` 通过。

新增但尚未运行：

- `WidgetTimePreferenceTest`：旧设置/快照默认值，以及关闭和恢复状态的读取与序列化。
- `WidgetCourseListTest`：深浅色下隐藏/恢复时间、回收地点空间、教师显示重置，以及双日列表高度和独立滚动。

编译尝试在 Gradle 下载阶段失败：`Network is unreachable`。当前环境只有 JDK 17，未配置 Android SDK；项目要求 JDK 21、Android SDK 37、Gradle 9.7.1。因此还没有编译成功的 APK，也未完成真实桌面宿主的运行验证。

## 后续构建与手机验收

### GitHub Actions

已新增 `.github/workflows/widget-apk.yml`，推送 `feat/widget-time-toggle-scroll` 分支自动构建，也支持手动触发。此工作流安装 JDK 21 和 Android SDK 37，准备教务离线资源，构建调试 APK 和设备测试 APK，并运行共享层 JVM 测试。设备测试仅编译，实际运行仍需要设备或模拟器。

构建不依赖原作者的 Release-Signing 环境或签名 Secrets。成功后在 Actions 对应任务的 Artifacts 中下载 `shiguangschedule-widget-apk-<提交号>`，手机通常选择文件名含 `arm64-v8a` 的 APK。

调试签名由运行器生成，后续不同构建的签名可能不同，不保证可以直接覆盖安装上一版调试包。

当前 GitHub 连接确认账号为 `Fy1ng`，但未提供创建 fork 的接口，且查询 `Fy1ng/shiguangschedule` 返回 404。工作流已在本地准备，尚未上传或触发。需要先在 GitHub 创建该 fork 并启用 Actions，再推送本地分支。

### 本地构建

准备 JDK 21、Android SDK 37，并在 `local.properties` 中设置 `sdk.dir`，使用项目自带 Gradle Wrapper。

```bash
./gradlew :androidApp:assembleDebug :shared:jvmTest
./gradlew :androidApp:connectedDebugAndroidTest
```

Windows 将 `./gradlew` 替换为 `gradlew.bat`。第二条命令需要已连接的设备或模拟器。

手机验收：

1. 导入一天至少 6 节课的数据，分别添加双日、紧凑和纵向小组件，并缩小高度。向上滑动应能看到最后一节课，再向下滑动能返回顶部。
2. 双日小组件只滑动今日栏，明日栏及日期标题应保持原位。点击课程应打开应用。
3. 关闭设置开关，确认四种样式不显示时间、地点获得更多宽度；重新开启应立即恢复。切换深浅色、重启应用后重复确认。
4. 同时添加两个同款小组件，确认更新无串栏；检查无课、假期、今日结束后明日预告等状态。
5. 至少在一个 Android 8–12 设备/模拟器和一个 Android 12L 以上设备验证；用户手机的系统桌面需单独确认手势兼容性。

调试 APK 使用调试签名，通常无法直接覆盖官方安装包；安装前先导出课表备份。
