package cn.martinkay.wechatroaming.utils

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLConnection

object SystemServiceUtils {
    /**
     * Copy text to system clipboard
     *
     * @param context [Context]
     * @param text    text will be copied.
     */
    @JvmStatic
    fun copyToClipboard(context: Context, text: CharSequence) {
        if (text.isEmpty()) {
            return
        }
        val clipData = ClipData.newPlainText("", text)
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(clipData)
    }

    /**
     * Copy file to system clipboard
     *
     * @param context [Context]
     * @param file    [File]
     */
    @JvmStatic
    fun copyToClipboard(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        copyToClipboard(context, uri)
    }

    /**
     * Copy uri to system clipboard
     *
     * @param context [Context]
     * @param uri     [Uri]
     */
    @SuppressLint("Recycle")
    @JvmStatic
    fun copyToClipboard(context: Context, uri: Uri) {
        val item = ClipData.Item(uri)
        val mimeType = context.contentResolver.openInputStream(uri)?.buffered().use {
            URLConnection.guessContentTypeFromStream(it)
        }
        val clipData = ClipData("", arrayOf(mimeType), item)
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(clipData)
    }
}
