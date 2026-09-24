import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "ThriveX 项目文档",
  description: "ThriveX 现代化博客管理系统官方文档",
  head: [
    ['link', { rel: 'icon', href: '/favicon.ico' }],
    ['link', { rel: 'stylesheet', href: '/styles/custom.css' }],
    // 引入百度统计配置
    ['script', {}, `
      var _hmt = _hmt || [];
      (function() {
        var hm = document.createElement("script");
        hm.src = "https://hm.baidu.com/hm.js?f3718a3e71b9e43dd7628b6d2e5f2b75";
        var s = document.getElementsByTagName("script")[0]; 
        s.parentNode.insertBefore(hm, s);
      })();
    `]
  ],
  themeConfig: {
    logo: '/logo.png',

    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: '首页', link: '/' },
      { text: '项目介绍', link: '/docs/项目介绍/README' },
      { text: '项目部署', link: '/docs/项目部署/1Panel' },
      { text: '项目使用', link: '/docs/项目使用/控制端/README' }
    ],

    sidebar: [
      {
        text: '项目介绍',
        items: [
          { text: '1. 介绍', link: '/docs/项目介绍/README' },
          { text: '2. 项目演示', link: '/docs/项目介绍/项目演示' },
          { text: '3. 项目结构', link: '/docs/项目介绍/项目结构' },
          { text: '4. 关于项目', link: '/docs/项目介绍/关于项目' },
          { text: '5. 特别鸣谢', link: '/docs/项目介绍/特别鸣谢' },
          { text: '6. 开源协议', link: '/docs/项目介绍/开源协议' },
          { text: '7. 开源地址', link: '/docs/项目介绍/开源地址' },
          { text: '8. 技术交流群', link: '/docs/项目介绍/技术交流群' },
          { text: '9. 常见疑惑', link: '/docs/项目介绍/常见疑惑' },
          { text: '10. 研发计划', link: '/docs/项目介绍/研发计划' },
        ]
      },
      {
        text: '项目部署',
        collapsed: true,
        items: [
          { text: 'Docker Compose', link: '/docs/项目部署/Compose' },
          { text: '1Panel', link: '/docs/项目部署/1Panel' },
          { text: '宝塔', link: '/docs/项目部署/宝塔' },
        ]
      },
      {
        text: '项目使用',
        collapsed: false,
        items: [
          {
            text: '前端',
            collapsed: true,
            items: [
              { text: '功能说明', link: '/docs/项目使用/前端/功能说明' },
              { text: '修改 favicon.ico', link: '/docs/项目使用/前端/修改 favicon.ico' },
            ]
          },
          {
            text: '控制端',
            collapsed: false,
            items: [
              { text: '使用总览', link: '/docs/项目使用/控制端/README' },
              { text: '内容创作', link: '/docs/项目使用/控制端/内容创作' },
              { text: '内容管理', link: '/docs/项目使用/控制端/内容管理' },
              { text: '系统配置', link: '/docs/项目使用/控制端/系统配置' },
              { text: '第三方配置', link: '/docs/项目使用/控制端/第三方配置' },
              { text: '文件系统', link: '/docs/项目使用/控制端/文件系统' },
            ]
          },
          {
            text: '后端',
            collapsed: true,
            items: [
              { text: '使用说明', link: '/docs/项目使用/后端/README' },
            ]
          },
        ]
      },
      {
        text: '第三方服务',
        collapsed: true,
        items: [
          { text: '人机验证', link: '/docs/项目部署/API/人机验证' },
          { text: '高德地图', link: '/docs/项目部署/API/高德地图' },
          { text: '百度统计', link: '/docs/项目部署/API/百度统计' },
          { text: '对象存储', link: '/docs/项目部署/API/对象存储' },
          { text: '邮件提醒', link: '/docs/项目部署/API/邮件提醒' },
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/LiuYuYang01/ThriveX-Blog' }
    ],

    // 开启深层目录
    outline: "deep",

    // 汉化内容
    outlineTitle: "目录",
    docFooter: {
      prev: "上一篇",
      next: "下一篇",
    },
    darkModeSwitchTitle: "切换暗色主题",
    lightModeSwitchTitle: "切换亮色主题",
    darkModeSwitchLabel: "切换主题",
    sidebarMenuLabel: "菜单",
    returnToTopLabel: "回到顶部",
    langMenuLabel: "切换语言",
    lastUpdatedText: "更新时间",
    externalLinkIcon: true,
    search: {
      provider: "local",
      options: {
        translations: {
          button: {
            buttonText: "搜索文档",
          },
          modal: {
            displayDetails: "显示详情",
            noResultsText: "未找到相关结果",
            resetButtonTitle: "清除",
            footer: {
              closeText: "关闭",
              selectText: "选择",
              navigateText: "切换",
            },
          },
        },
      },
    }
  },
  // 最后一次更新时间
  lastUpdated: true,
})
