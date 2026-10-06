# Docker Compose 部署

## 生产入口模式

`compose.production.yaml` 默认不包含 Miora Nginx/proxy，也不占用 VPS 的 80/443。公网域名、TLS 与访问策略由 1Panel、Nginx、Caddy、Nginx Proxy Manager、Cloudflare Tunnel 或其他网关负责。

| 服务 | 容器端口 | 默认宿主机绑定 | 默认端口 |
| --- | --- | --- | --- |
| Blog | 9001 | 127.0.0.1 | 9661 |
| Admin | 80 | 127.0.0.1 | 9662 |
| Server API | 9003 | 127.0.0.1 | 9663 |
| MySQL | 3306 | 不映射 | 无 |

Blog/Admin 继续使用相对 `/api`。每个公网域名均须把 `/api/` 与 `/static/upload/` 转发至 Server；其余路径转发至各自前端。

## 启动

```bash
cp .env.example .env
chmod 600 .env
# 填写 IMAGE_TAG、数据库密码和 JWT_SECRET_KEY
docker compose --env-file .env -f compose.production.yaml pull
docker compose --env-file .env -f compose.production.yaml up -d --remove-orphans
```

默认环境变量为 `BLOG_BIND_ADDRESS=127.0.0.1`、`ADMIN_BIND_ADDRESS=127.0.0.1`、`SERVER_BIND_ADDRESS=127.0.0.1`。可按部署拓扑调整 `*_BIND_ADDRESS` 与端口；不建议使用 `0.0.0.0` 将应用端口直接暴露公网。

## 反向代理

宿主机 Nginx/Caddy 直接代理 `127.0.0.1:9661`、`127.0.0.1:9662`、`127.0.0.1:9663`，示例见 `deploy/reverse-proxy/`。

1Panel/NPM 等容器化代理不能访问宿主机回环地址。推荐将代理容器加入 `miora-edge` 网络（项目名改变时网络名随之改变），并使用 `blog:9001`、`admin:80`、`server:9003` 作为上游。MySQL 只连接内部 `backend` 网络，不会暴露给宿主机或 edge 网络。若面板无法共享网络，才把 `*_BIND_ADDRESS` 改为经防火墙保护、对代理容器可达的地址。

## 可选 standalone

仅新 VPS 没有已有反向代理时，才叠加 Caddy standalone：

```bash
docker compose --env-file .env -f compose.production.yaml -f compose.standalone.yaml up -d
```

该模式才会占用 80/443；要求 `BLOG_HOST` 与 `ADMIN_HOST` 已解析到 VPS，并由 Caddy 申请 HTTPS 证书。已有 1Panel/NPM/Caddy 时不要使用。

## 数据库

生产 Compose 不会导入 `server/ThriveX.sql`。Miora 数据库初始化/迁移属于独立后续阶段；空数据库尚不可作为正式上线状态。

## 数据库初始化

Miora 使用 Flyway 自动执行版本化迁移。空数据库启动时会创建 22 张业务表和中性基础配置；不会导入 ThriveX 内容、用户、历史 Token 或默认密码。

首次部署必须在受保护的 .env 中设置 BOOTSTRAP_ADMIN_USERNAME 与至少 12 位的 BOOTSTRAP_ADMIN_PASSWORD。Server 仅在 user 表为空时创建该管理员；密码不会写入迁移文件。首次登录后应立即在初始化向导修改凭据并从部署环境删除 BOOTSTRAP_ADMIN_PASSWORD。

旧 ThriveX 数据库不提供自动迁移。上线 Miora 应使用空数据库，旧数据库请先独立备份。
