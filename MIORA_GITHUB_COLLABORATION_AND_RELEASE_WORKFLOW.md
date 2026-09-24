# Miora GitHub 协作与发布工作流（固定规范）

> **状态：固定。** 本文档只能在项目所有者明确授权，或一次真实发布、部署或回滚故障完成复盘后修改；修改必须记录原因、影响范围、修订日期和批准结论。

## 适用范围

- GitHub 远端：`https://github.com/miozen/Miora.git`。
- 单体仓库中的 Server、Admin、Blog、Docs、Compose 与部署配置。
- GitHub Actions CI、GHCR 生产镜像，以及 VPS/内网服务器部署。

## 不可变原则

- 项目所有者提出需求，Codex 负责在约定范围内实现、验证并汇报；每轮结束必须给出已完成内容、验证结果、清单状态和下一轮实施计划。
- Codex 不得擅自扩大需求范围、覆盖既有未提交改动或提交敏感信息；需要产品决策、外部账号操作或超出约定范围的权限时，必须先说明阻塞并请求项目所有者决定。
- 未收到项目所有者明确的“推送/提交到 GitHub”授权前，只允许本地修改、检查和验证，不执行 `git push`。
- 首次推送和任何生产发布前，必须由人工确认待提交文件、diff、分支和目标远端。
- `.env`、生产密码、JWT/S3 凭据、数据库转储、上传文件和本机构建产物不得进入 Git 历史。
- 每次合并或发布必须能对应一个可验证的 Git 提交 SHA；生产部署使用不可变版本镜像标签，不使用漂浮的 `latest`。
- `main` 始终保持可部署，并启用分支保护；禁止直接推送。

## GitHub SSH 访问

本项目的 GitHub 远端统一使用 SSH-over-443，避免依赖 HTTPS 凭据或 22 端口：

```text
ssh://git@ssh.github.com:443/miozen/Miora.git
```

首次连接前，必须验证 `ssh.github.com:443` 的 ED25519 指纹为 `SHA256:+DiY3wvvV6TuJJhbpZisF/zLDA0zPMSvHdkr4UvCOqU`，再将主机密钥写入 `known_hosts`。认证通过后，使用 `git remote set-url origin` 设置上述地址；不得把 SSH 私钥、访问令牌或认证输出写入仓库或日志。

## 开发环境前置条件

- 前端依赖 Node.js 20；当前开发机使用 NVM 管理的 `v20.20.2`。在非交互 shell 或 CI 以外的命令环境中，先执行 `source "$HOME/.nvm/nvm.sh"`，再运行 `node`、`npm` 或前端验证命令。
- CI 使用独立的 Node 20 runner，不依赖开发机的 NVM 安装；Node/NPM 二进制路径、NVM 目录及任何本机凭据均不得提交。

## 环境样例与密钥

- 测试与预发布只提交非敏感样例：`environments/test.env.example`、`environments/staging.env.example`；复制出的运行时 `environments/*.env` 必须保持 Git 忽略并限制文件权限。
- `MYSQL_PASSWORD`、`MYSQL_ROOT_PASSWORD`、`JWT_SECRET_KEY` 以及启用 S3 时的两项访问密钥必须只保存在 GitHub Environment Secrets（`test`、`staging`）或受保护的部署运行时；它们不得进入仓库、日志、Artifact、前端构建变量或 GitHub Variables。
- CI 可使用仅存在于 runner 的无价值验证占位值检查 Compose 模板，但不获取、打印或持久化真实 Secrets。部署工作流必须声明对应 GitHub Environment，并通过 `${{ secrets.NAME }}` 注入密钥；具体名称和操作边界见 `MIORA_ENVIRONMENT_AND_SECRETS.md`。

## GHCR 镜像命名

- GHCR 命名空间固定为 `ghcr.io/miozen`；Server、Blog、Admin、Proxy 分别使用 `miora-server`、`miora-blog`、`miora-admin`、`miora-proxy`。完整映射与版本语义以 `MIORA_GHCR_IMAGE_CONVENTION.md` 为准。
- 生产镜像必须使用与 Git 发布标签对应的 `vX.Y.Z` 标签，不得使用 `latest` 或不固定的分支标签。C.2/C.3/C.4 分别实现生产 Compose、构建推送和包访问控制。
- `compose.production.yaml` 只允许使用版本镜像、命名卷和运行时环境变量；禁止 `build`、源码目录与本地 Nginx 配置挂载。数据库初始化必须走经审查的迁移流程，不从部署源码树挂载 SQL。

## 协作回合契约

1. 项目所有者负责提出目标、优先级和必要的产品/运维决策；Codex 负责拆分为清单中的单个可验收子任务，并在该范围内完成实现和测试。
2. 每轮只实施一个编号子任务；若用户明确追加的工作是该子任务的必要文档、测试或验收记录，可在同一轮完成。未完成不得跳项或标记完成。
3. Codex 每轮的交付说明必须包含：任务编号、修改的文件与行为、执行的验证及结果、清单状态、下一编号任务和已知风险。验证未通过时，如实说明，不得以“完成”替代。
4. 本地提交、推送、创建 PR、发布镜像和部署属于独立外部状态变更：仅在项目所有者明确授权且完成相应 diff/目标确认后执行。完成后回报提交 SHA、PR/Actions 链接或部署版本。
5. 本契约适用于后续所有 Miora 改造回合；变更本节必须由项目所有者明确提出，并在下方修订记录中登记。

## 分支流转

```text
feature/<topic> -- PR --> dev -- PR --> main -- 手动发布 --> vX.Y.Z
                                  |            |                |
                                  |            |                +-- C.3：GHCR 镜像构建与推送
                                  |            +-- CI：发布前质量门禁
                                  +-- CI：集成质量门禁
```

1. 从 `dev` 创建短期 `feature/<topic>` 分支；一项独立改造对应一个分支或一组可回滚提交。
2. 提交 PR 到 `dev`。Actions 只执行质量门禁：后端 Maven 测试、Admin lint/build、Blog lint/build、Compose 校验；不推送 GHCR 生产镜像。
3. `dev` 的改动通过测试和人工复查后，以 PR 合并到受保护的 `main`。
4. `main` 的合并提交通过完整验证后，由项目所有者从 `main` 手动触发“Release tag”工作流，提供严格的 `vX.Y.Z` SemVer 版本号。工作流复跑完整质量门禁后才创建注释 Git tag；默认 `dry_run`，显式关闭后才允许创建标签。
5. B.3 只创建 Git tag，不构建或推送 GHCR 镜像；镜像发布由 C.3 接入版本标签触发器后才启用。
6. 生产/内网服务器的生产 Compose 固定使用该版本镜像标签，执行 `docker compose pull` 后启动；回滚时改回已验证的旧标签。

### 分支创建与紧急修复

- 常规改动必须从最新 `dev` 创建 `feature/<topic>`，并以 PR 合并回 `dev`；不得直接推送 `dev` 或 `main`。
- 只有通过 `dev` 集成验证的改动才可由 `dev` 发起 PR 到 `main`。`main` 合并后再发布版本标签。
- 紧急修复从最新 `main` 创建 `hotfix/<topic>`，以 PR 合并到 `main`；修复发布后必须立即创建 `main` 到 `dev` 的回补 PR，避免两个分支分叉。
- 每个 PR 说明必须包含影响范围、验证结果、回滚方式和关联清单任务；CI 建立后，未通过必需检查的 PR 不得合并。

## GitHub Actions 触发边界

| 事件                                           | 验证                 | 构建并推送 GHCR | 用途                             |
| ---------------------------------------------- | -------------------- | --------------- | -------------------------------- |
| `feature/*` 到 `dev` 的 PR                     | 是                   | 否              | 代码审查与集成验证               |
| 推送 `dev`                                     | 可选复跑             | 否              | 开发分支稳定性                   |
| `dev` 到 `main` 的 PR                          | 是                   | 否              | 上线前门禁                       |
| 合并到 `main`                                  | 是                   | 否              | 生产候选，等待项目所有者选择版本 |
| 从 `main` 手动触发 Release tag（默认 dry run） | 是，复跑四项质量门禁 | 否              | 校验版本号与发布前验证           |
| 从 `main` 手动触发 Release tag（关闭 dry run） | 是，复跑四项质量门禁 | 否              | 创建不可变 `vX.Y.Z` 注释标签     |
| 推送 `vX.Y.Z` 标签                             | 是，复跑四项质量门禁 | 是              | 发布四个 `linux/amd64` GHCR 镜像 |
| 从 `main` 手动触发 Publish GHCR images         | 是，复跑四项质量门禁 | 否              | 构建四个镜像的无副作用 dry run   |

## 首次推送的人工确认点

- [ ] 确认单体仓库采用保留历史还是重新初始化历史。
- [ ] 确认根目录最终目录名与 Docker 构建上下文。
- [ ] 确认 `.gitignore` 覆盖 Node/Maven 构建产物、`.env`、数据和 IDE 私有文件。
- [ ] 执行本地 `git status`、敏感信息扫描、构建测试和 Compose 校验。
- [ ] 人工审阅将要进入 GitHub 的文件与 diff。
- [ ] 收到明确授权后，配置远端 `origin` 为 `https://github.com/miozen/Miora.git`，创建首个分支并推送。
- [ ] 在 GitHub 侧启用 `main` 分支保护、Actions 最小权限与 GHCR 包可见性策略。

## 规范修订记录

| 日期       | 原因                                    | 结论                                                                                                                              |
| ---------- | --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| 2026-09-19 | 初始建立                                | 固定本文档；尚无真实发布故障。                                                                                                    |
| 2026-09-24 | 项目所有者明确要求固化协作方式          | 新增“协作回合契约”，明确需求、实现、测试、回合汇报与外部变更授权边界；适用于后续 Miora 改造。                                     |
| 2026-09-24 | 项目所有者确认 GitHub 使用 SSH 443 端口 | 新增 SSH-over-443 远端、主机指纹验证和密钥保护规范。                                                                              |
| 2026-09-24 | 开发机使用 NVM 提供 Node 20             | 新增前端 Node 前置条件及非交互 shell 的 NVM 初始化方式；CI 保持独立、可复现。                                                     |
| 2026-09-24 | B.3 发布前门禁                          | `main` 上的手动 Release tag 工作流复跑质量门禁；默认 dry run，只有项目所有者显式关闭后才创建 SemVer 注释标签。GHCR 发布留待 C.3。 |
| 2026-09-24 | B.4 测试/预发布环境样例与密钥边界       | 新增 test、staging 非敏感环境样例及 GitHub Environment Secrets 映射；CI 只使用 runner 内验证占位值。                              |
| 2026-09-24 | C.1 GHCR 命名                           | 固定 `ghcr.io/miozen/miora-{server,blog,admin,proxy}` 和 `vX.Y.Z` 发布标签；不创建包或推送镜像。                                  |
| 2026-09-24 | C.3 GHCR 标签发布                       | `vX.Y.Z` 标签仅在其提交可由 `main` 到达时发布四个 `linux/amd64` GHCR 镜像；发布前复跑质量门禁，另提供仅构建、不推送的 main dry run。 |
