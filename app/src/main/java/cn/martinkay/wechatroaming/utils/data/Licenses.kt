package cn.martinkay.wechatroaming.utils.data

import cn.martinkay.wechatroaming.R
import cn.martinkay.wechatroaming.settings.ui.ResUtils
import cn.martinkay.wechatroaming.utils.decodeToDataClass
import cn.martinkay.wechatroaming.utils.host.hostInfo
import kotlinx.serialization.Serializable

object Licenses {
    @Serializable
    data class AboutLibraries(
        val libraries: List<LibraryLicense>
    )

    @Serializable
    data class DeveloperInfo(
        val name: String
    )

    @Serializable
    data class LibraryLicense(
        val uniqueId: String,
        val website: String? = null,
        val licenses: List<String>,
        val developers: List<DeveloperInfo>
    ) {
        fun getAuthor(): String {
            return developers.joinToString(",") { it.name }
        }
    }

    val list: List<LibraryLicense> by lazy {
        val libs = hostInfo.application.resources.openRawResource(R.raw.aboutlibraries)
        val content = libs.bufferedReader().use { x -> x.readText() }
        val info: AboutLibraries = content.decodeToDataClass()
        info.libraries.filter { it.website != null }
    }
}
