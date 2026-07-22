#include <jni.h>
#include <string>

#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <fcntl.h>
#include <sys/ioctl.h>
//#include <termios.h>
#include <asm/termbits.h> // 必须使用 asm/termbits.h，不要使用 sys/termios.h，否则冲突
#include <stdlib.h>
#include <bits/termios_inlines.h>

#include "android/log.h"

static const char *TAG = "serial_port";
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static speed_t getBaudrate(jint baudrate) {
    switch (baudrate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_org_tcshare_utils_serial_SerialPort_close(JNIEnv *env, jobject instance) {
    jclass SerialPortClass = env->GetObjectClass(instance);
    jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

    jfieldID mFdID = env->GetFieldID(SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

    jobject mFd = env->GetObjectField(instance, mFdID);
    jint descriptor = env->GetIntField(mFd, descriptorID);

    LOGD("close(fd = %d)", descriptor);
    close(descriptor);
}


extern "C"
JNIEXPORT jobject JNICALL
Java_org_tcshare_utils_serial_SerialPort_open(JNIEnv *env, jclass type, jstring path,
                                              jint baudrate, jint flags) {
    int fd;
    speed_t speed;
    jobject mFileDescriptor;

    /* Opening device */
    {
        jboolean iscopy;
        const char *path_utf = env->GetStringUTFChars(path, &iscopy);
        LOGD("Opening serial port %s with flags 0x%x", path_utf, O_RDWR | flags);
        fd = open(path_utf, O_RDWR | flags);
        LOGD("open() fd = %d", fd);
        env->ReleaseStringUTFChars(path, path_utf);
        if (fd == -1) {
            /* Throw an exception */
            LOGE("Cannot open port");
            /* TODO: throw an exception */
            return NULL;
        }
    }


    /* Configure device */
    {
        LOGD("Configuring serial port...");
        speed = getBaudrate(baudrate);
        if (speed != -1) {
            LOGD("常规波特率! 串口配置!");
            // 常规波特率，使用 termios 结构体
            struct termios cfg;
            if (tcgetattr(fd, &cfg)) {
                LOGE("tcgetattr() failed");
                close(fd);
                return NULL;
            }

            cfmakeraw(&cfg);
            cfsetispeed(&cfg, speed);
            cfsetospeed(&cfg, speed);

            if (tcsetattr(fd, TCSANOW, &cfg)) {
                LOGE("tcsetattr() failed");
                close(fd);
                return NULL;
            }
            LOGD("标准波特率设置成功");

        } else {
            LOGD("不是常规波特率，尝试使用 termios2 设置自定义波特率: %d", baudrate);
            // 自定义波特率
            struct termios2 cfg2;

            if (ioctl(fd, TCGETS2, &cfg2) < 0) {
                LOGE("ioctl TCGETS2 failed. 内核可能拒绝或未提权");
                close(fd);
                return NULL;
            }

            // 配置为原始数据模式 (相当于 cfmakeraw)
            cfg2.c_iflag &= ~(IGNBRK | BRKINT | PARMRK | ISTRIP | INLCR | IGNCR | ICRNL | IXON);
            cfg2.c_oflag &= ~OPOST;
            cfg2.c_lflag &= ~(ECHO | ECHONL | ICANON | ISIG | IEXTEN);
            cfg2.c_cflag &= ~(CSIZE | PARENB);
            cfg2.c_cflag |= CS8;               // 8位数据位
            cfg2.c_cflag |= (CLOCAL | CREAD);  // 忽略控制线，使能接收器

            // 清除常规波特率标志，激活 BOTHER 标志以支持自定义数值
            cfg2.c_cflag &= ~CBAUD;
            cfg2.c_cflag |= BOTHER;

            cfg2.c_ispeed = baudrate;
            cfg2.c_ospeed = baudrate;

            if (ioctl(fd, TCSETS2, &cfg2) < 0) {
                LOGE("ioctl TCSETS2 failed! 无法写入自定义波特率 %d", baudrate);
                close(fd);
                return NULL;
            }

            struct termios2 verify_cfg;
            if (ioctl(fd, TCGETS2, &verify_cfg) == 0) {
                LOGD("当前实际输入波特率: %d, 输出波特率: %d", verify_cfg.c_ispeed,
                     verify_cfg.c_ospeed);
            }
        }
    }

    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
        jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
        mFileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
        env->SetIntField(mFileDescriptor, descriptorID, (jint) fd);
    }

    return mFileDescriptor;
}


