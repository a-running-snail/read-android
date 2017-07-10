{
  'variables': {
    'header_files': [
      './DRMLib.h',
      './drmalgorithm.h',
    ],
    'def_file' : 'drmlib.def'
  },
  
  'targets': [
    {
      'target_name': 'drm_api_dll_gyp',
      'type': 'shared_library',
      'sources':[
        'drm_api_dll.cpp', '<(def_file)', '<@(header_files)'
      ],
      'defines' : [
        '_EBOOK_DLL', 'WIN_DLL'
      ],
      'dependencies': [
        './drm_api.gyp:drm_api_gyp'
      ],
      'direct_dependent_settings': {
        'include_dirs': [
          './'
        ],
      },
      'actions': [
        {
          'action_name': 'gendef',
          'inputs': [
            './gendef.py',
            '<@(header_files)',
          ],

          'outputs': [
            '<(def_file)'
          ],
          'action': ['python', './gendef.py', '<(def_file)'],
          'msvs_cygwin_shell': 0,
        },
      ],
    },
  ],
}
