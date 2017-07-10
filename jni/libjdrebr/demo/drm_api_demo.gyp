{
  'targets': [
    {
      'target_name': 'drm_api_demo',
      'type': 'executable',
      'sources':[
        'demo.cpp',
      ],
      'dependencies': [
        '../DRM_API/drm_api_dll.gyp:drm_api_dll_gyp',
      ],
    },
  ],
}
