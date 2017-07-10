{
  'variables': {
    'OutDir': '<(DEPTH)/out',
  },
  'conditions' : [
    ['OS == "win"',
     {
        'target_defaults': {
          'defines': [
            'WIN32','_WINDOWS',
          ],
          'msvs_cygwin_shell': 0,
          'msvs_settings': {
            'VCCLCompilerTool': {
              'WarningLevel': '1',
              'WarnAsError': 'false',
              'DebugInformationFormat': '3',
              'AdditionalOptions': '/MP',
              'ExceptionHandling':'1',
            },
          },
          'configurations': {
            'Debug': {
              'defines': [
                '_DEBUG', '_MBCS'
              ],
              'msvs_settings': {
                'VCCLCompilerTool': {
                  'Optimization': '0',    # 0 = /Od
#                  'PreprocessorDefinitions': ['_DEBUG'],
                  'RuntimeLibrary': '1',  # 1 = /MTd (debug DLL)
                },
                'VCLinkerTool': {
                  'GenerateDebugInformation': 'true',
                },
              },
            },
            'Release': {
            'defines': [
                'NDEBUG','_MBCS',
              ],
              'msvs_settings': {
                'VCCLCompilerTool': {
                  'Optimization': '2',    # 2 = /Os
#                  'PreprocessorDefinitions': ['NDEBUG'],
                  'RuntimeLibrary': '0',  # 0 = /MT (nondebug DLL)
                },
                'VCLinkerTool': {
                  'GenerateDebugInformation': 'false',
                },
              },
            },
             'Unicode Debug': {
              'defines': [
                '_DEBUG','_UNICODE','UNICODE',
              ],
              'msvs_settings': {
                'VCCLCompilerTool': {
                  'Optimization': '0',    # 0 = /Od
#                  'PreprocessorDefinitions': ['_DEBUG'],
                  'RuntimeLibrary': '1',  # 1 = /MTd (debug DLL)
                },
                'VCLinkerTool': {
                  'GenerateDebugInformation': 'true',
                },
              },
            },
            'Unicode Release': {
            'defines': [
                'NDEBUG','_UNICODE','UNICODE',
              ],
              'msvs_settings': {
                'VCCLCompilerTool': {
                  'Optimization': '2',    # 2 = /Os
#                  'PreprocessorDefinitions': ['NDEBUG'],
                  'RuntimeLibrary': '0',  # 0 = /MT (nondebug DLL)
                },
                'VCLinkerTool': {
                  'GenerateDebugInformation': 'false',
                },
              },
            },
          },
        },
      },
   ],
    ['OS == "linux"', 
     {
        'target_defaults': {
          'configurations': {
            'Debug': {
              'cflags': ['-g'],
              'defines': [
                '_DEBUG',
              ],
            },
            'Release': {
              'cflags': ['-O2']
            },
          },
          'cflags': [ '-Wall', '-Wextra', '-Wno-unused' ]
        },
      },
   ],
    ['OS == "mac"', 
     {
        'target_defaults': {
          'configurations': {
            'Debug': {
              'cflags': ['-g']
            },
            'Release': {
              'cflags': ['-O2']
            },
          },
        },
        'xcode_settings': {
          'SYMROOT': '<(DEPTH)/xcodebuild',
        },
      },
   ],
  ],
}
# Local Variables:
# tab-width:2
# indent-tabs-mode:nil
# End:
# vim: set expandtab tabstop=2 shiftwidth=2:
