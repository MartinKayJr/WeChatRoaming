package cn.martinkay.wechatroaming.settings.hook

import android.content.Context
import cn.martinkay.wechatroaming.settings.base.ISwitchCellAgent
import cn.martinkay.wechatroaming.settings.base.IUiItemAgent
import cn.martinkay.wechatroaming.settings.base.IUiItemAgentProvider
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Just a button to be shown [IUiItemAgent]
 */
abstract class BasePlainUiAgentItem(
        val title: String,
        val description: CharSequence?,
) : IUiItemAgent, IUiItemAgentProvider {
    override val titleProvider: (IUiItemAgent) -> String = { title }
    override val summaryProvider: ((IUiItemAgent, Context) -> CharSequence?) = { _, _ -> description }
    override val valueState: MutableStateFlow<String?>? = null
    override val validator: ((IUiItemAgent) -> Boolean)? = null
    override val switchProvider: ISwitchCellAgent? = null
    override val extraSearchKeywordProvider: ((IUiItemAgent, Context) -> Array<String>?)? = null
    override val uiItemAgent: IUiItemAgent = this
}
