# Miora 实施清单：仓库、发布、品牌迁移与后台入口

## 已确认的决策

- 项目名称：`Miora`。
- GitHub 远端：`https://github.com/miozen/Miora.git`（已于 2026-09-19 以只读 `git ls-remote` 验证可连接；当前无 `HEAD` 或 `main` 引用，符合空仓库状态）。
- 仓库形态：迁移为一个 GitHub 单体仓库；`server`、`admin`、`blog`、`docs`、部署文件保持在同一次提交中。
- 文档：保留在单体仓库，但不作为生产 Compose 服务启动；后续可独立发布为文档站。
- 分支：`feature/*` 合并到 `dev` 做集成验证，验证通过后由 `dev` 合并到受保护的 `main`；生产只发布 `main` 的版本标签。
- 生产发布：GitHub Actions 构建镜像并推送到 GHCR；VPS/内网服务器只拉取指定镜像，不在生产机从源码构建。
- 站点入口：Blog 与 Admin 分别使用两个域名，例如 `blog.a.com` 与 `admin.a.com`；不采用 `/houtai/` 子路径部署。
- 存储：延续一期结论，默认 `local`，可选 S3 兼容存储。

## A. 单体仓库迁移

- [x] A.1 确定 GitHub 用户或组织、目标仓库名 `Miora`、默认分支 `main` 和开发分支 `dev`。
- [x] A.2 确定历史保留策略：使用 `git subtree`/迁移工具保留四个现有仓库历史，或以新的单体仓库重新初始化；在执行前记录原仓库地址和提交基线。
- [x] A.3 将 Server、Admin、Blog、Docs 纳入统一根目录；根目录 Compose 的构建上下文改为新目录结构。
- [x] A.4 统一根目录 `.gitignore`、编辑器配置、许可证、贡献说明和根 README。
- [x] A.5 配置 GitHub 分支保护：`main` 禁止直接推送，要求 PR 与 CI 通过。
- [x] A.6 验收：全新目录执行 clone 后，`docker compose config` 能解析所有构建上下文。

## B. 分支与质量门禁

- [x] B.1 建立分支约定：`feature/*` -> `dev` -> `main`，并约定紧急修复分支的回合路径。
- [x] B.2 为 PR 到 `dev` 配置 CI：后端 Maven 测试、Admin lint/build、Blog lint/build、Compose 配置校验。
- [x] B.3 为 `main` 配置发布前 CI：复跑质量门禁，并生成版本号与 Git tag。
- [x] B.4 建立测试/预发布环境变量样例；所有密钥使用 GitHub Secrets，不提交 `.env`。
- [x] B.5 验收：故意提交一项 lint 或测试失败的变更，确认无法合并到受保护分支。

## C. GHCR 生产镜像发布

- [x] C.1 确定镜像命名：`ghcr.io/<owner>/miora-server`、`miora-blog`、`miora-admin`、`miora-proxy`（或以单一发布仓库命名空间统一管理）。
- [x] C.2 调整 Dockerfile 与 Compose：开发模式可保留 `build`，生产 Compose 改为引用带版本标签的 `image`，不依赖本机源码。
- [x] C.3 编写 GitHub Actions 工作流：在 `main` 发布版本 tag 时构建、测试、推送四个 `linux/amd64` 镜像；另提供仅构建的 main dry run。
- [ ] C.4 选择镜像可见性：公开 GHCR 镜像，或在 VPS 安全保存只读 GHCR Pull Token 后登录拉取。
- [ ] C.5 新增生产环境文件样例：只包含镜像 tag、域名、密码与存储变量，不含构建路径。
- [ ] C.6 验收：在无源码的干净 Alpine 主机执行 `docker compose pull` 和 `docker compose up -d`，五个服务健康。
- [ ] C.7 回滚演练：将镜像 tag 回退到上一个版本，确认数据卷不被删除且站点恢复。

## D. 双域名后台入口

- [ ] D.1 确定生产与内网域名，例如 `blog.a.com` / `admin.a.com`、`blog.intra.example` / `admin.intra.example`。
- [ ] D.2 Compose/代理继续以 `BLOG_HOST` 和 `ADMIN_HOST` 分流；对外只暴露代理的 80/443，不公开 MySQL、Server、Blog、Admin 端口。
- [ ] D.3 在 Blog 顶部菜单栏最右侧加入“管理后台”入口；入口地址从构建环境变量读取，未配置时隐藏。
- [ ] D.4 配置 HTTPS；公网场景为两个域名分别签发证书，内网场景由内部 CA 或受信任网关终止 TLS。
- [ ] D.5 对 Admin 施加访问边界：优先 VPN/内网/IP 白名单，必要时加额外反向代理认证。
- [ ] D.6 验收：Blog、Admin、`/api/`、本地上传静态文件在两个域名下均可访问；管理入口跳转正确。

## E. Miora 品牌与技术命名迁移

### E.1 品牌基线

- [ ] 确定 Miora 的 GitHub 地址、域名、邮箱、Logo、站点中文名、镜像命名空间与 Java 包名。
- [ ] 确认原项目许可证与必须保留的版权、致谢和第三方声明。
- [ ] 建立“原值 -> 新值/处理决定”的映射表；未知域名或邮箱先使用明确占位符，不连向原作者服务。

### E.2 用户可见内容

- [ ] 替换 Blog/Admin 的页面标题、Logo、页脚、文案、浏览器元信息。
- [ ] 更新根 README、各组件 README、Docs、部署示例、GitHub/Release 链接与联系信息。
- [ ] 替换或移除原作者的演示地址、邮箱、社群二维码/联系方式。
- [ ] 验收：人工检查首页、后台登录页、文档首页、README，确认显示为 Miora。

### E.3 Java 与构建技术命名

- [ ] 将 `liuyuyang.net` Java 包路径、目录、`package`、`import` 和反射引用统一迁移到确定的新包名。
- [ ] 同步调整 Spring Boot 启动包扫描、MyBatis 扫描、测试包路径与 Maven 坐标。
- [ ] 更新 Docker 镜像、容器、Compose 项目、数据库与卷的 Miora 命名；已有生产数据的改名必须有迁移与回滚方案。
- [ ] 验收：后端完整测试、Admin/Blog 构建、Compose 首启均通过。

### E.4 外部依赖与遗留排查

- [ ] 处理 Blog 中指向 `frame-api.liuyuyang.net` 的相册接口：迁移到 Miora 服务、替换第三方，或暂时下线功能。
- [ ] 检查统计、邮件、地图、AI、OAuth、CDN、对象存储和 Release 检查等外部地址。
- [ ] 执行全仓搜索，分类处理 `liuyuyang`、`ThriveX`、原 GitHub 用户名、原域名和原邮箱。
- [ ] 验收：除许可证/历史致谢中明确允许保留的记录外，不存在会将用户请求发送到原作者服务的遗留配置。

## 一期完成但暂缓实施的事项

以下事项不阻塞已完成的一期存储与 Compose 基础交付，单列为后续计划：

- [ ] 真实 OCI 与腾讯 COS 凭据的联网验收：连接测试、上传、读取、列表、删除和匿名公开读取。
- [ ] 生产 HTTPS：当前 Compose 只提供 HTTP，需选择 Caddy、Nginx + Certbot、Traefik 或上游 CDN/网关方案。
- [ ] 数据库版本迁移、自动升级与可重复的 schema migration。
- [ ] 后台数据库备份、导入、恢复与初始化向导重构。
- [ ] 安全改造：后台访问控制、密钥轮换、审计、限流策略复核。
- [ ] 邮件、地图、AI 等非一期核心功能的配置与验收。
- [ ] 修复 Blog 既有的 1 条无效 `eslint-disable` warning。

## 建议执行顺序

1. A 单体仓库迁移与 B 基础 CI；
2. E.1、E.2（先完成用户可见的 Miora 品牌）；
3. D 双域名入口与 HTTPS/后台访问边界；
4. E.3、E.4（Java 包名及外部依赖迁移）；
5. C GHCR 镜像发布与无源码 VPS 部署；
6. 完成“一期暂缓事项”中与正式上线相关的 HTTPS、备份和真实 S3 验收。

> 每个复选项完成后应记录对应提交、CI 运行链接、验收命令和回滚点；不要将密钥、生产 `.env` 或数据库备份提交到 Git。协作、发布、GHCR 与分支规则以 [MIORA_GITHUB_COLLABORATION_AND_RELEASE_WORKFLOW.md](MIORA_GITHUB_COLLABORATION_AND_RELEASE_WORKFLOW.md) 为唯一固定规范。

## 实施记录

- 2026-09-24，A.1 完成：确认 GitHub 所有者为 `miozen`、目标空仓库为 `Miora`（`https://github.com/miozen/Miora.git`）、默认分支为 `main`、集成分支为 `dev`。同时经项目所有者授权，将“需求—实现—测试—回合总结—按需外部变更”的协作契约固化到发布工作流。验收：核对四个原仓库远端及当前分支（Server 为 `master`，其余为 `main`），确认尚未连接或推送 Miora 远端；无回滚需要。本轮未创建提交、未运行 CI、未推送。
- 2026-09-24，A.2 完成：项目所有者决定以新的 Miora 单体仓库重新初始化历史，不使用 `git subtree` 导入旧提交。原仓库基线已记录：Server `https://github.com/LiuYuYang01/ThriveX-Server.git` @ `60b24e559a9a6ee708b62ecb80d533274a5e0fea`；Admin `https://github.com/LiuYuYang01/ThriveX-Admin.git` @ `b3bac9d730fb840e7f33dc8535ae9820aad2b788`；Blog `https://github.com/LiuYuYang01/ThriveX-Blog.git` @ `d98dacfddabbbaf30a4b6ec37b5183161024dad0`；Docs `https://github.com/LiuYuYang01/ThriveX-Docs.git` @ `e59ada939e2171311cbf6057836d5119db1fc1f1`。验收：使用各仓库 `remote get-url origin`、`rev-parse HEAD` 与最后提交记录核对；无回滚需要。本轮未创建提交、未运行 CI、未推送。
- 2026-09-24，A.3 完成：已将 `ThriveX-Server`、`ThriveX-Admin`、`ThriveX-Blog`、`ThriveX-Docs` 分别迁移为根目录 `server/`、`admin/`、`blog/`、`docs/`，并按项目所有者授权移除四个嵌套 `.git` 目录；现有未提交的一期改动随目录保留。已将 Compose 构建上下文和 SQL 初始化挂载改为新路径，并同步 Compose 部署文档。由于原工作区根 `.git` 为只读 tmpfs 挂载，已按项目所有者授权创建可写工作区 `/home/mio/projects/miora`，在其中以 `main` 初始化新的单体 Git 仓库；确认不存在嵌套 Git 仓库。验收：`docker compose --env-file .env.example config --quiet` 通过，`git status --branch --short` 显示无提交的 `main`；未创建提交、未推送。
- 2026-09-24，A.4 完成：新增根 `.gitignore`、`.editorconfig`、`LICENSE`、`CONTRIBUTING.md` 与 `README.md`。根许可证采用 AGPL-3.0，并保留 Admin 的 AGPL 与 Blog 的 GPL-3.0 组件许可证；忽略规则覆盖 `.env`、密钥、Node/Java 构建产物、日志与本地数据。验收：检查 `git status --ignored`、根忽略规则及待提交文件；未创建提交、未推送。
- 2026-09-24，A.5 完成：已创建并推送首个单体仓库提交 `04e89d2`（`chore: initialize Miora monorepo`）到 `main`；远端使用固定 SSH-over-443 地址 `ssh://git@ssh.github.com:443/miozen/Miora.git`，并已验证远端 SHA 一致。GitHub `main` 与 `dev` 已启用分支保护：要求 PR、管理员受规则约束、禁止强推和删除、要求解决对话；目前为单维护者设置 0 个必需批准评审。两条分支均启用严格必需状态检查：`Server`、`Admin`、`Blog`、`Compose`。已检查提交候选、忽略规则和已提交内容的私钥/AWS Access Key 模式；推送前移除了遗漏的 `admin/.vite/` 缓存。
- 2026-09-24，B.1 完成：从 `main` 创建并推送集成分支 `dev`，随后创建 `feature/branch-workflow` 用于承载本轮规范与 A.5 记录的 PR。工作流已明确常规 `feature/* -> dev -> main` 路径，以及 `hotfix/*` 从 `main` 到 `main`、再回补 `dev` 的路径。验收：确认 `dev` 基于 `main` 的提交建立；本轮 PR 指向 `dev`，待 B.2 的 CI 建立后合并。
- 2026-09-24，B.2 完成：新增根目录 GitHub Actions 工作流，覆盖后端 `mvn -pl blog -am test`、Admin `npm run build`（其构建脚本已包含 lint）、Blog `npm run lint` 与 `npm run build`、以及 `docker compose --env-file .env.example config --quiet`。GitHub Actions 首次运行已全部通过：<https://github.com/miozen/Miora/actions/runs/35965134679>；本地 Maven（8 项测试）与 Compose 配置也已通过。工作流使用 Actions v5，避免首轮运行报告的旧版运行时弃用警告。
- 2026-09-24，A.6 完成：在一次性干净目录 `/tmp/miora-a6-2Re8l1` 从 GitHub SSH-over-443 克隆 `dev`，检出提交 `3d301a9918d5297c152f8bc891879c23830e6e9a`。验收：`docker compose --env-file .env.example config --quiet` 通过；`server/`、`blog/`、`admin/` 均存在且各自包含 `Dockerfile`，并确认 `server/ThriveX.sql` 和 `deploy/nginx/default.conf.template` 两个 Compose 挂载源存在。开发机 Node 环境已核验为 NVM 管理的 Node `v20.20.2`、npm `10.8.2`；无容器构建、提交或推送。
- 2026-09-24，B.3 完成：新增 `Release tag` 手动工作流，只允许从 `main` 发起，要求显式提供严格的 `vX.Y.Z` SemVer 版本，并在创建标签前复跑 `Server`、`Admin`、`Blog`、`Compose`；默认 `dry_run=true`，仅显式关闭后才使用 GitHub Actions 身份创建注释标签。验收：从 `main` 提交 `ebbda6665d8fe017ff90fb2d6aa6c6322d8a2005` 以 `v0.1.0` 执行 dry run，全部门禁通过：<https://github.com/miozen/Miora/actions/runs/35967052876>；确认不存在 `v0.1.0` 标签。GHCR 镜像构建与发布仍由 C.3 实施。
- 2026-09-24，B.4 完成：新增 `environments/test.env.example`、`environments/staging.env.example`，其中只保留非敏感 Compose 配置；真实运行时文件 `environments/*.env` 已由根 `.gitignore` 保护。已创建 GitHub Environments `test`、`staging`，并在 `MIORA_ENVIRONMENT_AND_SECRETS.md` 固定数据库、JWT 和可选 S3 密钥的同名 Environment Secrets 映射及注入边界。Compose CI 同时以 runner 内无价值占位值验证三份样例；在项目所有者明确授权后，已为 `test`、`staging` 分别配置独立随机的数据库/JWT Secrets，未读取、输出或提交其值，且未配置 S3 Secrets。
- 2026-09-24，B.5 完成：在一次性分支 `feature/b5-required-check-rejection` 添加明确失败的 JUnit 测试并创建 PR #10 到 `dev`。GitHub CI 中 `Server` 失败、`Admin`、`Blog`、`Compose` 通过，PR 合并状态为 `BLOCKED`：<https://github.com/miozen/Miora/actions/runs/35969923505>。验证后关闭 PR #10，`mergedAt` 为 null；失败夹具未进入 `dev` 或 `main`，远端分支保留为审计证据。
- 2026-09-24，C.1 完成：固定 GHCR 命名空间为 `ghcr.io/miozen`，分别使用 `miora-server`、`miora-blog`、`miora-admin`、`miora-proxy`；`mysql` 保持上游镜像。Git 发布标签与生产镜像标签均使用 `vX.Y.Z`，禁止 `latest`。完整约定见 `MIORA_GHCR_IMAGE_CONVENTION.md`；本轮未创建 GHCR 包、未构建或推送镜像。
- 2026-09-24，C.2 完成：Server、Blog、Admin Dockerfile 新增 OCI 来源/修订标签，新增将 Nginx 模板烘焙进镜像的 Proxy Dockerfile。新增 `compose.production.yaml`，四个应用均引用 `ghcr.io/miozen/miora-*:${IMAGE_TAG}`，无 `build`、源码或 Nginx 本地挂载；MySQL 保持上游镜像并要求通过审查的迁移流程预初始化。CI 使用无价值占位值解析生产 Compose；本轮未构建或推送 GHCR 镜像。
- 2026-09-24，C.3 完成：新增 `Publish GHCR images` 工作流。推送 `vX.Y.Z` 标签时，工作流验证标签提交可由 `main` 到达、复跑 `Server`、`Admin`、`Blog`、`Compose` 四项质量门禁，然后分别构建并以 `GITHUB_TOKEN` 推送 Server、Blog、Admin、Proxy 四个 `linux/amd64` GHCR 镜像；每个镜像使用版本标签和同一完整提交 SHA 的审计标签，未使用 `latest`。从 `main` 手动触发时工作流只构建不推送，供无副作用 dry run 使用。本轮不创建版本标签、不创建 GHCR 包；首次 dry run 在工作流合并并进入 `main` 后执行。
