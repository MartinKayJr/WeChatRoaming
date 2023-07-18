package cn.martinkay.wechatroaming.settings.dsl

import cn.martinkay.wechatroaming.BuildConfig
import cn.martinkay.wechatroaming.settings.base.IUiItemAgentProvider
import cn.martinkay.wechatroaming.settings.dsl.func.*

object FunctionEntryRouter {

    // cn.martinkay.wechatroaming.gen.getAnnotatedUiItemAgentEntryList() : Array<IUiItemAgentProvider>
    private val mAnnotatedUiItemAgentDescriptionList: Array<IUiItemAgentProvider> by lazy {
        cn.martinkay.wechatroaming.gen.getAnnotatedUiItemAgentEntryList()
    }

    /**
     * The full UI-DSL-function tree
     */
    @JvmStatic
    val settingsUiItemDslTree: RootFragmentDescription by lazy {
        zwBuildUiItemDslTree()
    }

    /**
     * Skeletons of the DSL tree, used for any-cast lookup
     */
    private val settingsUiItemDslTreeSkeleton: RootFragmentDescription by lazy {
        zwCreateBaseDslTree()
    }

    @JvmStatic
    fun findDescriptionByLocation(location: Array<String>): IDslItemNode? {
        val absoluteLocation = resolveUiItemAnycastLocation(location) ?: return null
        if (absoluteLocation.isEmpty() || absoluteLocation.size == 1 && absoluteLocation[0] == "") {
            // root
            return settingsUiItemDslTree
        }
        return settingsUiItemDslTree.lookupHierarchy(absoluteLocation)
    }

    @JvmStatic
    fun queryAnnotatedUiItemAgentEntries(): Array<IUiItemAgentProvider> {
        return mAnnotatedUiItemAgentDescriptionList
    }

    @JvmStatic
    fun resolveUiItemAnycastLocation(location: Array<String>): Array<String>? {
        // check if the location is a anycast location
        if (location.size != 2 || location[0] != Locations.ANY_CAST_PREFIX) {
            // return as is
            return location
        }
        val tag = location[1]
        if (tag.isEmpty()) {
            return arrayOf()
        }
        return settingsUiItemDslTreeSkeleton.findLocationByIdentifier(tag)
    }

    /**
     * Create a DSL tree for the function entry. The tree is a tree of [RootFragmentDescription]
     * Categories are defined here. Keep values sync with the [Locations].
     * Otherwise, the tree will be broken and items will be thrown into lost-and-found.
     */
    private fun zwCreateBaseDslTree(): RootFragmentDescription {
        val baseTree = RootFragmentDescription {
            category("core", "核心功能") {
                fragment("core-spider", "蛛网") {
                    category("", "")
                }
            }
            category("module-config", "配置", false) {
            }
            category("debug-category", "调试", false) {
                if (BuildConfig.DEBUG) {
                    fragment("debug-function", "调试功能", false)
                }
            }
        }
        return baseTree
    }

    private fun zwBuildUiItemDslTree(): RootFragmentDescription {
        val baseTree: RootFragmentDescription = zwCreateBaseDslTree()
        val lostAndFoundItems = mutableListOf<IUiItemAgentProvider>()
        val annotatedUiItemAgentEntries = queryAnnotatedUiItemAgentEntries()
        for (uiItemAgentEntry in annotatedUiItemAgentEntries) {
            var location = uiItemAgentEntry.uiItemLocation
            location = resolveUiItemAnycastLocation(location) ?: location
            // find the parent node
            val parentNode = baseTree.lookupHierarchy(location)
            if (parentNode is IDslParentNode) {
                parentNode.addChild(UiItemAgentDescription(uiItemAgentEntry))
            } else {
                // not found, add to lost and found
                lostAndFoundItems.add(uiItemAgentEntry)
            }
        }
        if (lostAndFoundItems.isNotEmpty()) {
            // create a lost and found node
            val lostAndFoundFragmentDescription =
                FragmentDescription("lost-and-found", "Lost & Found") {
                    lostAndFoundItems.forEach {
                        addChild(UiItemAgentDescription(it))
                    }
                }
            // add to the top of the tree to make it the first node
            baseTree.addChild(lostAndFoundFragmentDescription, 0)
            // sync with the skeleton
            settingsUiItemDslTreeSkeleton.addChild(
                FragmentDescription(
                    "lost-and-found",
                    "Lost & Found",
                    false,
                    null
                ), 0
            )
        }
        return baseTree
    }


    /**
     * Static routing destinations
     */
    class Locations {
        companion object {
            const val ANY_CAST_PREFIX: String = "@any-cast"
        }

        object Core {
            /**
             * 蛛网
             */
            @JvmField
            val CORE_SPIDER: Array<String> = arrayOf(ANY_CAST_PREFIX, "core-spider")
        }

        object ConfigCategory {

            @JvmField
            val CONFIG_CATEGORY: Array<String> = arrayOf(ANY_CAST_PREFIX, "module-config")

        }

        object DebugCategory {

            @JvmField
            val DEBUG_CATEGORY: Array<String> = arrayOf(ANY_CAST_PREFIX, "debug-function")

        }
    }

}

