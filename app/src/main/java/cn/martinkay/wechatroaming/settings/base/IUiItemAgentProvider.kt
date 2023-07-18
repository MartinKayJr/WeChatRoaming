package cn.martinkay.wechatroaming.settings.base

interface IUiItemAgentProvider {
    val uiItemAgent: IUiItemAgent

    val uiItemLocation: Array<String>

    val itemAgentProviderUniqueIdentifier: String get() = javaClass.name
}