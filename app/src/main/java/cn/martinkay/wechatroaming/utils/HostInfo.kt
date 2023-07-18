@file:JvmName("HostInfo")

package cn.martinkay.wechatroaming.utils

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat

const val PACKAGE_NAME_SELF = "cn.martinkay.wechatroaming"

fun init(applicationContext: Context): HostInfoImpl {
    val packageInfo = getHostInfo(applicationContext)
    val packageName = applicationContext.packageName
    return HostInfoImpl(
        applicationContext,
        packageName,
        applicationContext.applicationInfo.loadLabel(applicationContext.packageManager).toString(),
        PackageInfoCompat.getLongVersionCode(packageInfo),
        PackageInfoCompat.getLongVersionCode(packageInfo).toInt(),
        packageInfo.versionName,
        when (packageName) {
            PACKAGE_NAME_SELF -> HostSpecies.BlackSpider
            else -> HostSpecies.Unknown
        },
    )
}

private fun getHostInfo(context: Context): PackageInfo {
    try {
        return context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("Utils", "Can not get PackageInfo!", e)
        throw e
    }
}

//val isInModuleProcess: Boolean
//    get() = hostInfo.hostSpecies == HostSpecies.BlackSpider
//
//val isInHostProcess: Boolean get() = !isInModuleProcess

data class HostInfoImpl(
    val context: Context,
    val packageName: String,
    val hostName: String,
    val versionCode: Long,
    val versionCode32: Int,
    val versionName: String,
    val hostSpecies: HostSpecies
)

enum class HostSpecies {
    BlackSpider,
    Unknown
}
