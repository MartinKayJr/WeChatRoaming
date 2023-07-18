package cn.martinkay.wechatroaming.settings.fragment

import cn.martinkay.wechatroaming.config.ConfigManager

object ConfigEntrySearchHistoryManager {

    // the ordered search history, split by '\n'
    private const val KEY_SEARCH_HISTORY_LIST = "search_history_list"

    // the max length of search history list, set to 0 to disable
    private const val KEY_SEARCH_HISTORY_LIST_SIZE = "search_history_list_size"

    private const val SEARCH_HISTORY_LIST_SIZE_DEFAULT = 30

    private val cfg: ConfigManager by lazy {
        ConfigManager.getDefaultConfig()
    }

    val historyList: List<String>
        get() = getHistoryListInternal()

    var maxHistoryListSize: Int
        get() = cfg.getInt(KEY_SEARCH_HISTORY_LIST_SIZE, SEARCH_HISTORY_LIST_SIZE_DEFAULT)
        set(value) {
            cfg.putInt(KEY_SEARCH_HISTORY_LIST_SIZE, value)
            // trim the list
            val list = historyList
            if (list.size > value) {
                val newList = list.subList(0, value)
                updateHistoryListInternal(newList)
            }
        }

    fun addHistory(history: String) {
        val list = ArrayList(getHistoryListInternal())
        // check if the history is already in the list, if so, remove it and add it to the head
        val index = list.indexOf(history)
        if (index >= 0) {
            list.removeAt(index)
        }
        list.add(0, history)
        // trim the list if necessary
        if (list.size > maxHistoryListSize) {
            list.subList(maxHistoryListSize, list.size).clear()
        }
        updateHistoryListInternal(list)
    }

    fun removeHistory(history: String) {
        val list = ArrayList(getHistoryListInternal()).filter { it != history }
        updateHistoryListInternal(list)
    }

    val isHistoryListEnabled: Boolean
        get() = maxHistoryListSize > 0

    fun clearHistoryList() {
        cfg.putString(KEY_SEARCH_HISTORY_LIST, "")
    }

    private fun getHistoryListInternal(): List<String> {
        return cfg.getString(KEY_SEARCH_HISTORY_LIST, "").orEmpty()
            .split("\n").filter { it.isNotEmpty() }
    }

    private fun updateHistoryListInternal(list: List<String>) {
        cfg.putString(KEY_SEARCH_HISTORY_LIST, list.filter { it.isNotEmpty() }.joinToString("\n"))
    }
}
