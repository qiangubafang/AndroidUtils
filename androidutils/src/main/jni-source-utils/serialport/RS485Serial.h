//
// Created by oulei on 2020/8/26.
//

#ifndef ESTOOLCABIN_RS485SERIAL_H
#define ESTOOLCABIN_RS485SERIAL_H

#include <sys/types.h>
#include <asm/termbits.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL
Java_org_tcshare_utils_RS485SerialPort_open(JNIEnv *env, jobject type, jstring devPath, jstring enableIO, jint baudRate, jint flags, jboolean hasDriver);

JNIEXPORT void JNICALL
Java_org_tcshare_utils_RS485SerialPort_close(JNIEnv *env, jobject type, jint fd);

JNIEXPORT jbyteArray JNICALL
Java_org_tcshare_utils_RS485SerialPort_send(JNIEnv *env, jobject type, jint fd, jbyteArray sendArray, jint revBufSize, jint readWaitTime);


#ifdef __cplusplus
}
#endif

#endif //ESTOOLCABIN_RS485SERIAL_H
