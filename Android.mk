# Copyright (C) 2016 CypherOS
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

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := aoscp-framework
LOCAL_SDK_VERSION := 24
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-preference \
    android.support.v14.preference \
    android.support.v17.preference \
    android-support-design

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v4
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat
LOCAL_AAPT_FLAGS += --extra-packages android.support.design
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.preference
LOCAL_AAPT_FLAGS += --extra-packages android.support.v14.preference
LOCAL_AAPT_FLAGS += --extra-packages android.support.v17.preference

include $(BUILD_STATIC_JAVA_LIBRARY)