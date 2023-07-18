package cn.martinkay.wechatroaming.settings.dsl.func

abstract class BaseParentNode : IDslParentNode {
    protected open val mChildren: MutableList<IDslItemNode> = mutableListOf()
    override val children: List<IDslItemNode> get() = mChildren

    override val isSearchable: Boolean = true

    override fun findChildById(id: String): IDslItemNode? {
        return mChildren.find { it.identifier == id }
    }

    override fun getChildAt(index: Int): IDslItemNode {
        return mChildren[index]
    }

    override fun addChild(child: IDslItemNode, index: Int) {
        // 检查是否存在相同id的子节点
        if (mChildren.find { it.identifier == child.identifier } != null) {
            throw IllegalAccessException("child with id '${child.identifier}' already exists")
        }
        if (index < 0 || index > mChildren.size) {
            mChildren.add(child)
        } else {
            mChildren.add(index, child)
        }
    }

    override fun removeChild(child: IDslItemNode) {
        mChildren.remove(child)
    }

    override fun removeChildAt(index: Int) {
        mChildren.removeAt(index)
    }

    override fun removeAllChildren() {
        mChildren.clear()
    }

    override fun lookupHierarchy(ids: Array<String>): IDslItemNode? {
        if (ids.isEmpty() || (ids.size == 1 && ids[0] == "")) {
            return this
        }
        val child = findChildById(ids[0]) ?: return null
        if (ids.size == 1) {
            return child
        }
        // 并行递归查找
        if (child is IDslParentNode) {
            return child.lookupHierarchy(ids.copyOfRange(1, ids.size))
        }
        return null
    }

    override fun findChildWithId(id: String): IDslItemNode? {
        return mChildren.find { it.identifier == id }
    }

    override fun findLocationByIdentifier(identifier: String): Array<String>? {
        if (identifier == this.identifier) {
            // Self，返回空数组
            return arrayOf()
        }
        for (i in 0 until mChildren.size) {
            val child = mChildren[i]
            if (child.identifier == identifier) {
                // 找到
                return arrayOf(child.identifier)
            }
            if (child is IDslParentNode) {
                val location = child.findLocationByIdentifier((identifier))
                if (location != null) {
                    // 找到
                    return arrayOf(child.identifier, *location)
                }
            }
        }
        // 未找到
        return null
    }
}