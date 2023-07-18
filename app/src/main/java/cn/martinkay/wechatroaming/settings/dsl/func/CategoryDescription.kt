package cn.martinkay.wechatroaming.settings.dsl.func

import cn.martinkay.wechatroaming.settings.fragment.BaseSettingFragment

class CategoryDescription(
    override val identifier: String,
    override val name: String,
    categoryTitleSearchable: Boolean = true,
    initializer: (CategoryDescription.() -> Unit)?
) : BaseParentNode() {
    override val isSearchable: Boolean = categoryTitleSearchable

    init {
        initializer?.invoke(this)
    }

    fun fragment(
        identifier: String,
        name: String,
        categoryTitleSearchable: Boolean = true,
        initializer: (FragmentDescription.() -> Unit)? = null
    ): FragmentDescription = FragmentDescription(identifier, name, categoryTitleSearchable, initializer).also {
            addChild(it)
    }

    open fun fragmentImpl(
        identifier: String,
        name: String,
        targetClass: Class<out BaseSettingFragment>,
        categoryTitleSearchable: Boolean = true
    ):FragmentImplDescription = FragmentImplDescription(identifier, name, targetClass, categoryTitleSearchable).also {
        addChild(it)
    }


}