package cn.martinkay.wechatroaming.settings.dsl.item.impl

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.martinkay.wechatroaming.settings.dsl.cell.SpacerCell
import cn.martinkay.wechatroaming.settings.dsl.item.TMsgListItem

class SpacerItem : TMsgListItem {
    class HeaderViewHolder(cell: SpacerCell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = false
    override val isVoidBackground = false
    override val isClickable = false
    override val isLongClickable = false

    override fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder(SpacerCell(context))
    }

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        // check cast
        viewHolder.itemView as SpacerCell
        // nothing more to do
    }

    override fun onItemClick(v: View, position: Int, x: Int, y: Int) {
        // do nothing
    }
}
