package cn.martinkay.wechatroaming.settings.base

import android.content.Context
import cn.martinkay.wechatroaming.settings.step.Step

/**
 * The base interface for dynamic hooks.
 * It's just a hook, not a function, having nothing to do with the UI.
 */
interface IDynamicHook {

    /**
     * Check if the hook needs to initialize.
     * Note that a hook may be required to initialize but not initialized successfully.
     * If you want to check if the hook is initialized and successfully, use [isInitializationSuccessful] instead.
     * Return true if the hook is ever attempted to initialize and you don't want [initialize] to be called again.
     *
     * @return true if the hook is initialized.
     */
    val isInitialized: Boolean

    /**
     * Check if the hook is initialized and ready to be used.
     * If initialization is not successful, the hook should not be used, and this method should return false.
     *
     * @return true if the hook is initialized and usable.
     */
    val isInitializationSuccessful: Boolean

    /**
     * Initialize the hook.
     * Note that you MUST NOT take too much time to initialize the hook.
     * Because the initialization may be called in main thread.
     * Avoid time-consuming operations in this method.
     *
     * @return true if initialization is successful.
     */
    fun initialize(): Boolean

    /**
     * Get the errors if anything goes wrong.
     * Note that this method has NOTHING to do with the initialization.
     * You should NOT modify the returned value, treat it read-only.
     *
     * @return the errors, empty if no errors.
     */
    val runtimeErrors: List<Throwable>

    /**
     * Target effective process for the hook.
     * @see io.github.qauxv.util.SyncUtils.getProcessType
     */
    val targetProcesses: Int

    /**
     * Check if the hook is effective for the current process.
     * @see io.github.qauxv.util.SyncUtils.getProcessType
     * @see targetProcesses
     */
    val isTargetProcess: Boolean

    /**
     * Whether the hook is enabled by user.
     */
    var isEnabled: Boolean

    /**
     * Check if the hook is compatible with the current application.
     * If the hook is not compatible, the hook should not be used, and initialize() shall NOT be called.
     * This method is called before initialize() on main thread.
     * Avoid time-consuming operations in this method.
     *
     * @return true if the hook is compatible
     */
    val isAvailable: Boolean

    /**
     * Some hooks may need to do some time-consuming operations before initialization.
     * Such as dex-deobfuscation, or some other operations.
     *
     * @return true if the hook wants to do some time-consuming operations before initialization.
     */
    val isPreparationRequired: Boolean

    /**
     * Get the steps which are required to be done before initialization.
     * You may perform time-consuming operations in those steps.
     *
     * @return a step array, or null if no preparation is required.
     */
    fun makePreparationSteps(): Array<Step>?

    /**
     * Whether an application restart required to use this hook
     *
     * @return true if a restart is required.
     */
    val isApplicationRestartRequired: Boolean
}
