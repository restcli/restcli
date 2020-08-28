module.exports = {
  someSidebar: [
    {
      type: 'doc',
      id: 'about',
    },
    {
      type: 'doc',
      id: 'features',
    },
    {
      type: 'category',
      label: 'CI Integration',
      items: [
        'gitlab-pipeline-integration',
        'jenkins-ci-integration',
        'travis-integration',
      ],
    },
  ],
};
