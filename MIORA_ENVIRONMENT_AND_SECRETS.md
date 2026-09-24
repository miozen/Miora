# Miora 测试、预发布环境与密钥约定

## 已提交的样例

- `environments/test.env.example`：测试环境的非敏感 Compose 配置。
- `environments/staging.env.example`：预发布环境的非敏感 Compose 配置。

两者都可作为 `docker compose --env-file <样例文件>` 的基础配置；真实运行时由进程环境覆盖和补齐密钥。复制出的 `environments/*.env` 已被 Git 忽略，权限应设为仅部署账号可读。

## GitHub Environments 与 Secrets

部署工作流必须使用 GitHub Environments `test` 或 `staging`，并在对应 Environment 中配置同名 Secrets；不得把 Secrets 写入仓库 Variables、Actions 日志、构建参数、前端环境变量或 `.env` 文件。

| Secret 名称             | 适用范围                 | 说明                                |
| ----------------------- | ------------------------ | ----------------------------------- |
| `MYSQL_PASSWORD`        | test、staging            | 业务数据库用户密码。                |
| `MYSQL_ROOT_PASSWORD`   | test、staging            | MySQL 管理员密码。                  |
| `JWT_SECRET_KEY`        | test、staging            | 后端 JWT 签名密钥；环境间必须不同。 |
| `STORAGE_S3_ACCESS_KEY` | 仅 `STORAGE_PROVIDER=s3` | S3 访问密钥。                       |
| `STORAGE_S3_SECRET_KEY` | 仅 `STORAGE_PROVIDER=s3` | S3 私密访问密钥。                   |

非敏感配置（域名、端口、数据库名、bucket 名、公开对象 URL）保留在对应样例或 GitHub Environment Variables。真正的部署工作流必须以如下方式注入密钥：

```yaml
environment: staging
env:
  MYSQL_PASSWORD: ${{ secrets.MYSQL_PASSWORD }}
  MYSQL_ROOT_PASSWORD: ${{ secrets.MYSQL_ROOT_PASSWORD }}
  JWT_SECRET_KEY: ${{ secrets.JWT_SECRET_KEY }}
  STORAGE_S3_ACCESS_KEY: ${{ secrets.STORAGE_S3_ACCESS_KEY }}
  STORAGE_S3_SECRET_KEY: ${{ secrets.STORAGE_S3_SECRET_KEY }}
```

当前 CI 不需要任何真实 Secrets。它只在 runner 内为 Compose 样例提供 `ci-validation-*` 占位值以验证变量解析，既不部署服务，也不输出或持久化这些值。后续 C.3 的部署工作流必须复用本约定。

## 操作边界

1. `test`、`staging` Environment 已于 2026-09-24 配置独立随机的 `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD` 与 `JWT_SECRET_KEY`；其值只可在 GitHub 中轮换，不得回显、下载或提交。两套环境均保持 `local` 存储，未配置 S3 Secrets。
2. 运行前以受保护的运行时环境注入 Secrets，并使用对应样例执行 Compose；不要将展开后的环境写入工作区、artifact 或日志。
3. 正式部署必须新建独立的 `production` Environment 和独立 Secrets；不得引用、复制或提升 test/staging 的值。
4. 变更数据库密码或 `JWT_SECRET_KEY` 前必须记录回滚计划；JWT 变更会使现有会话失效。
