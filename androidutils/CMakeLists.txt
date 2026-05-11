cmake_minimum_required(VERSION 3.4.1)
project("qgbfutils")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/build/jniLibs/${ANDROID_ABI})


set(UTILS_PATH src/main/jni-source-utils)
add_library( ${CMAKE_PROJECT_NAME}
        SHARED
        ${UTILS_PATH}/serialport/RS485Serial.cpp
        ${UTILS_PATH}/serialport/RS485SerialMulti.cpp
        ${UTILS_PATH}/serialport/serialport.cpp
        ${UTILS_PATH}/gpio/gpio.cpp
        ${UTILS_PATH}/util/PipeUtil.cpp
        )

target_link_libraries( ${CMAKE_PROJECT_NAME}
        ${log-lib}
        log
        android
        )