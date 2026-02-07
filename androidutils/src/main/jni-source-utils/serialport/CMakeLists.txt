cmake_minimum_required(VERSION 3.4.1)
set(CPATH "${CMAKE_SOURCE_DIR}/src/main/jni-source/serialport")

file(GLOB_RECURSE myCFile "${CPATH}/*")


add_library(
        tc_serialport_n
        SHARED
        serialport.cpp RS485Serial.h RS485Serial.cpp RS485SerialMulti.h RS485SerialMulti.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        tc_serialport_n
        ${log-lib})