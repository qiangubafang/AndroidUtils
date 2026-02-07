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
jstring charToJString(JNIEnv *env, char *pat) {
    jclass strClass = env->FindClass("java/lang/String");
    jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = env->NewByteArray(strlen(pat));
    env->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte *) pat);
    jstring encoding = env->NewStringUTF("utf-8");
    return (jstring) env->NewObject(strClass, ctorID, bytes, encoding);
}
jbyteArray charToJByteArray(JNIEnv *env, char *buf, int len) {
    jbyteArray array = env->NewByteArray(len);
    env->SetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
    return array;
}
char *jByteArrayToChar(JNIEnv *env, jbyteArray buf) {
    char *chars = NULL;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(buf, 0);
    int chars_len = env->GetArrayLength(buf);
    chars = new char[chars_len + 1];
    memset(chars, 0, chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;
    env->ReleaseByteArrayElements(buf, bytes, 0);
    return chars;
}



int RS485SerialMulti::openPort(JNIEnv *env, jobject type, jstring devPath, jstring enableIO, jint baudRate, jint flags, jboolean hasDriver) {
    if (enableIO != nullptr) {
        const char *enable_io = env->GetStringUTFChars(enableIO, JNI_FALSE);
        memset(enableSend, 0, sizeof(enableSend));
        memset(enableRecv, 0, sizeof(enableRecv));
        sprintf(enableSend, "%s%s", ECHO_1, enable_io);
        sprintf(enableRecv, "%s%s", ECHO_0, enable_io);
        env->ReleaseStringUTFChars(enableIO, enable_io);
        if(DEBUG) LOGD("enSend: %s", enableSend);
        if(DEBUG) LOGD("enRecv: %s", enableRecv);

        kernelHasDriver = (hasDriver == JNI_TRUE);
        if(kernelHasDriver){
            FD_IO=open(enable_io,O_RDWR);
            if(FD_IO == -1)
            {
                LOGE("can't open gpio ");
                return -1;
            }
            if(DEBUG) LOGD("open( %s ) fd = %d", enable_io, FD_IO);
        }
    } else{
        autoSend = true;
    }

    speed_t speed;
    /* 检查波特率 */
    {
        speed = getBaudrate(baudRate);
        if (speed == -1) {
            /* TODO: throw an exception */
            LOGE("Invalid baudrate");
            return -1;
        }
    }

    /* 打开设备 */
    {
        const char *path_utf = env->GetStringUTFChars(devPath, JNI_FALSE);
        if(DEBUG) LOGD("Opening serial port %s with band %d and flags 0x%x", path_utf, baudRate, O_SYNC |O_RDWR | O_NOCTTY | flags);
        FD = open(path_utf,  O_SYNC |O_RDWR | O_NOCTTY | flags);
        if(DEBUG) LOGD("open() fd = %d kernelHasDriver %d", FD, kernelHasDriver);
        env->ReleaseStringUTFChars(devPath, path_utf);
        if (FD == -1) {
            LOGE("Cannot open port");
            return -1;
        }
    }

    /* 配置设备 */
    {
        if (setSpeed(speed) != 0) {
            LOGE("setSpeed failed!");
            return -1;
        }
        if (setParity(8, 1, 'N') != 0) {
            LOGE("setParity failed!");
            return -1;
        }

    }
    if(DEBUG) LOGD("Open serial success!");
    return FD;
}

void RS485SerialMulti::closePort() {
    if(DEBUG) LOGD("close(FD = %d)", FD);
    close(FD);
    if(kernelHasDriver){
        close(FD_IO);
    }
}

jbyteArray RS485SerialMulti::send(JNIEnv *env, jobject type, jbyteArray sendArray, jint revBufSize, jint readWaitTime) {
    int chars_len = env->GetArrayLength(sendArray);

    char *sendData = jByteArrayToChar(env, sendArray);
    char recvbuf[revBufSize];
    memset(recvbuf, 0, sizeof(recvbuf));


    int recvLen = sendWaitRecv(sendData, chars_len, recvbuf, revBufSize, readWaitTime);
    return recvLen == -1 ? nullptr : charToJByteArray(env, recvbuf, recvLen);
}

/*****************************************************************************
* Function:      uart_set_speed
* Description:   设置波特率
* Input:         fd:    文件标识付
*                speed: 波特率值
* Output:        None
* Return:        None
* Others:        None
*****************************************************************************/
int RS485SerialMulti::setSpeed(speed_t speed)
{
    int status;
    struct termios Opt;

    tcgetattr(FD, &Opt);
    tcflush(FD, TCIOFLUSH);
    cfsetispeed(&Opt, speed);
    cfsetospeed(&Opt, speed);
    status = tcsetattr(FD, TCSANOW, &Opt);
    if (status != 0) {
        LOGE("tcsetattr FD:%d speed:%d error\n", FD, speed);
        return -1;
    }

    tcflush(FD, TCIOFLUSH);
    return 0;
}

/*****************************************************************************
* Function:      uart_set_parity
* Description:   设置串口数据位 停止位和效验位
* Input:         FD:    文件标识付
*                databits:数据为7,8
*                stopbits:停止位1,2
*                parity:  校验位N,E,O,S
* Output:        None
* Return:        OS_FALSE,OS_TRUE
* Others:        None
*****************************************************************************/
int RS485SerialMulti::setParity(int databits, int stopbits, int parity)
{
    struct termios   Opt;

    tcgetattr(FD, &Opt);
    //tcflush(FD, TCIOFLUSH);

    //设置数据位数
    Opt.c_cflag &= ~CSIZE;
    switch (databits)
    {
        case 5	:
            Opt.c_cflag |= CS5;
            break;
        case 6	:
            Opt.c_cflag |= CS6;
            break;
        case 7:
            Opt.c_cflag |= CS7;
            break;
        case 8:
            Opt.c_cflag |= CS8;
            break;
        default:
            LOGE("DecoderSetCom: Unsupported data size\n");
            return -1;
    }

    //设置校验位
    switch (parity)
    {
        case 'n':
        case 'N':
            Opt.c_cflag &= ~PARENB;             // Clear parity enable
            Opt.c_iflag &= ~INPCK;              // Clear parity checking
            break;

        case 'o':
        case 'O':
            Opt.c_cflag |= (PARODD | PARENB);   // 设置为奇效验
            Opt.c_iflag |= INPCK;               // Enable parity checking
            break;

        case 'e':
        case 'E':
            Opt.c_cflag |= PARENB;              // Enable parity
            Opt.c_cflag &= ~PARODD;             // 转换为偶效验
            Opt.c_iflag |= INPCK;               // Enable parity checking
            break;
        default:
            LOGE("DecoderSetCom: Unsupported parity\n");
            return -1;
    }

    //设置停止位
    switch (stopbits)
    {
        case 1:
            Opt.c_cflag &= ~CSTOPB;
            break;
        case 2:
            Opt.c_cflag |= CSTOPB;
            break;
        default:
            LOGE("DecoderSetCom: Unsupported stop bits\n");
            return -1;
    }

    //其他属性
    Opt.c_cflag &= ~CRTSCTS;
    Opt.c_cflag |= (CLOCAL | CREAD);

    Opt.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    Opt.c_lflag |= FLUSHO;	// Output flush

    Opt.c_oflag &= ~OPOST;
    Opt.c_cc[VMIN] = 0;
    Opt.c_cc[VTIME] = 1;
    Opt.c_iflag = IGNBRK | IGNPAR;

    //设置串口属性
    if (0 != tcsetattr(FD, TCSANOW, &Opt))
    {
        LOGE("DecoderSetCom: tcsetattr err\n");
        return -1;
    }

    //清空串口数据
    tcflush(FD,TCIOFLUSH);
    return 0;
}

int RS485SerialMulti::rsWrite(char *buf, int dataLen) {
    int writeLen = 0;

    if (NULL == buf) {
        return -1;
    }


    writeLen = write(FD, buf, dataLen);
    if(DEBUG) LOGD("write FD = %d", FD);
    return writeLen;
}

int RS485SerialMulti::rsRead(char *buf, int MaxLen, int waitTime) {

    int len = 0, nRet = 0;
    struct pollfd stPoll[1];

    stPoll[0].fd = FD;
    stPoll[0].events = POLLIN;

    if (poll(stPoll, 1, waitTime) > 0)           //如果接收不到返回数据，返回错误
    {
        len = 0;
        while (len < MaxLen) {
            if(DEBUG) LOGI("before read");
            nRet = read(FD, (buf + len), MaxLen - len);
            if(DEBUG)  LOGI("nRet:%d data_len:%d\n", nRet, MaxLen - len);
            if (nRet <= 0) {
                break;
            }
            len += nRet;
        }
    }

   if(DEBUG) LOGI("uart_read len:%d \n", len);

    return len;

}

jbyteArray RS485SerialMulti::drain(JNIEnv *env, jobject type, jint time) {
    int len = 0, nRet = 0;
    struct pollfd stPoll[1];
    int revBufSize = 512;
    char recvbuf[revBufSize];

    stPoll[0].fd = FD;
    stPoll[0].events = POLLIN;
    memset(recvbuf, 0, sizeof(recvbuf));

    if (poll(stPoll, 1, time) > 0)           //如果接收不到返回数据，返回错误
    {
        len = 0;
        while (len < revBufSize) {
            nRet = read(FD, (recvbuf + len), 1);
            if (nRet <= 0) {
                break;
            }
            len += nRet;
        }
    }

    if(/*DEBUG && */len > 0) LOGD("drain len :%d \n", len);

    return len == -1 ? nullptr : charToJByteArray(env, recvbuf, len);
}

//485发送接收函数
int RS485SerialMulti::sendWaitRecv(char *sendBuf, int sendLen, char *revBuf, int readMaxLen, int readWaitTime) {
    int len = 0;
    if (FD < 0) {
        LOGE("error FD < 0 ");
        return -1;
    }
    //使能发送
    if(kernelHasDriver){
        int gpioONValue;
        ioctl(FD_IO,GPIO_ON, &gpioONValue) ;
        if (DEBUG) LOGI("GPIO_ON");
    }else if(!autoSend){
        int ret = system(enableSend);
        if(DEBUG) LOGI("enableSend %s %d", enableSend, ret);
    }
    // 开始写入数据
    rsWrite(sendBuf, sendLen);

    //等发送完成
    int nValue;

    do {
        ioctl(FD, TIOCSERGETLSR, &nValue);
    } while( !nValue & TIOCSER_TEMT );

    // 使能接受
    if (kernelHasDriver) {
        // usleep(500*1000);//test
        int gpioOFFValue;
       // usleep(30*1000);
        ioctl(FD_IO, GPIO_OFF, &gpioOFFValue);
        if (DEBUG) LOGI("GPIO_OFF");
    }else if(!autoSend){
        int ret = system(enableRecv);
        if (DEBUG) LOGI("enableRecv %s %d", enableRecv, ret);
    }

    len = rsRead(revBuf, readMaxLen, readWaitTime);

    return len;
}

