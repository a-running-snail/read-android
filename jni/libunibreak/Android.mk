LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := unibreak-v1
LOCAL_SRC_FILES := \
	./LineBreaker.cpp \
	./libunibreak-3.0/src/unibreakbase.c \
	./libunibreak-3.0/src/unibreakdef.c \
	./libunibreak-3.0/src/linebreak.c \
	./libunibreak-3.0/src/linebreakdata.c \
	./libunibreak-3.0/src/linebreakdef.c \
	./libunibreak-3.0/src/wordbreak.c

LOCAL_C_INCLUDES := ./libunibreak-3.0/src

include $(BUILD_SHARED_LIBRARY)
