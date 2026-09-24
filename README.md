# Miora

Miora 是一个由 Spring Boot API、Next.js 访客站、React 管理后台和 VitePress 文档站组成的开源内容管理与博客系统。本仓库统一管理应用代码、部署配置与项目文档。

> 项目正在从 ThriveX 迁移为 Miora。品牌、技术命名与外部服务替换按 [实施清单](MIORA_IMPLEMENTATION_CHECKLIST.md) 分阶段完成。

## 目录

| 目录 | 作用 |
| --- | --- |
| `server/` | Java / Spring Boot 后端与 MySQL 初始化脚本 |
| `admin/` | React + Vite 管理后台 |
| `blog/` | Next.js 访客站 |
| `docs/` | VitePress 使用与部署文档 |
| `deploy/` | Nginx 等部署资源 |

## 本地部署

1. 复制并填写环境变量：`cp .env.example .env`；至少替换数据库密码和 `JWT_SECRET_KEY`。
2. 启动服务：`docker compose up -d --build`。
3. 查看服务状态：`docker compose ps`。

完整部署、备份和恢复说明见 [Compose 文档](docs/src/docs/项目部署/Compose.md)。生产环境必须使用两个域名分别承载 Blog 与 Admin，并在上线前配置 HTTPS 与访问边界。

## 开发与发布

分支流程固定为 `feature/* → dev → main`：功能分支通过 PR 合并到 `dev` 完成集成验证；仅验证通过的 `dev` 才能通过 PR 合并到受保护的 `main`，由 CI 构建和发布。

详细规则见 [协作与发布工作流](MIORA_GITHUB_COLLABORATION_AND_RELEASE_WORKFLOW.md)，贡献方式见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

根仓库采用 GNU Affero General Public License v3.0（AGPL-3.0）。迁移源代码中的版权、致谢及组件内许可证文件均予以保留；`blog/LICENSE` 保留其原有 GPL-3.0 文本。
