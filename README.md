# HealthLSP

一个针对 **OPPO 健康（com.heytap.health）** 的 LSPosed 模块。

让“同步手机通知 / 应用通知”列表显示**全部已安装应用**，包括没有启动图标、没有界面的系统包（overlay、共享库、系统服务等），并额外提供搜索框。

## 功能

- 全量应用列表：`PackageManager.getInstalledApplications(0)` 的所有包都会进入通知同步列表（测试机 411 个包）。
- 搜索：同步页列表最上方新增“搜索应用”条目，点击后输入应用名 / 包名过滤，清空即恢复全部。
- 放行系统应用：绕过 OPPO 健康配置里的 `ignore` / `forceAbandon`，日历、电话等系统应用可以正常开关。
- 修正开关状态：让“已开启”的应用在推送判断中真正生效。

## 原理（OPPO 健康 6.10.15）

| Hook | 作用 |
| --- | --- |
| `ApplicationPackageManager.queryIntentActivities(Intent, int)` | 对外部启动器查询补全所有已安装包，避免被列表剔除 |
| `dg0.k.i()` | 把缺失的应用行注入通知开关数据库列表 |
| `com.heytap.health.watch.notification.impl.module.a.d(String)` | 让 `ignore` / `ignoreBand` 不排除已安装应用 |
| `NotificationConfigBean.getForceAbandon()` | 放行 `forceAbandon` 中的已安装应用 |
| `dg0.k.d(String)` | 数据库里开关为开启时，返回可推送 |
| `NotificationSyncFragment.onResume` / `f0` | 插入“搜索应用”条目并过滤列表 |

> 类名来自 OPPO 健康 6.10.15（versionCode 6101500）。其他版本可能需要调整。

## 测试环境

- Xiaomi 13 (fuxi)，Android 16 / SDK 36
- OPPO 健康 6.10.15_9e2a81e_261107
- KernelSU + Zygisk Next，LSPosed 2.1.1 (7790)

## 编译

需要 JDK 8+ 和 Android SDK（build-tools 含 aapt2 / d8 / zipalign / apksigner）。

```bash
export ANDROID_SDK_ROOT=/path/to/android-sdk
# 可选：BUILD_TOOLS_DIR、ANDROID_JAR、JAVAC、KEYTOOL
./build.sh
```

产物：`HealthLSP.apk`。首次编译会在当前目录生成 `keystore.jks`（密码 `android`，仅用于本地测试；开源发布请替换）。

## 使用

1. `adb install -r HealthLSP.apk`
2. 打开 LSPosed 管理器，启用 **HealthLSP**。
3. 作用域只勾选 **OPPO 健康 / com.heytap.health**（不需要“系统框架”）。
4. 强制停止并重新打开 OPPO 健康（或重启手机后再打开一次）。
5. 进入“设备 → 同步手机通知”，即可看到全部应用和搜索框。

首次全量加载需要为几百个包建库，可能需要十几秒；之后会快很多。如果重启后列表变少，强制停止并重开一次 OPPO 健康即可。

## 注意

- 这是针对特定应用版本的 Hook，应用升级后类名可能变化。
- 手机端放行后，手表端仍可能有自己的白名单/过滤；如仍然收不到系统应用通知，需要在手表 SystemUI 侧另行放行。
- 仅供个人学习与设备自用，请遵守当地法律法规与应用服务条款。

## License

MIT
