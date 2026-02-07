//
// Created by oulei on 2020/8/26.
//

#include <jni.h>
#include "RS485Serial.h"
#include <pthread.h>
#include <sys/prctl.h>
#include <sys/ioctl.h>
#include <android/log.h>
#include <sys/poll.h>
#include <termios.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <cstring>
#include <cstdlib>
#include "RS485SerialMulti.h"
#include <map>

std::map<int,RS485SerialMulti*> instanceMaps;

extern "C"
JNIEXPORT jint JNICALL
Java_org_tcshare_utils_rs485serial_RS485SerialPort_nativeOpen(JNIEnv *env, jobject type, jstring devPath,
                                            jstring enableIO, jint baudRate, jint flags, jboolean hasDriver) {

    auto* instance = new RS485SerialMulti();
    int id = instance->openPort(env, type, devPath, enableIO, baudRate, flags, hasDriver);
    if(id == -1){
        return id;
    }
    auto it = instanceMaps.find(id); // 旧实例是否存在
    if (it != instanceMaps.end()){
        instanceMaps[id]->closePort();
        free(instanceMaps[id]);
        instanceMaps.erase (it);
    }

    instanceMaps[id] = instance;
    return id;
}

extern "C"
JNIEXPORT void JNICALL
Java_org_tcshare_utils_rs485serial_RS485SerialPort_nativeClose(JNIEnv *env, jobject type, jint id) {
    auto it = instanceMaps.find(id); // 查找实例
    if (it != instanceMaps.end()){
        instanceMaps[id]->closePort();
        free(instanceMaps[id]);
        instanceMaps.erase(it);
    }
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_org_tcshare_utils_rs485serial_RS485SerialPort_nativeSend(JNIEnv *env, jobject type, jint id, jbyteArray sendArray, jint revBufSize, jint readWaitTime) {
    auto it = instanceMaps.find(id); // 查找实例
    if (it != instanceMaps.end()){
        return instanceMaps[id]->send(env,type, sendArray, revBufSize, readWaitTime);
    }
    return nullptr;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_org_tcshare_utils_rs485serial_RS485SerialPort_nativeDrain(JNIEnv *env, jobject type, jint id, jint time) {
    auto it = instanceMaps.find(id); // 查找实例
    if (it != instanceMaps.end()){
        return instanceMaps[id]->drain(env, type, time);
    }
    return nullptr;
}

