package cn.martinkay.wechatroaming.utils.ui.dsl

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.martinkay.wechatroaming.R
import cn.martinkay.wechatroaming.settings.dsl.item.DslTMsgListItemInflatable
import cn.martinkay.wechatroaming.settings.dsl.item.TMsgListItem
import cn.martinkay.wechatroaming.settings.dsl.item.UiAgentItem
import cn.martinkay.wechatroaming.utils.SyncUtils
import cn.martinkay.wechatroaming.utils.ui.drawable.BackgroundDrawableUtils
import kotlinx.coroutines.flow.MutableStateFlow

class RecyclerListViewController(
        val context: Context,
        val lifecycleScope: LifecycleCoroutineScope,
) {

    // DSL stuff below

    var typeList: Array<Class<*>> = emptyArray()
    var itemList: ArrayList<TMsgListItem> = ArrayList()
    var itemTypeIds: Array<Int> = emptyArray()
    var itemTypeDelegate: Array<TMsgListItem> = emptyArray()

    var recyclerListView: RecyclerView? = null


    var adapter: RecyclerView.Adapter<*>? = null
    val layoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    var items: Array<DslTMsgListItemInflatable>? = null
        set(value) {
            field = value
            updateDslItems()
        }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateDslItems() {
        // inflate DSL tree
        itemList = ArrayList()
        // inflate hierarchy recycler list view items, each item will have its own view holder type
        items?.forEach {
            itemList.addAll(it.inflateTMsgListItems(context))
        }
        // group items by java class
        typeList = itemList.map { it.javaClass }.distinct().toTypedArray()
        // item id to type id mapping
        itemTypeIds = Array(itemList.size) {
            typeList.indexOf(itemList[it].javaClass)
        }
        // item type delegate is used to create view holder
        itemTypeDelegate = Array(typeList.size) {
            itemList[itemTypeIds.indexOf(it)]
        }
        if (adapter != null && recyclerListView != null) {
            SyncUtils.runOnUiThread {
                adapter!!.notifyDataSetChanged()
            }
        }
        // collect all StateFlow and observe them in case of state change
        for (i in itemList.indices) {
            val item = itemList[i]
            if (item is UiAgentItem) {
                val valueStateFlow: MutableStateFlow<String?>? = item.agentProvider.uiItemAgent.valueState
                if (valueStateFlow != null) {
                    lifecycleScope.launchWhenStarted {
                        valueStateFlow.collect {
                            SyncUtils.runOnUiThread { adapter?.notifyItemChanged(i) }
                        }
                    }
                }
            }
        }
    }

    fun initAdapter() {
        // init
        if (adapter == null) {
            // set adapter to default adapter if user does not set it
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val delegate = itemTypeDelegate[viewType]
                    val vh = delegate.createViewHolder(context, parent)
                    if (!delegate.isVoidBackground && delegate.isClickable) {
                        // add ripple effect
                        val rippleColor: Int = ResourcesCompat.getColor(context.resources, R.color.rippleColor, parent.context.theme)
                        vh.itemView.background = BackgroundDrawableUtils.getRoundRectSelectorDrawable(parent.context, rippleColor)
                    }
                    return vh
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val item = itemList[position]
                    item.bindView(holder, position, context)
                }

                override fun getItemCount() = itemList.size

                override fun getItemViewType(position: Int) = itemTypeIds[position]
            }
        }
    }

    fun initRecyclerListView() {
        // init
        if (recyclerListView == null) {
            recyclerListView = RecyclerView(context).apply {
                layoutManager = this@RecyclerListViewController.layoutManager
                adapter = this@RecyclerListViewController.adapter
                clipToPadding = false
            }
        }
    }

}
