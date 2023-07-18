package cn.martinkay.wechatroaming.settings.dsl.func

interface IDslItemNode {
    /**
     * 此节点的标识符，将用于在 UI 中标识此节点。
     * 该标识符在同一父项的所有子项中应该是唯一的，
     * 但在整棵树的所有节点中不一定是唯一的。
     */
    val identifier: String

    /**
     * 此节点的人类可读名称，将显示在 UI 中。
     */
    val name: String?

    /**
     * 该节点是否会出现在搜索结果中。
     */
    val isSearchable: Boolean
}