# 贡献指南

Miora 使用单体仓库管理 Server、Admin、Blog、Docs 与部署配置。提交前请先阅读 [协作与发布工作流](MIORA_GITHUB_COLLABORATION_AND_RELEASE_WORKFLOW.md)。

## 分支流程

从 `dev` 创建短期 `feature/<topic>` 分支。功能完成后提交 PR 到 `dev`，通过代码审查与 CI 后，再由 `dev` 提交 PR 到受保护的 `main`。不要直接推送 `dev` 或 `main`。

## 本地验证

按改动范围执行对应检查：

- Server：`mvn -pl blog -am test`（在 `server/` 中执行）；
- Admin：`npm run lint && npm run build`（在 `admin/` 中执行）；
- Blog：`npm run lint && npm run build`（在 `blog/` 中执行）；
- Compose：`docker compose --env-file .env.example config --quiet`（在仓库根目录执行）。

不要提交 `.env`、凭据、数据库转储、上传文件或构建产物。涉及许可证、原作者致谢或外部服务地址的变更，必须在 PR 中说明处理决定。
