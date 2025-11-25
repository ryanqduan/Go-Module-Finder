# GoLand 插件：Go Module Finder

## 安装指南
- 使用 `./gradlew buildPlugin` 生成插件包，产物位于 `build/distributions/*.zip`。
- 在 GoLand 中打开 `Preferences → Plugins → ⚙ → Install Plugin from Disk...`，选择生成的插件包安装。
- 重启 IDE 后，在左侧工具窗口栏看到 `Go Module Finder`。

## 功能说明
- 模块搜索：支持按模块名称、作者、版本号关键词组合搜索，来源可选择 `官方仓库`、`goproxy.io`、`goproxy.cn` 或 `godoc`。
- 模糊匹配与自动补全：输入模块名称时自动联想常见模块路径（基于 `godoc` 搜索结果）。
- 下载管理：选择结果后点击 `下载`，执行 `go get <module>@<version>`，可选择最新稳定版或指定版本；显示下载进度与结果通知。
- 依赖与冲突：`go get` 由 Go 工具链解析依赖与冲突，插件提供过程可视化与日志输出。

## 使用步骤
- 打开工具窗口 `Go Module Finder`。
- 输入 `模块名称`（可选：`作者`、`版本号`），选择 `来源`，点击 `搜索`。
- 选择列表中的某个模块，必要时在 `版本号` 中填写指定版本，点击 `下载`。
- 下载完成后在通知气泡中查看结果，失败时查看详细日志。

## 配置选项
- `默认来源`：设置搜索与下载的默认代理来源。
- `go命令路径`：设置 `go` 命令路径，默认使用系统 `PATH` 中的 `go`。
- 入口：`Preferences → Tools → Go Module Finder`。

## 常见问题
- 无法搜索作者：公共 API 对作者维度支持有限，插件以 `godoc` 包搜索为主，作者过滤为关键词匹配。
- 无法解析版本：某些镜像可能不同步，切换 `来源` 或直接填入指定版本再下载。
- `go` 不在 PATH：在设置页填写 `go` 的绝对路径。

## 技术实现
- 遵循 JetBrains 插件开发规范，使用 `IntelliJ Platform Gradle Plugin` 构建，目标产品为 `GoLand 2025.1`。
- UI：工具窗口、表格展示、通知气泡、进度任务。
- 网络：`HttpRequests` 调用 `godoc` 搜索与 `GOPROXY` 版本查询。
- 下载：`GeneralCommandLine` 执行 `go get` 并捕获输出。
- 错误处理：统一捕获、日志记录、用户提示。

## 运行与测试
- 运行插件：`./gradlew runIde`。
- 构建插件包：`./gradlew buildPlugin`。
- 单元测试：`./gradlew test`。

## 示例场景
- 搜索 `gin`，选择 `官方仓库`，获取最新版本，一键下载。
- 在中国大陆网络环境下切换到 `goproxy.cn` 提升下载速度。
- 针对特定版本回退：在 `版本号` 中填入 `v1.9.0` 并执行下载。

