LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := libNativeImageProcessor
LOCAL_SRC_FILES := NativeImageProcessor.cpp
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
