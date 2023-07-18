package cn.martinkay.wechatroaming.settings.base

import android.app.Activity
import android.content.Context
import android.view.View
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * It's a single "cell" of the UI on the settings page.
 * A cell must have a title,
 * and may or may not have a description(usually gray text),
 * and may or may not have a switch,
 * and may or may not have a value(usually orange text),
 * and may or may not have a on click listener.
 *
 * Cells are searchable by their title description. (Value is not searchable)
 * You can provide extra searchable words by implementing [IUiItemAgent.extraSearchKeywordProvider],
 * The search is implemented by full-text search, so you may want to keep the search key words long.
 * the search is like
 * ```
 * int score = 0;
 * if(title == userSearchInput) {
 *    score += 100;
 * } else if (title.contains(userSearchInput)) {
 *    score += 20;
 * }
 * ... // same to description
 * for(auto &word : extraSearchKeyWords?.invoke(...)?: emptyList()) {
 *    if (word == userSearchInput) {
 *        score += 50;
 *    } else if (word.contains(userSearchInput)) {
 *        score += 10;
 *    }
 * }
 * return score; // if score is 0, the cell will not show in the search result.
 * ```
 * If it has a on click listener, this cell will be clickable. If it don't have a validator, it will always be valid.
 *
 * You can't both have a switch, value, and description at the same time, if you do, the description will be ignored.
 */
interface IUiItemAgent {
    val titleProvider: (IUiItemAgent) -> String
    val summaryProvider: ((IUiItemAgent, Context) -> CharSequence?)?
    val valueState: MutableStateFlow<String?>?
    val validator: ((IUiItemAgent) -> Boolean)?
    val switchProvider: ISwitchCellAgent?
    val onClickListener: ((IUiItemAgent, Activity, View) -> Unit)?
    val extraSearchKeywordProvider: ((IUiItemAgent, Context) -> Array<String>?)?
}