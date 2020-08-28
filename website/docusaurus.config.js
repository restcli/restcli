module.exports = {
  title: 'restcli',
  tagline: 'A missing commandline application for execute IntelliJ HTTP Client files',
  url: 'https://quangson91.github.io/intellij_rest_cli/',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  favicon: 'img/favicon.ico',
  organizationName: 'quangson91', // Usually your GitHub org/user name.
  projectName: 'intellij_rest_cli', // Usually your repo name.
  themeConfig: {
    navbar: {
      title: 'restcli',
      logo: {
        alt: 'rest-cli-logo',
        src: 'img/logo.png',
      },
      items: [
        {
          to: 'docs/',
          activeBasePath: 'docs',
          label: 'Docs',
          position: 'left',
        },
        {
          href: 'https://github.com/quangson91/intellij_rest_cli',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      copyright: `Copyright © ${new Date().getFullYear()} QuangSon. Built with Docusaurus.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          // It is recommended to set document id as docs home page (`docs/` path).
          homePageId: 'about',
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl:
            'https://github.com/quangson91/intellij_rest_cli/edit/master/website/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
