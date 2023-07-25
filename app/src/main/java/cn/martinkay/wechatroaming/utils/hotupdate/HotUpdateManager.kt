package cn.martinkay.wechatroaming.utils.hotupdate

import cn.martinkay.wechatroaming.config.ConfigManager


object HotUpdateManager {

    const val KEY_HOT_UPDATE_CHANNEL = "KEY_HOT_UPDATE_CHANNEL"

    const val CHANNEL_DISABLED = 0
    const val CHANNEL_STABLE = 1
    const val CHANNEL_BETA = 3
    const val CHANNEL_CANARY = 4

    var currentChannel: Int
        get() = ConfigManager.getDefaultConfig().getIntOrDefault(KEY_HOT_UPDATE_CHANNEL, CHANNEL_DISABLED)
        set(value) {
            check(value in CHANNEL_DISABLED..CHANNEL_CANARY)
            ConfigManager.getDefaultConfig().putInt(KEY_HOT_UPDATE_CHANNEL, value)
        }

    val isHotUpdateEnabled: Boolean
        get() = currentChannel > 0

}
