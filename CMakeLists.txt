cmake_minimum_required(VERSION 2.8.4)

if (POLICY CMP0048)
    cmake_policy(SET CMP0048 OLD)
endif (POLICY CMP0048)

project(shiruka C CXX)

set(CMAKE_BUILD_TYPE Release)
set(SRC_DIR src/main/c)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY src/main/resources)

find_package(JNI REQUIRED)

add_subdirectory(${SRC_DIR}/zlib)
add_subdirectory(${SRC_DIR}/mbedtls)
include_directories(${SRC_DIR}/mbedtls/include)
include_directories(${SRC_DIR}/zlib)

set(MBED_SOURCE src/main/c/mbedtls/library/sha256.c src/main/c/mbedtls/library/aes.c src/main/c/mbedtls/library/aesni.c
        src/main/c/mbedtls/library/platform_util.c)
include_directories(${JNI_INCLUDE_DIRS})

add_library(shiruka SHARED ${MBED_SOURCE} src/main/c/NativeSha256.c src/main/c/NativeAes.c)
