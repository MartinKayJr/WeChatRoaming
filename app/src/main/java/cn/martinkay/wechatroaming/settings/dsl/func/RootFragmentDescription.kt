package cn.martinkay.wechatroaming.settings.dsl.func

class RootFragmentDescription(
    initializer: (FragmentDescription.() -> Unit)?
) : FragmentDescription("root", "root", false, initializer) {
    override val name: String? = null
}