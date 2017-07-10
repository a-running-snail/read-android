# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
#LOCAL_CPP_EXTENSION := cpp
LOCAL_MODULE    := jdrebr
LOCAL_SRC_FILES := \
  JNI_Client/com_jingdong_app_reader_data_Ebr.cpp \
  DRM_API/DRMLib.cpp \
  DRM_API/drmalgorithm.cpp \
  DRM_API/Utility.cpp  \
  DRM_API/md5.c  \
  miracl/mraes.c \
  miracl/mrshs256.c \
  miracl/p1363.c \
  miracl/rijndael-alg-fst.c

#LOCAL_C_INCUDES := $(LOCAL_PATH) 
LOCAL_CFLAGS := -I./JNI_Client -I./DRM_API -I./miracl
LOCAL_CFLAGS += -D_CLIENT
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
