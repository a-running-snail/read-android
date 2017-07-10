{
  'variables' : {
    'javah' : '''<!(python -c "import os; print os.path.normpath(os.path.join(os.environ['JDK_HOME'], 'bin/javah.exe'))")'''
  },

  'targets': [
    {
      'target_name': 'java_drmlib',
      'type': 'shared_library',
      'sources':[
        "./Cipher.cpp", "./Cipher.h"
      ],
      'defines' : [
      ],
      'dependencies': [
        '../jni.gyp:jni',
        '../drm_api/drm_api_dll.gyp:drm_api_dll_gyp',
      ],
      'actions': [
        {
          'action_name': 'gen_jni_header',
          'inputs': [
            './Cipher.java',
          ],

          'outputs': [
            'Cipher.h'
          ],
          'action': ['<(javah)', 'Cipher'],
          'msvs_cygwin_shell': 0,
        },
      ],
      
    },
  ],
}
