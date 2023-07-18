package cn.martinkay.wechatroaming.settings.dsl.func

interface IDslParentNode : IDslItemNode {
    val children: List<IDslItemNode>

    fun findChildById(id: String): IDslItemNode?

    fun getChildAt(index: Int): IDslItemNode

    fun addChild(child: IDslItemNode, index: Int = -1)

    fun removeChild(child: IDslItemNode)

    fun removeChildAt(index: Int)

    fun removeAllChildren()

    fun lookupHierarchy(ids: Array<String>): IDslItemNode?

    fun findChildWithId(id: String): IDslItemNode?

    fun findLocationByIdentifier(identifier: String): Array<String>?
}