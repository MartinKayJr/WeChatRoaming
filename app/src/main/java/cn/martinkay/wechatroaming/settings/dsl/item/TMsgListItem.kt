package cn.martinkay.wechatroaming.settings.dsl.item

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

interface TMsgListItem : DslTMsgListItemInflatable {

    val isEnabled: Boolean
    val isClickable: Boolean
    val isLongClickable: Boolean
    val isVoidBackground: Boolean

    fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder

    fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context)

    fun onItemClick(v: View, position: Int, x: Int, y: Int)

    fun onLongClick(v: View, position: Int, x: Int, y: Int): Boolean {
        return false
    }

    override fun inflateTMsgListItems(context: Context): List<TMsgListItem> {
        return listOf(this)
    }
}
