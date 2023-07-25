// QAuxiliary - An Xposed module for QQ/TIM
// Copyright (C) 2019-2023 QAuxiliary developers
// https://github.com/cinit/QAuxiliary
//
// This software is non-free but opensource software: you can redistribute it
// and/or modify it under the terms of the GNU Affero General Public License
// as published by the Free Software Foundation; either
// version 3 of the License, or any later version and our eula as published
// by QAuxiliary contributors.
//
// This software is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// and eula along with this software.  If not, see
// <https://www.gnu.org/licenses/>
// <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.

//
// Created by sulfate on 2023-05-18.
//

#ifndef WECHATROAMING_TEXTUTILS_H
#define WECHATROAMING_TEXTUTILS_H

#include <vector>
#include <string_view>
#include <string>
#include <cstdint>

namespace utils {

std::vector<std::string> SplitString(std::string_view str, std::string_view delimiter);

bool IsPrintableAscii(char c) noexcept;

std::string LastPartOf(std::string_view str, std::string_view delimiter);

}

#endif //WECHATROAMING_TEXTUTILS_H
