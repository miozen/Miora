# Miora GHCR 镜像命名约定

## 命名空间与镜像

GitHub Container Registry 命名空间固定为仓库所有者的小写名 `miozen`。后续 C.2/C.3 使用以下镜像，不创建“单一万能镜像”：

| Compose 服务 | GHCR 镜像                     |
| ------------ | ----------------------------- |
| `server`     | `ghcr.io/miozen/miora-server` |
| `blog`       | `ghcr.io/miozen/miora-blog`   |
| `admin`      | `ghcr.io/miozen/miora-admin`  |
| `proxy`      | `ghcr.io/miozen/miora-proxy`  |

`mysql` 继续使用上游 `mysql:8.0`，不镜像重发布。

## 标签与可追溯性

- Git 发布标签与生产镜像标签一一对应，均使用 `vX.Y.Z`，例如 Git `v1.2.3` 对应四个镜像的 `:v1.2.3`。
- 生产 Compose 只能引用明确版本标签；不得使用 `latest`、未固定的分支标签或仅依赖本机 `build` 上下文。
- C.3 同时生成 `sha-<full-commit-sha>` 审计标签；生产部署仍以发布版本标签为准。后续 C.5/C.6 可进一步记录镜像 digest。
- 已决定四个 Miora GHCR 包均为 **Public**。VPS 匿名拉取版本镜像，不保存 GHCR Pull Token；镜像中仍不得包含任何密码、JWT、环境文件或其他密钥。仅发布工作流的镜像构建 job 获得 `packages: write`；其余工作流保持只读权限。

## 实施边界

本文件只确定稳定的名称与标签语义。C.2 负责生产 Compose 与 Dockerfile，C.3 已负责标签触发的构建/推送工作流，C.4 已确定公开包与匿名拉取。

## C.3 发布行为

- 推送严格匹配 SemVer 的 `vX.Y.Z` Git 标签时，工作流先复跑四项质量门禁，再构建 Server、Blog、Admin、Proxy 四个 `linux/amd64` 镜像并推送到 GHCR。
- 工作流会拒绝不在 `main` 可达历史中的标签，避免从功能分支发布生产镜像。
- 从 `main` 手动触发 `Publish GHCR images` 时只执行相同验证和构建，绝不登录或推送 GHCR；它用于发布前的无副作用 dry run。
- 不创建 `latest` 或分支浮动标签。镜像只获得发布版本标签和同一提交的 `sha-<full-commit-sha>` 审计标签。

## C.4 可见性与拉取

- 首次标签发布创建包后，仓库所有者必须在 GitHub 的每个 Package settings 中将四个 Miora 容器包设为 **Public**；发布前没有可配置的包。
- Public 后，VPS 使用 `docker compose pull` 即可匿名拉取；不得为此创建、保存或轮换 Pull Token。
- 公开的是镜像层和镜像元数据，不是运行时密钥。数据库密码、JWT 与 S3 凭据始终只存在于部署服务器的受保护环境文件或密钥管理系统。
