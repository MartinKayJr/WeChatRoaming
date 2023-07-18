package cn.martinkay.wechatroaming.settings.dsl.func

import android.os.Bundle
import cn.martinkay.wechatroaming.settings.fragment.BaseSettingFragment

/**
 * It's a fragment node.
 */
interface IDslFragmentNode {
    /**
     * 获取Fragment的类。
     * @param location Fragment的位置
     */
    fun getTargetFragmentClass(location: Array<String>): Class<out BaseSettingFragment>

    /**
     * 获取Fragment的参数
     * @param location Fragment的位置
     * @param targetItemId 目标项(用于搜索结果导航)。
     */
    fun getTargetFragmentArguments(location: Array<String>, targetItemId: String? = null): Bundle?
}