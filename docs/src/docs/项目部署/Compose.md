# Docker Compose 部署

根目录的 `compose.yaml` 会启动 MySQL、后端 Server、Blog、Admin 与 Nginx 入口代理。对外只开放 Nginx 的 HTTP 端口：博客使用 `BLOG_HOST`，控制端使用 `ADMIN_HOST`；两者的 `/api/` 与本地上传文件请求由 Nginx 转发给后端。

## 环境变量

复制根目录样例并限制权限：

```bash
cp .env.example .env
chmod 600 .env
```

至少替换 `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD` 与 `JWT_SECRET_KEY`。可用 `openssl rand -hex 32` 生成 JWT 密钥。生产环境还需要将 `BLOG_HOST` 和 `ADMIN_HOST` 分别解析到此服务器，并根据实际监听端口设置 `HTTP_PORT`。

| 变量 | 必填 | 说明 |
| --- | --- | --- |
| `COMPOSE_PROJECT_NAME` | 建议 | 固定容器、网络和命名卷前缀，升级和恢复时不要随意改动。 |
| `MYSQL_DATABASE`、`MYSQL_USER` | 是 | 首次初始化时创建的业务数据库与用户。 |
| `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD` | 是 | 数据库业务用户与 root 密码。 |
| `JWT_SECRET_KEY` | 是 | 后端 JWT 签名密钥；更换后所有已登录会话失效。 |
| `HTTP_PORT` | 否 | Nginx 对外 HTTP 端口，默认 `80`。 |
| `BLOG_HOST`、`ADMIN_HOST` | 是 | 博客与控制端的域名，Nginx 根据 Host 分流。 |
| `STORAGE_PROVIDER` | 否 | 默认 `local`；只有配置完整 S3 参数后才设为 `s3`。 |
| `FILE_PUBLIC_URL` | 否 | 同域 Compose 部署留空，文件 URL 为 `/static/upload/...`；后端独立暴露时才填写公开根地址。 |

S3 变量仅由后端容器读取。不要把 Access Key 或 Secret Key 放进前端构建变量、数据库或 Git；OCI 与腾讯 COS 的具体填写方式见[对象存储](./API/对象存储)。

## 首次启动

1. 安装 Docker Engine 与 Docker Compose 插件，克隆项目后进入仓库根目录。
2. 按上一节创建并编辑 `.env`，确认 DNS 已指向服务器；测试环境可使用默认 `localhost` 与 `admin.localhost`。
3. 构建并后台启动：

   ```bash
   docker compose up -d --build
   docker compose ps
   ```

4. 等待所有服务显示为 `healthy`。首次运行时 MySQL 会在空的 `mysql-data` 命名卷中导入 `server/ThriveX.sql`。
5. 访问 `https://$BLOG_HOST` 和 `https://$ADMIN_HOST`。本 Compose 只提供 HTTP 入口；生产 HTTPS 应由上游网关、CDN 或扩展的 TLS Nginx 配置终止。

常用排错命令：

```bash
docker compose logs -f proxy server
docker compose ps
```

## 数据持久化

| 命名卷 | 内容 | 说明 |
| --- | --- | --- |
| `${COMPOSE_PROJECT_NAME}_mysql-data` | MySQL 数据目录 | SQL 初始化脚本只会在该卷第一次创建时执行。 |
| `${COMPOSE_PROJECT_NAME}_upload-data` | `local` 模式上传文件 | 容器重建不会删除该卷；使用 S3 时文件数据在 bucket 中。 |

不要执行 `docker compose down -v`，除非确认要同时删除数据库和本地上传文件。

## 升级

升级前先完成下一节备份。随后在仓库根目录执行：

```bash
git pull
docker compose up -d --build --remove-orphans
docker compose ps
```

该命令会重建变更的服务，但保留两个命名卷。升级后检查 `server`、`blog`、`admin` 与 `proxy` 的健康状态，再访问博客、控制端和上传文件。若升级涉及数据库结构变更，必须先阅读该版本的迁移说明；本一期不提供自动数据库迁移。

## 备份

在宿主机创建受保护的备份目录后，导出数据库和本地上传文件：

```bash
mkdir -p backups
docker compose exec -T mysql sh -c 'exec mysqldump --single-transaction -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' > backups/thrivex-$(date +%F).sql
docker compose exec -T server tar -C /app/upload -czf - . > backups/upload-$(date +%F).tar.gz
```

将两个备份文件复制到独立于此服务器的安全位置。S3 模式不使用 `upload-data` 保存业务文件，应按云厂商策略对 bucket 或对象版本进行备份。

## 恢复

恢复会覆盖数据。先停止除数据库外的应用，并确认当前数据已经另行备份：

```bash
docker compose stop proxy blog admin server
docker compose exec -T mysql sh -c 'exec mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' < backups/thrivex-YYYY-MM-DD.sql
```

如需恢复本地上传文件，将上传压缩包解压到 `/app/upload`：

```bash
docker compose start server
docker compose exec -T server tar -xzf - -C /app/upload < backups/upload-YYYY-MM-DD.tar.gz
docker compose up -d
```

该文件恢复会覆盖同名文件但不会自动清除压缩包中不存在的旧文件；需要精确回滚时，应先在停机状态下清空 `upload-data` 卷内容，并确认备份完整后再解压。数据库与文件恢复完成后，检查容器健康状态并验证文章、图片和控制端登录。
