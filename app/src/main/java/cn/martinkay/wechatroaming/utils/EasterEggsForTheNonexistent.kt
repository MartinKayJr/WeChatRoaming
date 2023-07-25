package cn.martinkay.wechatroaming.utils

import android.content.Context
import android.util.Base64
import androidx.appcompat.app.AlertDialog

fun processSearchEasterEgg(text: String, context: Context) {
    for (pair in easterEggsMap) {
        for (key in pair.key) {
            if (text.contains(key, true)) {
                AlertDialog.Builder(context)
                    .setTitle(pair.value.first)
                    .setMessage(pair.value.second)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
                return
            }
        }
    }
}

private val easterEggsMap by lazy {
    hashMapOf(
        arrayOf("\u26A7\uFE0F", "\uD83C\uDF65", "mtf", "mtx", "ftm", "ftx", "transgender") to forSuBanXia,
        arrayOf("喵") to ("喵喵" to "喵喵喵")
    )
}

val forSuBanXia: Pair<String, String> = (String(Base64.decode("Rm9yIHVzIA==", Base64.DEFAULT)) + "\uD83C\uDFF3\uFE0F\u200D\u26A7\uFE0F" to String(
    Base64.decode(
        "5oS/5q+P5LiA5Liq5Lq66YO96IO96Ieq55Sx55qE55Sf5rS75Zyo6Ziz5YWJ5LiL77yM5oS/5oiR55qE6byT5Yqx5LiO5YuH5rCU6ZqPUUF1eGlsaWFyeeS8tOS9oOi6q+aXgeOAggoKCQkJCeKAlOKAlENyeW9saXRpYSwgYW4gZXhvcmRpbmFyeSBkZXZlbG9wZXIsIGFuIG9yZGluYXJ5IE10Rg==",
        Base64.CRLF
    )
))
