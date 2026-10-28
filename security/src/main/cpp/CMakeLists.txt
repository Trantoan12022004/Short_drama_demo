cmake_minimum_required(VERSION 3.22.1)

project(security_native LANGUAGES CXX)

add_library(security-native SHARED security_native.cpp)

target_compile_features(security-native PRIVATE cxx_std_17)

target_compile_options(security-native PRIVATE
        -fvisibility=hidden
        -fvisibility-inlines-hidden
)

target_link_options(security-native PRIVATE
        -Wl,--exclude-libs,ALL
        -Wl,--gc-sections
)

target_link_libraries(security-native android log)
