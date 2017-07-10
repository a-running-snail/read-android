LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LIBEBR_ROOT := ../../../libjdrebr

LOCAL_MODULE := ebr

LOCAL_SRC_FILES := \
  $(LIBEBR_ROOT)/DRM_API/DRMLib.cpp \
  $(LIBEBR_ROOT)/DRM_API/drmalgorithm.cpp \
  $(LIBEBR_ROOT)/DRM_API/Utility.cpp  \
  $(LIBEBR_ROOT)/DRM_API/md5.c  \
  $(LIBEBR_ROOT)/miracl/mraes.c \
  $(LIBEBR_ROOT)/miracl/mrshs256.c \
  $(LIBEBR_ROOT)/miracl/p1363.c \
  $(LIBEBR_ROOT)/miracl/rijndael-alg-fst.c

LOCAL_C_INCLUDES := \
  ../../libjdrebr/JNI_Client \
  ../../libjdrebr/DRM_API \
  ../../libjdrebr/miracl

LOCAL_CFLAGS += -D_CLIENT
LOCAL_LDLIBS := -llog

include $(BUILD_STATIC_LIBRARY)
