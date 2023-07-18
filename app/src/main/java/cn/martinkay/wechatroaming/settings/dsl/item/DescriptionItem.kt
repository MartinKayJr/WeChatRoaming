package cn.martinkay.wechatroaming.settings.dsl.item

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.martinkay.wechatroaming.settings.dsl.cell.TextInfoCell

class DescriptionItem(
    private val textString: CharSequence,
    private val textIsSelectable: Boolean = false,
) : DslTMsgListItemInflatable, TMsgListItem {

    class HeaderViewHolder(cell: TextInfoCell) : RecyclerView.ViewHolder(cell)

    override val isEnabled = false
    override val isClickable = false
    override val isLongClickable = false
    override val isVoidBackground = false

    override fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder(TextInfoCell(context))
    }

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        val cell = viewHolder.itemView as TextInfoCell
        cell.text = textString
        cell.textIsSelectable = textIsSelectable
    }

    override fun onItemClick(v: View, position: Int, x: Int, y: Int) {
        // do nothing
    }
}
