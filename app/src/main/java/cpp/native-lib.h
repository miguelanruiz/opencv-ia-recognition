//
// Created by Dell on 1/23/2024.
//
#ifndef OPENCV_IA_RECOGNITION_NATIVE_LIB_H
#define OPENCV_IA_RECOGNITION_NATIVE_LIB_H

#include <android/log.h>

#define LOG_TAG "OpenCV_IA_Recognition"

#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,    LOG_TAG, __VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,       LOG_TAG, __VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,      LOG_TAG, __VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,       LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,      LOG_TAG, __VA_ARGS__)

#endif //OPENCV_IA_RECOGNITION_NATIVE_LIB_H
