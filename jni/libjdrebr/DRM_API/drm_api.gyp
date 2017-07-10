{
  'targets': [
    {
      'target_name': 'drm_api_gyp',
      'type': 'static_library',
      'sources':[
        'DRMLib.cpp', 'HardwareInfo.cpp', 'LoadExe.cpp', 'Utility.cpp', 'drmalgorithm.cpp', 'sha256.c',
        #includes
        'DRMLib.h', 'HardwareInfo.h', 'LoadExe.h', 'Utility.h', 'drmalgorithm.h', 'sha256.h'
      ],
      'dependencies': [
        '../miracl/miracl.gyp:miracl_gyp'
      ],
      'direct_dependent_settings': {
        'include_dirs': [
          './'
        ],
      },
    },
  ],
}
