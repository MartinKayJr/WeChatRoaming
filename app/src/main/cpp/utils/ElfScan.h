//
// Created by sulfate on 2023-06-24.
//

#ifndef WECHATROAMING_ELFSCAN_H
#define WECHATROAMING_ELFSCAN_H

#include <cstdint>
#include <string>
#include <vector>
#include <span>

namespace utils {

std::vector<uint64_t> FindByteSequenceForImageFile(const void* baseAddress, std::span<const uint8_t> sequence, bool execMemOnly, int step);

std::vector<uint64_t> FindByteSequenceForLoadedImage(const void* baseAddress, std::span<const uint8_t> sequence, bool execMemOnly, int step);

std::vector<uint64_t> FindByteSequenceImpl(const void* baseAddress, bool isLoaded, std::span<const uint8_t> sequence, bool execMemOnly, int step);

}

#endif //WECHATROAMING_ELFSCAN_H
