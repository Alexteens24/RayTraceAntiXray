import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'RayTraceAntiXray',
  description: 'Paper plugin for server-side async ray tracing with Anti-Xray engine-mode 1',
  base: '/RayTraceAntiXray/',
  cleanUrls: true,
  head: [
    ['link', { rel: 'icon', type: 'image/png', href: '/RayTraceAntiXray/logo.png' }],
    ['link', { rel: 'apple-touch-icon', href: '/RayTraceAntiXray/logo.png' }],
  ],
  themeConfig: {
    logo: '/logo.png',
    socialLinks: [
      { icon: 'github', link: 'https://github.com/Alexteens24/RayTraceAntiXray' },
    ],
    search: {
      provider: 'local',
    },
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Download', link: '/docs/download' },
      { text: 'Docs', link: '/docs/' },
    ],
    sidebar: [
      {
        text: 'General',
        items: [
          { text: 'Welcome', link: '/docs/' },
          { text: 'Features', link: '/docs/features' },
        ],
      },
      {
        text: 'Getting Started',
        items: [
          { text: 'Download', link: '/docs/download' },
          { text: 'Installation', link: '/docs/installation' },
        ],
      },
      {
        text: 'Reference',
        items: [
          { text: 'Commands', link: '/docs/commands' },
          { text: 'Permissions', link: '/docs/permissions' },
          { text: 'Configuration', link: '/docs/configuration' },
        ],
      },
      {
        text: 'Advanced',
        items: [
          { text: 'Troubleshooting', link: '/docs/troubleshooting' },
          { text: 'Development', link: '/docs/development' },
        ],
      },
    ],
    editLink: {
      pattern: 'https://github.com/Alexteens24/RayTraceAntiXray/edit/main/docs/:path',
      text: 'Edit this page on GitHub',
    },
  },
})
