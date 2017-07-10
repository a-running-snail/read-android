{
  'variables' : {
    'source_files' : [
      "mr87f.c",
      "mr87v.c",
      "mraes.c",
      "mralloc.c",
      "mrarth0.c",
      "mrarth1.c",
      "mrarth2.c",
      "mrarth3.c",
      "mrbrick.c",
      "mrbuild.c",
      "mrcore.c",
      "mrcrt.c",
      "mrcurve.c",
      "mrdouble.c",
      "mrebrick.c",
      "mrecgf2m.c",
      "mrfast.c",
      "mrflash.c",
      "mrflsh1.c",
      "mrflsh2.c",
      "mrflsh3.c",
      "mrflsh4.c",
      "mrfrnd.c",
      "mrgcd.c",
      "mrio1.c",
      "mrio2.c",
      "mrjack.c",
      "mrlucas.c",
      "mrmonty.c",
      "mrmuldv.c",
      "mrpi.c",
      "mrpower.c",
      "mrprime.c",
      "mrrand.c",
      "mrround.c",
      "mrscrt.c",
      "mrshs.c",
      "mrshs256.c",
      "mrshs512.c",
      "mrsmall.c",
      "mrstrong.c",
      "mrxgcd.c",
      "p1363.c",
      "rijndael-alg-fst.c",
      "rijndael-api-fst.c",
    ],
    'include_files' : [
      "miracl.h",
      "mirdef.h",
      "p1363.h",
      "rijndael-alg-fst.h",
      "rijndael-api-fst.h",
    ],
  },
  'targets': [
    {
      'target_name': 'miracl_gyp',
      'type': 'static_library',
      'sources': [
        '<@(source_files)',
        '<@(include_files)',
      ],
      'include_dirs' : [
        './include', './source/p1363'
      ],
      'defines' : [
        'MR_P1363_DLL', 
      ],
      'direct_dependent_settings': {
        'include_dirs': [
          './'
        ],
      },
    }
  ]
}
