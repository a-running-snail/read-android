TOP_LOCAL_PATH := $(call my-dir)

LOCAL_PATH = $(TOP_LOCAL_PATH)

include $(TOP_LOCAL_PATH)/libebr.mk

include $(CLEAR_VARS)

LOCAL_MODULE    := jdrdrm

LOCAL_SRC_FILES := __b.c  jddecompress.cpp jddrm.c
#jni_jeb_api.c 

LOCAL_STATIC_LIBRARIES := ebr

LOCAL_LDLIBS    := -lm -llog -lz

include $(BUILD_SHARED_LIBRARY)
