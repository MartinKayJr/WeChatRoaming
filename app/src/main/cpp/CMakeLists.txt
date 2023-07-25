cmake_minimum_required(VERSION 3.10)
project(wechatroaming)

find_library(ANDROID_LIBS log)

macro(SET_OPTION option value)
    set(${option} ${value} CACHE INTERNAL "" FORCE)
endmacro()

enable_language(CXX C)
set(CMAKE_CXX_STANDARD 20)
set(CMAKE_C_STANDARD 11)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Werror=format-invalid-specifier -Werror=return-type -Wno-invalid-offsetof")
if ("${CMAKE_CXX_COMPILER_ID}" MATCHES "Clang")
    SET(CLANG_CXX_EXTRA_OPT "-Werror=unknown-warning-option -Werror=format-invalid-specifier -Werror=call-to-pure-virtual-from-ctor-dtor")
    SET(CLANG_C_EXTRA_OPT "-Werror=format-invalid-specifier")
else ()
    SET(CLANG_CXX_EXTRA_OPT "")
    SET(CLANG_C_EXTRA_OPT "")
endif ()

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CLANG_CXX_EXTRA_OPT} -fPIC -Werror=return-type -Wno-invalid-offsetof")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${CLANG_C_EXTRA_OPT} -fPIC -Werror=return-type")

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--no-allow-shlib-undefined,--no-undefined -Wl,-z,defs,-z,now,-z,relro")

SET_OPTION(DOBBY_GENERATE_SHARED OFF)
SET_OPTION(Plugin.SymbolResolver ON)
add_subdirectory(../../../../libs/Dobby dobby)

# set -fno-omit-frame-pointer for Dobby if ANDROID_ABI is armeabi-v7a
if ("${ANDROID_ABI}" STREQUAL "armeabi-v7a")
    target_compile_options(dobby PRIVATE -fomit-frame-pointer)
endif ()

# add -fno-omit-frame-pointer -Werror=non-virtual-dtor -Werror=delete-non-virtual-dtor
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-omit-frame-pointer -Werror=non-virtual-dtor -Werror=delete-non-virtual-dtor")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fno-omit-frame-pointer")


add_subdirectory(../../../../libs/mmkv mmkv)
add_subdirectory(../../../../libs/dexkit/DexKit/Core dex_kit_static)
add_subdirectory(../../../../libs/libunwindstack unwindstack)
add_subdirectory(../../../../libs/fmt fmt)


target_compile_definitions(fmt-header-only INTERFACE FMT_STATIC_THOUSANDS_SEPARATOR=1 FMT_USE_FLOAT=0 FMT_USE_DOUBLE=0 FMT_USE_LONG_DOUBLE=0)

add_library(wechatroaming SHARED
        misc/version.c

        ../../../../libs/dexkit/DexKit/dexkit/src/main/cpp/native-bridge.cpp

        wechatroaming_core/Natives.cpp
        wechatroaming_core/HostInfo.cc
        wechatroaming_core/NativeCoreBridge.cc

        utils/shared_memory.cpp
        utils/auto_close_fd.cc
        utils/JniUtils.cc
        utils/TextUtils.cc
        utils/ProcessView.cpp
        utils/ElfView.cpp
        utils/FileMemMap.cpp
        utils/ThreadUtils.cc
        utils/MemoryUtils.cc
        utils/ConfigManager.cc
        utils/ElfScan.cc
        utils/AobScanUtils.cc
        utils/arch_utils.cc
        )

target_include_directories(wechatroaming PRIVATE .)

target_compile_definitions(wechatroaming PRIVATE WECHATROAMING_VERSION=\"${WECHATROAMING_VERSION}\")
target_link_options(wechatroaming PRIVATE "-Wl,-e,__libwechatroaming_main")

target_link_libraries(wechatroaming dobby mmkv dex_kit_static unwindstack base c dl fmt-header-only z ${ANDROID_LIBS})
