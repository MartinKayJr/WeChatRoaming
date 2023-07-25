@file:JvmName("HostInfo")
package cn.martinkay.wechatroaming.utils.host

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat

const val PACKAGE_NAME_WECHAT = "com.tencent.mm"
const val PACKAGE_NAME_SELF = "cn.martinkay.wechatroaming"

lateinit var hostInfo: HostInfoImpl

fun init(applicationContext: Application) {
    if (::hostInfo.isInitialized) throw IllegalStateException("Host Information Provider has been already initialized")
    val packageInfo = getHostInfo(applicationContext)
    val packageName = applicationContext.packageName
    hostInfo = HostInfoImpl(
        applicationContext,
        packageName,
        applicationContext.applicationInfo.loadLabel(applicationContext.packageManager).toString(),
        PackageInfoCompat.getLongVersionCode(packageInfo),
        PackageInfoCompat.getLongVersionCode(packageInfo).toInt(),
        packageInfo.versionName,
        when (packageName) {
            PACKAGE_NAME_WECHAT -> HostSpecies.Wechat
            PACKAGE_NAME_SELF -> HostSpecies.QAuxiliary
            else -> HostSpecies.Unknown
        },
    )
}

private fun getHostInfo(context: Context): PackageInfo {
    try {
        return context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("Utils", "Can not get PackageInfo!", e)
        throw e
    }
}

fun requireMinQQVersion(versionCode: Long): Boolean {
    return requireMinVersion(versionCode, HostSpecies.Wechat)
}
fun requireMinVersion(versionCode: Long, hostSpecies: HostSpecies): Boolean {
    return hostInfo.hostSpecies == hostSpecies && hostInfo.versionCode >= versionCode
}

fun requireMinVersion(
    QQVersionCode: Long = Long.MAX_VALUE,
    TimVersionCode: Long = Long.MAX_VALUE,
    PlayQQVersionCode: Long = Long.MAX_VALUE
): Boolean {
    return requireMinQQVersion(QQVersionCode)
}

val isInModuleProcess: Boolean
    get() = hostInfo.hostSpecies == HostSpecies.QAuxiliary

val isInHostProcess: Boolean get() = !isInModuleProcess

val isAndroidxFileProviderAvailable: Boolean by lazy {
    val ctx = hostInfo.application
    // check if androidx.core.content.FileProvider is available
    val pm = ctx.packageManager
    try {
        pm.getProviderInfo(ComponentName(hostInfo.packageName, "androidx.core.content.FileProvider"), 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

data class HostInfoImpl(
    val application: Application,
    val packageName: String,
    val hostName: String,
    val versionCode: Long,
    val versionCode32: Int,
    val versionName: String,
    val hostSpecies: HostSpecies
)

enum class HostSpecies {
    Wechat,
    QAuxiliary,
    Unknown
}