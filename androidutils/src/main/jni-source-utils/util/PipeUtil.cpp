/**
* Created by 千古八方 on 2026/3/17.
*
* Copyright (c) 2026 千古八方 All rights reserved.
*/
#include <sys/stat.h>
#include <errno.h>
#include <string.h>
#include <jni.h>
#include <android/log.h>

extern "C" JNIEXPORT jint JNICALL
Java_org_tcshare_utils_PipeUtil_createNamedPipe(JNIEnv *env, jobject instance, jstring path) {
    const char *pipePath = env->GetStringUTFChars(path, JNI_FALSE);
    // Permissions: read/write for owner/group/others (0666)
    int result = mkfifo(pipePath, 0666);
    if (result == -1) {
        __android_log_print(ANDROID_LOG_ERROR, "PipeUtil", "mkfifo failed: %s", strerror(errno));
    }
    env->ReleaseStringUTFChars(path, pipePath);
    return result;
}