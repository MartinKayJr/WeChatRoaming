package cn.martinkay.wechatroaming.settings.hook

import android.app.Activity
import android.content.Context
import android.view.View
import cn.martinkay.wechatroaming.settings.base.ISwitchCellAgent
import cn.martinkay.wechatroaming.settings.base.IUiItemAgent
import cn.martinkay.wechatroaming.utils.SyncUtils
import cn.martinkay.wechatroaming.utils.dexkit.DexKitTarget
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A function that only has a enable/disable switch function.
 */
abstract class CommonSwitchFunctionHook(
    hookKey: String? = null,
    defaultEnabled: Boolean = false,
    targets: Array<DexKitTarget>? = null,
    private val targetProc: Int = SyncUtils.PROC_MAIN
) : BaseFunctionHook(hookKey, defaultEnabled, targets = targets) {

    constructor() : this(null, false)
    constructor(defaultEnabled: Boolean) : this(null, defaultEnabled)
    constructor(key: String) : this(key, false)
    constructor(key: String, targets: Array<DexKitTarget>) : this(key, false, targets)
    constructor(targets: Array<DexKitTarget>) : this(null, false, targets)
    constructor(key: String, targetProc: Int) : this(hookKey = key, targetProc = targetProc)
    constructor(targetProc: Int) : this(null, targetProc = targetProc)
    constructor(targetProc: Int, targets: Array<DexKitTarget>) : this(null, targetProc = targetProc, targets = targets)

    /**
     * Name of the function.
     */
    abstract val name: String

    /**
     * Description of the function.
     */
    open val description: CharSequence? = null

    override val targetProcesses = targetProc

    open val extraSearchKeywords: Array<String>? = null

    override val uiItemAgent: IUiItemAgent by lazy {
        object : IUiItemAgent {
            override val titleProvider: (IUiItemAgent) -> String = { _ -> name }
            override val summaryProvider: (IUiItemAgent, Context) -> CharSequence? = { _, _ -> description }
            override val valueState: MutableStateFlow<String?>? = null
            override val validator: ((IUiItemAgent) -> Boolean) = { _ -> true }
            override val switchProvider: ISwitchCellAgent? by lazy {
                object : ISwitchCellAgent {
                    override val isCheckable = true
                    override var isChecked: Boolean
                        get() = isEnabled
                        set(value) {
                            if (value != isEnabled) {
                                isEnabled = value
                            }
                        }
                }
            }
            override val onClickListener: ((IUiItemAgent, Activity, View) -> Unit)? = null
            override val extraSearchKeywordProvider: ((IUiItemAgent, Context) -> Array<String>?)?
                get() = extraSearchKeywords?.let { { _, _ -> it } }
        }
    }
}
