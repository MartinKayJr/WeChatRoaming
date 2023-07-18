package cn.martinkay.wechatroaming.settings.hook

import android.content.Context
import cn.martinkay.wechatroaming.BuildConfig
import cn.martinkay.wechatroaming.settings.base.IDynamicHook
import cn.martinkay.wechatroaming.settings.base.IUiItemAgentProvider
import cn.martinkay.wechatroaming.settings.base.RuntimeErrorTracer
import cn.martinkay.wechatroaming.config.ConfigManager
import cn.martinkay.wechatroaming.settings.step.DexDeobfStep
import cn.martinkay.wechatroaming.settings.step.Step
import cn.martinkay.wechatroaming.utils.Log
import cn.martinkay.wechatroaming.utils.SyncUtils
import cn.martinkay.wechatroaming.utils.dexkit.DexKit
import cn.martinkay.wechatroaming.utils.dexkit.DexKitFinder
import cn.martinkay.wechatroaming.utils.dexkit.DexKitTarget
import java.util.Arrays

abstract class BaseFunctionHook(
    hookKey: String? = null,
    defaultEnabled: Boolean = false,
    targets: Array<DexKitTarget>? = null
) : IDynamicHook, IUiItemAgentProvider, RuntimeErrorTracer {

    private val mErrors: ArrayList<Throwable> = ArrayList()
    private var mInitialized = false
    private var mInitializeResult = false
    private val mHookKey: String = hookKey ?: this::class.java.simpleName
    private val mDefaultEnabled: Boolean = defaultEnabled
    private val mDexDeobfIndexes: Array<DexKitTarget>? = targets

    override val isInitialized: Boolean
        get() = mInitialized

    override val isInitializationSuccessful: Boolean
        get() = mInitializeResult

    override fun initialize(ctx: Context): Boolean {
        if (mInitialized) {
            return mInitializeResult
        }
        mInitializeResult = try {
            initOnce(ctx)
        } catch (e: Throwable) {
            traceError(e)
            // don't throw exception here, except errors like OutOfMemoryError or StackOverflowError
            if (e is Error && e !is AssertionError && e !is LinkageError) {
                // wtf Throwable
                throw e
            }
            false
        }
        mInitialized = true
        return mInitializeResult
    }

    @Throws(Exception::class)
    protected abstract fun initOnce(ctx: Context): Boolean

    override val runtimeErrors: List<Throwable> = mErrors

    override val targetProcesses = SyncUtils.PROC_MAIN

    override val isTargetProcess by lazy { SyncUtils.isTargetProcess(targetProcesses) }

    override val isAvailable = true

    override val isPreparationRequired: Boolean
        get() {
            if (this is DexKitFinder) {
                if (this.isNeedFind) {
                    return true
                }
            }
            if (mDexDeobfIndexes == null) return false
            return mDexDeobfIndexes.any {
                DexKit.isRunDexDeobfuscationRequired(it)
            }
        }

    override fun makePreparationSteps(): Array<Step>? =
        mDexDeobfIndexes?.map {
            DexDeobfStep(
                it
            )
        }?.toTypedArray()

    override val isApplicationRestartRequired = false

    override var isEnabled: Boolean
        get() = enableAllHook() || ConfigManager.getDefaultConfig()
            .getBooleanOrDefault("$mHookKey.enabled", mDefaultEnabled)
        set(value) {
            ConfigManager.getDefaultConfig().putBoolean("$mHookKey.enabled", value)
        }

    override fun traceError(e: Throwable) {
        // check if there is already an error with the same error message and stack trace
        var alreadyLogged = false
        for (error in mErrors) {
            if (error.message == e.message && Arrays.equals(error.stackTrace, e.stackTrace)) {
                alreadyLogged = true
            }
        }
        if (!alreadyLogged) {
            mErrors.add(e)
        }
        Log.e(e)
    }

    private fun enableAllHook(): Boolean {
        return BuildConfig.DEBUG && ConfigManager.getDefaultConfig()
            .getBooleanOrDefault("EnableAllHook.enabled", false)
    }
}
