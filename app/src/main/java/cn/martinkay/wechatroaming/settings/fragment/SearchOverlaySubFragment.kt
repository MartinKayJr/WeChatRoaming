package cn.martinkay.wechatroaming.settings.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.martinkay.wechatroaming.R
import cn.martinkay.wechatroaming.settings.activity.SettingsUiFragmentHostActivity
import cn.martinkay.wechatroaming.settings.base.IUiItemAgent
import cn.martinkay.wechatroaming.settings.base.IUiItemAgentProvider
import cn.martinkay.wechatroaming.databinding.FragmentSettingSearchBinding
import cn.martinkay.wechatroaming.databinding.SearchResultItemBinding
import cn.martinkay.wechatroaming.settings.dsl.func.IDslFragmentNode
import cn.martinkay.wechatroaming.settings.dsl.func.IDslParentNode
import cn.martinkay.wechatroaming.utils.LayoutHelper
import cn.martinkay.wechatroaming.utils.NonUiThread
import cn.martinkay.wechatroaming.utils.SyncUtils
import cn.martinkay.wechatroaming.utils.processSearchEasterEgg
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

/**
 * The search sub fragment of [SettingsMainFragment]
 */
class SearchOverlaySubFragment {
    var parent: SettingsMainFragment? = null
    var arguments: Bundle? = null
    var context: Context? = null
    var settingsHostActivity: SettingsUiFragmentHostActivity? = null
    private var mView: View? = null

    private var mSearchView: SearchView? = null
    private var binding: FragmentSettingSearchBinding? = null
    private var currentKeyword: String = ""
    private var lastSearchKeyword: String = ""
    private var searchHistoryList: List<String> = listOf()
    private val searchResults: ArrayList<SearchResult> = ArrayList()
    private var mPossibleInputUin: Long = 0L
    private val allItemsContainer: ArrayList<SearchResult> by lazy {
        val items = cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter.queryAnnotatedUiItemAgentEntries()
        ArrayList<SearchResult>(items.size).apply {
            items.forEach {
                add(SearchResult(it))
            }
        }
    }

    fun requireContext(): Context {
        return context!!
    }

    private fun requireActivity(): SettingsUiFragmentHostActivity {
        return settingsHostActivity!!
    }

    private fun requireView(): View {
        return mView!!
    }

    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = doOnCreateView(inflater, container, savedInstanceState)
        return mView
    }

    fun initForSearchView(searchView: SearchView) {
        mSearchView = searchView.apply {
            queryHint = "搜索..."
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    processSearchEasterEgg(newText, requireContext())
                    search(newText)
                    return false
                }
            })

            setIconifiedByDefault(false)
            findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon).apply {
                setImageDrawable(null)
            }
            // hide search plate
            findViewById<View>(androidx.appcompat.R.id.search_plate).apply {
                setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    @UiThread
    fun updateSearchResultForView() {
        binding?.let {
            if (currentKeyword.isEmpty()) {
                it.searchSettingNoResultLayout.visibility = View.GONE
                it.searchSettingSearchResultLayout.visibility = View.GONE
                if (searchHistoryList.isNotEmpty()) {
                    it.searchSettingSearchHistoryLayout.visibility = View.VISIBLE
                }
            } else {
                if (searchResults.isEmpty() && mPossibleInputUin < 10000L) {
                    it.searchSettingNoResultLayout.visibility = View.VISIBLE
                    it.searchSettingSearchResultLayout.visibility = View.GONE
                    it.searchSettingSearchHistoryLayout.visibility = View.GONE
                } else {
                    it.searchSettingNoResultLayout.visibility = View.GONE
                    it.searchSettingSearchResultLayout.visibility = View.VISIBLE
                    it.searchSettingSearchHistoryLayout.visibility = View.GONE
                    mRecyclerAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private class SearchResultViewHolder(val binding: SearchResultItemBinding) : RecyclerView.ViewHolder(binding.root)

    private val mOnSearchHistoryItemClickListener = View.OnClickListener {
        val keyword = (it as TextView).text.toString()
        if (keyword != currentKeyword && keyword.isNotEmpty()) {
            mSearchView!!.setQuery(keyword, true)
            currentKeyword = keyword
        }
    }

    private val mOnSearchHistoryItemLongClickListener = View.OnLongClickListener {
        val keyword = (it as TextView).text.toString()
        if (keyword.isNotEmpty()) {
            AlertDialog.Builder(context!!)
                .setTitle("删除搜索历史")
                .setMessage("确定要删除搜索历史 '$keyword' 吗？")
                .setPositiveButton("确定") { _, _ ->
                    ConfigEntrySearchHistoryManager.removeHistory(keyword)
                    updateHistoryListForView()
                }
                .setCancelable(true)
                .setNegativeButton("取消", null)
                .show()
            true
        } else {
            false
        }
    }

    private class SearchHistoryItemViewHolder(val context: Context, r: TextView) : RecyclerView.ViewHolder(r) {
        val textView: TextView = r

        companion object {
            @JvmStatic
            fun newInstance(that: SearchOverlaySubFragment): SearchHistoryItemViewHolder {
                val context = that.requireContext()
                val v = TextView(context).apply {
                    textSize = 14f
                    isClickable = true
                    isLongClickable = true
                    isFocusable = true
                    gravity = Gravity.CENTER
                    minHeight = LayoutHelper.dip2px(context, 32f)
                    val dp16 = LayoutHelper.dip2px(context, 16f)
                    setPadding(dp16, 0, dp16, 0)
                    setTextColor(ResourcesCompat.getColor(context.resources, R.color.firstTextColor, context.theme))
                    background = ResourcesCompat.getDrawable(context.resources, R.drawable.bg_item_light_grey_r16, context.theme)
                    this.setOnClickListener(that.mOnSearchHistoryItemClickListener)
                    this.setOnLongClickListener(that.mOnSearchHistoryItemLongClickListener)
                }
                return SearchHistoryItemViewHolder(context, v)
            }
        }
    }

    private val mRecyclerAdapter = object : RecyclerView.Adapter<SearchResultViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
            return SearchResultViewHolder(SearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
            if (mPossibleInputUin >= 10000L) {
                if (position >= 2) {
                    val item = searchResults[position - 2]
                    bindSearchResultItem(holder.binding, item)
                } else {
                    bindSearchResultAsUin(holder.binding, mPossibleInputUin, position)
                }
            } else {
                val item = searchResults[position]
                bindSearchResultItem(holder.binding, item)
            }
        }

        override fun getItemCount(): Int {
            return searchResults.size + if (mPossibleInputUin >= 10000L) 2 else 0
        }
    }

    private val mHistoryRecyclerAdapter = object : RecyclerView.Adapter<SearchHistoryItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryItemViewHolder {
            return SearchHistoryItemViewHolder.newInstance(this@SearchOverlaySubFragment)
        }

        override fun onBindViewHolder(holder: SearchHistoryItemViewHolder, position: Int) {
            val keyword = searchHistoryList[position]
            holder.textView.text = keyword
        }

        override fun getItemCount(): Int {
            return searchHistoryList.size
        }
    }

    private fun bindSearchResultItem(binding: SearchResultItemBinding, item: SearchResult) {
        val title: String = item.agent.uiItemAgent.titleProvider.invoke(item.agent.uiItemAgent)
        val description: String = "[${item.score}] " +
                (item.agent.uiItemAgent.summaryProvider?.invoke(item.agent.uiItemAgent, requireContext()) ?: "")
        binding.title.text = title
        binding.summary.text = description
        val locationString = item.shownLocation!!.joinToString(separator = " > ")
        binding.description.text = locationString
        binding.root.setTag(R.id.tag_searchResultItem, item)
        binding.root.setOnClickListener { v ->
            val result = v?.getTag(R.id.tag_searchResultItem) as SearchResult?
            result?.let {
                navigateToTargetSearchResult(it)
            }
        }
    }

    private fun bindSearchResultAsUin(binding: SearchResultItemBinding, uin: Long, uinType: Int) {
        val title: String = when (uinType) {
            0 -> "用户"
            1 -> "群聊"
            else -> "未知"
        } + " $uin"
        val description = "打开资料卡: $title"
        binding.title.text = title
        binding.summary.text = description
        val locationString = ""
        binding.description.text = locationString
        binding.root.setTag(R.id.tag_searchResultItem, null)
        binding.root.setOnClickListener { v ->
            if (uinType == 0) {
//                OpenProfileCard.openUserProfileCard(v.context, uin)
            } else if (uinType == 1) {
//                OpenProfileCard.openTroopProfileActivity(v.context, uin.toString())
            }
        }
    }

    @NonUiThread
    fun search(query: String?) {
        if (query == lastSearchKeyword) return
        currentKeyword = query ?: ""
        mPossibleInputUin = tryParseUin(currentKeyword)
        // search is performed by calculating the score of each item and sort the result by the score
        val keywords: List<String> = currentKeyword.replace("\r", "")
            .replace("\n", "").replace("\t", "")
            .split(" ").filter { it.isNotBlank() && it.isNotEmpty() }
        // update the score of each item
        allItemsContainer.forEach {
            it.score = 0
            keywords.forEach { keyword ->
                it.score += calculatePartialScoreBySingleKeyword(keyword, it.agent.uiItemAgent)
            }
        }
        // find score > 0
        searchResults.clear()
        allItemsContainer.forEach {
            if (it.score > 0) {
                searchResults.add(it)
            }
        }
        // sort by score
        searchResults.sortByDescending { it.score }
        // update the item location if missing
        searchResults.forEach {
            if (it.location == null) {
                updateUiItemAgentLocation(it)
            }
        }
        lastSearchKeyword = currentKeyword
        // update the view
        SyncUtils.runOnUiThread { updateSearchResultForView() }
    }

    private fun calculatePartialScoreBySingleKeyword(keyword: String, item: IUiItemAgent): Int {
        var score = 0
        val context = requireContext()
        val title: String = item.titleProvider.invoke(item).replace(" ", "")
        val summary: String? = item.summaryProvider?.invoke(item, context)?.toString()?.replace(" ", "")?.replace("\n", "")
        val extraKeywords: Array<String>? = item.extraSearchKeywordProvider?.invoke(item, context)
        if (title == keyword) {
            score += 80
        } else if (title.contains(keyword, true)) {
            score += 50
        }
        summary?.let {
            if (it == keyword) {
                score += 40
            } else if (it.contains(keyword, true)) {
                score += 20
            }
        }
        extraKeywords?.let { words ->
            words.forEach {
                if (it == keyword) {
                    score += 10
                } else if (it.contains(keyword, true)) {
                    score += 5
                }
            }
        }
        return score
    }

    private fun updateUiItemAgentLocation(item: SearchResult) {
        val agent = item.agent
        val containerLocation: Array<String> = cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter.resolveUiItemAnycastLocation(agent.uiItemLocation)
            ?: agent.uiItemLocation
        val fullLocation = arrayOf(*containerLocation, agent.itemAgentProviderUniqueIdentifier)
        item.location = fullLocation
        // translate the container location to human readable string
        // e.g. arrayOf("home", "app", "search") -> "Home > App > Search" (and the the target item is "Item 0")
        // get the DSL element of each level to get the title
        // start from the first level
        val currentLocation: ArrayList<String> = ArrayList()
        var currentNode: IDslParentNode? = cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter.settingsUiItemDslTree
        for (i in containerLocation.indices) {
            if (currentNode == null) {
                // we are lost!!! use raw identifier as the location
                currentLocation.add(containerLocation[i])
            } else {
                val nextNode = currentNode.findChildById(containerLocation[i])
                if (nextNode == null) {
                    currentNode = null
                    // we are lost!!! use raw identifier as the location
                    currentLocation.add(containerLocation[i])
                } else {
                    nextNode.name?.let { currentLocation.add(it) }
                    currentNode = if (nextNode is IDslParentNode) {
                        nextNode
                    } else {
                        // this is the target item
                        null
                    }
                }
            }
        }
        item.shownLocation = currentLocation.toTypedArray()
    }



    private data class SearchResult(
        val agent: IUiItemAgentProvider,
        var score: Int = 0,
        var location: Array<String>? = null,
        var shownLocation: Array<String>? = null
    )

    @UiThread
    private fun navigateToTargetSearchResult(item: SearchResult) {
        ConfigEntrySearchHistoryManager.addHistory(currentKeyword)
        updateHistoryListForView()
        if (item.location == null) {
            updateUiItemAgentLocation(item)
        }
        // find containing fragment
        val absFullLocation = item.location!!
        val identifier = absFullLocation.last()
        var container = absFullLocation.dropLast(1).toTypedArray()
        var targetFragmentLocation: Array<String>? = null
        var node = cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter.settingsUiItemDslTree.lookupHierarchy(container)
        // lookup the parent container, until we find the parent is a fragment
        while (true) {
            if (node == null) {
                // we are lost!!!
                break
            }
            if (node is IDslFragmentNode) {
                // found
                targetFragmentLocation = container
                break
            }
            if (container.isEmpty()) {
                // we are lost!!!
                break
            }
            // not a fragment, keep looking up parent
            container = container.dropLast(1).toTypedArray()
            // get current node
            node = cn.martinkay.wechatroaming.settings.dsl.FunctionEntryRouter.settingsUiItemDslTree.lookupHierarchy(container)
        }
        if (targetFragmentLocation == null) {
            // tell user we are lost
            AlertDialog.Builder(requireContext()).apply {
                setTitle("Navigation Error")
                setMessage("We are lost, can't find the target fragment: " + absFullLocation.joinToString("."))
                setPositiveButton(android.R.string.ok) { _, _ -> }
                setCancelable(true)
            }.show()
        } else {
            // hide IME
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            val fragment = SettingsMainFragment.newInstance(targetFragmentLocation, identifier)
            parent!!.onNavigateToOtherFragment()
            settingsHostActivity!!.presentFragment(fragment)
        }
    }

    private fun doOnCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingSearchBinding.inflate(inflater, container, false).apply {
            searchSettingSearchResultRecyclerView.apply {
                adapter = mRecyclerAdapter
                layoutManager = LinearLayoutManager(inflater.context).apply {
                    orientation = LinearLayoutManager.VERTICAL
                }
                id = R.id.fragmentMainRecyclerView // id is used to allow saving state
            }
            searchSettingSearchHistoryRecyclerView.apply {
                adapter = mHistoryRecyclerAdapter
                layoutManager = FlexboxLayoutManager(inflater.context).apply {
                    flexWrap = FlexWrap.WRAP
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                }
                val dp5 = LayoutHelper.dip2px(inflater.context, 5f)
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        outRect.set(dp5, dp5, dp5, dp5)
                    }
                })
            }
            searchSettingClearHistory.setOnClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle("清除历史记录")
                    setMessage("确定要清除历史记录吗？")
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        ConfigEntrySearchHistoryManager.clearHistoryList()
                        searchHistoryList = ConfigEntrySearchHistoryManager.historyList
                        binding!!.searchSettingSearchHistoryLayout.visibility = View.GONE
                        mHistoryRecyclerAdapter.notifyDataSetChanged()
                    }
                    setNegativeButton(android.R.string.cancel) { _, _ -> }
                    setCancelable(true)
                }.show()
            }
        }
        updateHistoryListForView()
        updateSearchResultForView()
        return binding!!.root
    }

    private fun updateHistoryListForView() {
        searchHistoryList = ConfigEntrySearchHistoryManager.historyList
        if (searchHistoryList.isEmpty()) {
            binding!!.searchSettingSearchHistoryLayout.visibility = View.GONE
        }
        mHistoryRecyclerAdapter.notifyDataSetChanged()
    }

    fun onDestroyView() {
        binding = null
        mView = null
        mSearchView = null
    }

    fun onResume() = Unit

    private fun tryParseUin(string: String): Long {
        return try {
            string.trim().toLong()
        } catch (e: NumberFormatException) {
            0L
        }
    }

}