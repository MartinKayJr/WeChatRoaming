package cn.martinkay.wechatroaming.settings.dsl.func

import android.os.Bundle
import cn.martinkay.wechatroaming.settings.fragment.BaseSettingFragment

class FragmentImplDescription(
    override val identifier: String,
    override val name: String?,
    private val targetClass: Class<out BaseSettingFragment>,
    categoryTitleSearchable: Boolean = true
) : BaseParentNode(), IDslFragmentNode {
    override val isSearchable: Boolean = categoryTitleSearchable

    override fun getTargetFragmentClass(location: Array<String>): Class<out BaseSettingFragment> {
        return targetClass
    }

    override fun getTargetFragmentArguments(
        location: Array<String>,
        targetItemId: String?
    ): Bundle? {
        // TODO: 2022-02-13: add search navigation support for custom fragment
        return null
    }
}