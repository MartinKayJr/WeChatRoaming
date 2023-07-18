
package cn.martinkay.wechatroaming.settings.dsl.item

interface DslTMsgListItemInflatable {
    fun inflateTMsgListItems(context: android.content.Context): List<TMsgListItem>
}
