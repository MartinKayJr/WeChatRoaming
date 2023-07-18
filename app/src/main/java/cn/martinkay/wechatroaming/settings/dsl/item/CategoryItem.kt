package cn.martinkay.wechatroaming.settings.dsl.item

import android.content.Context
import android.view.View
import cn.martinkay.wechatroaming.settings.dsl.item.impl.HeaderItem
import cn.martinkay.wechatroaming.settings.dsl.item.impl.SpacerItem

open class CategoryItem(
        val titleString: String,
        private val initializer: (CategoryItem.() -> Unit)?
) : DslTMsgListItemInflatable {

    private val dslItems = ArrayList<DslTMsgListItemInflatable>()
    private lateinit var listItems: ArrayList<TMsgListItem>
    private var isAfterBuild: Boolean = false

    override fun inflateTMsgListItems(context: Context): List<TMsgListItem> {
        if (!::listItems.isInitialized) {
            initializer?.invoke(this)
            isAfterBuild = true
            listItems = ArrayList()
            listItems.add(HeaderItem(titleString))
            dslItems.forEach {
                listItems.addAll(it.inflateTMsgListItems(context))
            }
            listItems.add(SpacerItem())
        }
        return listItems.toMutableList()
    }

    open fun description(
            text: CharSequence,
            isTextSelectable: Boolean = false,
    ) = DescriptionItem(text, isTextSelectable).also {
        checkState()
        dslItems.add(it)
    }

    open fun textItem(
            title: String,
            summary: String? = null,
            value: String? = null,
            onClick: View.OnClickListener? = null
    ) {
        checkState()
        dslItems.add(TextListItem(title, summary, value, onClick))
    }

    @JvmOverloads
    open fun add(item: DslTMsgListItemInflatable, index: Int = -1) {
        checkState()
        if (index < 0) {
            dslItems.add(item)
        } else {
            dslItems.add(index, item)
        }
    }

    open fun add(items: List<DslTMsgListItemInflatable>) {
        checkState()
        dslItems.addAll(items)
    }

    private fun checkState() {
        if (isAfterBuild) {
            throw IllegalStateException("you can't add item after build")
        }
    }
}
