# GHCR 生产镜像部署

本指南使用公开 GitHub Container Registry（GHCR）的 Miora 镜像。生产服务器不克隆仓库、不从源码构建，也**不需要**执行 `docker login ghcr.io`。

> 当前生产 Compose 只提供 HTTP 80 端口。公网使用前必须在上游网关或后续 HTTPS 配置中终止 TLS，并分别为 Blog、Admin 使用两个域名。

## 首次发布

1. 确认目标提交已通过 PR 合并到 `main`，且四项必需 CI 全绿。
2. 在 GitHub Actions 从 `main` 手动运行 **Release tag**：先以 `dry_run=true` 验证，再以 `dry_run=false` 创建新标签，例如 `v1.0.0`。
3. 标签自动触发 **Publish GHCR images**，复跑质量门禁并推送四个 `linux/amd64` 镜像。等待工作流成功。
4. 首次发布创建包后，在 GitHub **Packages** 中逐一打开 `miora-server`、`miora-blog`、`miora-admin`、`miora-proxy`，于 **Package settings** 将 Visibility 设为 **Public**。

之后 VPS 可匿名拉取版本镜像，无需 PAT 或 Pull Token。仅以发布标签 `vX.Y.Z` 部署，不使用 `latest` 或 `sha-*` 审计标签。

## 准备无源码服务器

服务器需要 Docker Engine 与 Docker Compose plugin。以部署账号建立只存 Compose 和受保护环境文件的目录：

```bash
install -d -m 700 ~/miora-deploy
cd ~/miora-deploy
curl -fsSLO https://raw.githubusercontent.com/miozen/Miora/v1.0.0/compose.production.yaml
```

把 URL 的 `v1.0.0` 换成实际发布标签。服务器中不要保留仓库工作树、`.git`、Dockerfile 或应用源码。

创建仅部署账号可读的 `.env`：

```bash
umask 077
${EDITOR:-vi} .env
chmod 600 .env
```

填写以下内容；尖括号值必须由部署者生成，不能提交、记录在工单或写入 GitHub Variables：

```dotenv
IMAGE_TAG=v1.0.0
COMPOSE_PROJECT_NAME=miora
MYSQL_DATABASE=miora
MYSQL_USER=miora
MYSQL_PASSWORD=<random-database-password>
MYSQL_ROOT_PASSWORD=<different-random-root-password>
JWT_SECRET_KEY=<openssl-rand-hex-32-output>
BLOG_HOST=blog.example.com
ADMIN_HOST=admin.example.com
HTTP_PORT=80
STORAGE_PROVIDER=local
FILE_PUBLIC_URL=
```

用 `openssl rand -hex 32` 生成 JWT 密钥。启用 S3 时，在相同受保护 `.env` 填写 S3 变量；密钥边界见 `MIORA_ENVIRONMENT_AND_SECRETS.md`。

## 首次启动与数据初始化

```bash
docker compose --env-file .env -f compose.production.yaml pull
docker compose --env-file .env -f compose.production.yaml up -d --remove-orphans
docker compose --env-file .env -f compose.production.yaml ps
```

生产 Compose 不会挂载或自动导入 `server/ThriveX.sql`。首次空数据库必须先走经审查的数据库初始化/迁移流程；在该流程确定前，不要把服务器直接暴露给用户或自行把开发 SQL 挂入容器。启动后确认服务均为 `healthy`，再完成 DNS、HTTPS 和后台访问边界。

```bash
docker compose --env-file .env -f compose.production.yaml logs -f proxy server
docker compose --env-file .env -f compose.production.yaml ps
```

## 升级与回滚

升级前备份数据库和本地上传卷。把 `.env` 的 `IMAGE_TAG` 改为新的已发布版本，再执行：

```bash
docker compose --env-file .env -f compose.production.yaml pull
docker compose --env-file .env -f compose.production.yaml up -d --remove-orphans
docker compose --env-file .env -f compose.production.yaml ps
```

回滚时把 `IMAGE_TAG` 改回上一个已验证的 `vX.Y.Z`，重复同样的 `pull` 与 `up`。不要运行 `docker compose down -v`，它会删除 MySQL 和本地上传数据卷。
