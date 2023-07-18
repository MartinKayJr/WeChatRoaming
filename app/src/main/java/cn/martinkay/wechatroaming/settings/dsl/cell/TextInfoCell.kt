package cn.martinkay.wechatroaming.settings.dsl.cell

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import cn.martinkay.wechatroaming.R
import cn.martinkay.wechatroaming.utils.LayoutHelper
import cn.martinkay.wechatroaming.utils.LayoutHelperViewScope
import cn.martinkay.wechatroaming.utils.ui.ThemeAttrUtils

class TextInfoCell @JvmOverloads constructor(context: Context, padding: Int = 21)
    : FrameLayout(context), LayoutHelperViewScope {

    val textView: TextView
    private var topPadding = 10
    private var bottomPadding = 17
    private var fixedSize = 0

    var textColor: Int
        get() = textView.currentTextColor
        set(value) {
            textView.setTextColor(value)
        }
    var textLinkColor: Int
        get() = textView.currentTextColor
        set(value) {
            textView.setLinkTextColor(value)
        }
    var text: CharSequence? = null
        set(value) {
            if (!TextUtils.equals(value, field)) {
                field = value
                if (value == null) {
                    textView.setPadding(0, 2.dp, 0, 0)
                } else {
                    textView.setPadding(0, topPadding.dp, 0, bottomPadding.dp)
                }
                var spannableString: SpannableString? = null
                if (value != null) {
                    var i = 0
                    val len = value.length
                    while (i < len - 1) {
                        if (value[i] == '\n' && value[i + 1] == '\n') {
                            if (spannableString == null) {
                                spannableString = SpannableString(value)
                            }
                            spannableString.setSpan(AbsoluteSizeSpan(10, true), i + 1, i + 2,
                                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        i++
                    }
                }
                textView.text = spannableString ?: value
            }
        }

    var textIsSelectable: Boolean
        get() = textView.isTextSelectable
        set(value) {
            textView.setTextIsSelectable(value)
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (fixedSize != 0) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(fixedSize.dp, MeasureSpec.EXACTLY))
        } else {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        }
    }

    fun setTopPadding(topPadding: Int) {
        this.topPadding = topPadding
    }

    fun setBottomPadding(value: Int) {
        bottomPadding = value
    }

    fun setFixedSize(size: Int) {
        fixedSize = size
    }

    val length: Int
        get() = textView.text?.length ?: 0

    fun setEnabled(value: Boolean, animators: ArrayList<Animator?>?) {
        if (animators != null) {
            animators.add(ObjectAnimator.ofFloat(textView, "alpha", if (value) 1f else 0.5f))
        } else {
            textView.alpha = if (value) 1f else 0.5f
        }
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = TextView::class.java.name
        info.text = text
    }

    init {
        textView = AppCompatTextView(context)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        textView.gravity = Gravity.START
        textView.setPadding(0, 10.dp, 0, 17.dp)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setTextColor(textColor)
        textView.setLinkTextColor(textLinkColor)
        textView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        textColor = ResourcesCompat.getColor(context.resources, R.color.thirdTextColor, context.theme)
        textLinkColor = ThemeAttrUtils.resolveColorOrDefaultColorRes(context, androidx.appcompat.R.attr.colorAccent, R.color.colorAccent)
        addView(textView, LayoutHelper.newFrameLayoutParamsAbs(
            LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT,
                Gravity.START or Gravity.TOP, padding.dp, 0, padding.dp, 0))
    }
}
