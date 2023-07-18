project(wechatroaming)
# 添加如下两行，注意必须添加 libz，如果你有其他依赖可以放在后面
#find_package(dexkit REQUIRED CONFIG)

#add_subdirectory(../../../../../libs/mmkv ../../../../../libs/mmkv/MMKV)


add_library(wechatroaming SHARED
        wechatroaming/Natives.cpp)

target_link_libraries(wechatroaming mmkv #[[dexkit::dex_kit_static]] c dl z ${ANDROID_LIBS})