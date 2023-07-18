package cn.martinkay.wechatroaming.settings.dsl.func

import cn.martinkay.wechatroaming.settings.base.IUiItemAgentProvider

class UiItemAgentDescription(
    val itemAgentProvider: IUiItemAgentProvider,
) : IDslItemNode {
    override val identifier: String get() = itemAgentProvider.itemAgentProviderUniqueIdentifier

    override val name: String
        get() = itemAgentProvider.uiItemAgent.titleProvider.invoke(
            itemAgentProvider.uiItemAgent
        )

    override val isSearchable: Boolean = true
}