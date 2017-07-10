cmake_minimum_required(VERSION 2.8)

project (ebdrm)

include_directories (
	../miracl
	../DRM_API
)

set (PUBLIC_HEADER_FILES
	../miracl/p1363.h
	../DRM_API/DRMLib.h
	../DRM_API/Utility.h
	../DRM_API/drmalgorithm.h
  com_jd_ebook_rights_rpc_drm_DrmLib.h
)

set (SOURCE_FILES
  ../miracl/mraes.c
  ../miracl/mrshs256.c
  ../miracl/rijndael-alg-fst.c
  ../miracl/p1363.c
  ../DRM_API/DRMLib.cpp
  ../DRM_API/Utility.cpp
  ../DRM_API/drmalgorithm.cpp
  com_jd_ebook_rights_rpc_drm_DrmLib.cpp
)

add_definitions (${DEFINITIONS} ${PLATFORM_DEFINITIONS})
ADD_DEFINITIONS(-D_SERVER)
add_library (${PROJECT_NAME} SHARED ${PUBLIC_HEADER_FILES} ${SOURCE_FILES})
