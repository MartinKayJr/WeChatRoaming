package cn.martinkay.wechatroaming.settings.dsl.func

import android.os.Bundle
import cn.martinkay.wechatroaming.settings.fragment.BaseSettingFragment
import cn.martinkay.wechatroaming.settings.fragment.SettingsMainFragment

open class FragmentDescription(
    override val identifier: String,
    override val name: String?,
    categoryTitleSearchable: Boolean = true,
    initializer: (FragmentDescription.() -> Unit)?
) : BaseParentNode(), IDslFragmentNode {

    init {
        initializer?.invoke(this)
    }

    override val isSearchable: Boolean = categoryTitleSearchable

    open fun category(
        identifier: String,
        name: String,
        categoryTitleSearchable: Boolean = true,
        initializer: (CategoryDescription.() -> Unit)? = null
    ): CategoryDescription = CategoryDescription(identifier, name, categoryTitleSearchable, initializer).also {
            addChild(it)
    }

    open fun fragment(
        identifier: String,
        name: String,
        categoryTitleSearchable: Boolean = true,
        initializer: (FragmentDescription.() -> Unit)? = null
    ) : FragmentDescription = FragmentDescription(identifier, name, categoryTitleSearchable, initializer).also {
        addChild(it)
    }

    open fun fragmentImpl(
        identifier: String,
        name: String,
        targetClass: Class<out BaseSettingFragment>,
        categoryTitleSearchable: Boolean = true
    ) : FragmentImplDescription = FragmentImplDescription(identifier, name, targetClass, categoryTitleSearchable).also {
        addChild(it)
    }

    override fun getTargetFragmentClass(location: Array<String>) = SettingsMainFragment::class.java

    override fun getTargetFragmentArguments(location: Array<String>, targetItemId: String?): Bundle? {
        return SettingsMainFragment.getBundleForLocation(location, targetItemId)
    }

}