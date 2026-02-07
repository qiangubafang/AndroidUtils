//
// Created by oulei on 2020/8/26.
//

#ifndef ESTOOLCABIN_RS485SERIALMulti_H
#define ESTOOLCABIN_RS485SERIALMulti_H

static const char *TAG = "RS485SerialMulti";
#define LOGV(fmt, args...) __android_log_print(ANDROID_LOG_VERBOSE,  TAG, fmt, ##args)
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

#define CMD_FLAG 'i'
#define GPIO_ON      _IOR(CMD_FLAG,0x00000001,__u32)
#define GPIO_OFF     _IOR(CMD_FLAG,0x00000000,__u32)

#define DEBUG false

#include <sys/types.h>
#include <asm/termbits.h>


class RS485SerialMulti{
    int FD;
    int FD_IO;
    char enableSend[255]; // 使能发送字符串
    char enableRecv[255]; // 使能接收字符串
    const char *ECHO_1 = "echo 1 > ";
    const char *ECHO_0 = "echo 0 > ";

    int dataBits = 8;
    int parity = 'N';
    int stopBits = 1;

    bool kernelHasDriver;
    bool autoSend = false;

public:
    int openPort(JNIEnv *env, jobject type, jstring devPath, jstring enableIO, jint baudRate, jint flags, jboolean hasDriver);
    void closePort();
    jbyteArray send(JNIEnv *env, jobject type, jbyteArray sendArray, jint revBufSize, jint readWaitTime);

    int setSpeed(speed_t speed);
    int setParity(int databits, int stopbits, int parity);

    int rsWrite(char *buf, int dataLen);
    int rsRead(char *buf, int MaxLen, int waitTime);
    int sendWaitRecv(char *sendBuf, int sendLen, char *revBuf, int readMaxLen, int readWaitTime);

    jbyteArray drain(JNIEnv *env, jobject type,jint time);


};

#endif //ESTOOLCABIN_RS485SERIAL_H
