package cn.martinkay.wechatroaming.settings.dsl.item.impl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.martinkay.wechatroaming.settings.dsl.cell.HeaderCell
import cn.martinkay.wechatroaming.settings.dsl.item.TMsgListItem
class HeaderItem(
        val headerText: String?
) : TMsgListItem {

    class HeaderViewHolder(cell: HeaderCell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = false
    override val isVoidBackground = false
    override val isClickable = false
    override val isLongClickable = false

    override fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder(HeaderCell(context))
    }

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        val cell = viewHolder.itemView as HeaderCell
        cell.title = headerText.orEmpty()
    }

    override fun onItemClick(v: View, position: Int, x: Int, y: Int) {
        // do nothing
    }
}
