# GHCR 生产镜像部署

默认生产 Compose 使用公开的 `miora-server`、`miora-blog`、`miora-admin` 版本镜像，不需要源码或 Docker 登录；不再依赖 `miora-proxy`，也不占用 80/443。

```bash
install -d -m 700 ~/miora-deploy
cd ~/miora-deploy
curl -fsSLO https://raw.githubusercontent.com/miozen/Miora/vX.Y.Z/compose.production.yaml
curl -fsSLO https://raw.githubusercontent.com/miozen/Miora/vX.Y.Z/.env.example
cp .env.example .env
chmod 600 .env
# 编辑 .env：设置 IMAGE_TAG=vX.Y.Z 和所有密码
docker compose --env-file .env -f compose.production.yaml pull
docker compose --env-file .env -f compose.production.yaml up -d --remove-orphans
```

默认端口为本机 `127.0.0.1:9661`（Blog）、`9662`（Admin）、`9663`（Server）。宿主机网关使用这些地址；1Panel/NPM 等容器化网关应加入 `miora-edge` 网络，使用 `blog:9001`、`admin:80`、`server:9003`。两个站点都必须把 `/api/` 和 `/static/upload/` 指向 Server，保持相对 API 路径。

仅没有既有网关的全新 VPS 使用 `compose.standalone.yaml`；它会由 Caddy 占用 80/443 并要求真实域名。不要使用 `latest`、不要裸露高位端口、不要执行 `down -v`。

生产数据库不会自动导入旧 SQL；初始化/迁移另行实施。

## 数据库初始化

Miora 使用 Flyway 自动执行版本化迁移。空数据库启动时会创建 22 张业务表和中性基础配置；不会导入 ThriveX 内容、用户、历史 Token 或默认密码。

首次部署必须在受保护的 .env 中设置 BOOTSTRAP_ADMIN_USERNAME 与至少 12 位的 BOOTSTRAP_ADMIN_PASSWORD。Server 仅在 user 表为空时创建该管理员；密码不会写入迁移文件。首次登录后应立即在初始化向导修改凭据并从部署环境删除 BOOTSTRAP_ADMIN_PASSWORD。

旧 ThriveX 数据库不提供自动迁移。上线 Miora 应使用空数据库，旧数据库请先独立备份。
