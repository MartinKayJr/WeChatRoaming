package cn.martinkay.wechatroaming.settings.dsl.cell

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.res.ResourcesCompat
import cn.martinkay.wechatroaming.R
import cn.martinkay.wechatroaming.utils.LayoutHelper
import cn.martinkay.wechatroaming.utils.LayoutHelper.MATCH_PARENT
import cn.martinkay.wechatroaming.utils.LayoutHelper.WRAP_CONTENT
import cn.martinkay.wechatroaming.utils.LayoutHelperViewScope
import cn.martinkay.wechatroaming.utils.ui.ThemeAttrUtils

class TitleValueCell(
    context: Context,
) : FrameLayout(context), LayoutHelperViewScope {

    val titleView: TextView
    val summaryView: TextView
    val valueView: TextView
    val switchView: SwitchCompat

    private val dividerColor: Int
    private val dip1: Float = 1.dp.toFloat()
    private val errorLineColor: Int

    private val dividerPaint by lazy { Paint() }

    private val mCenterVertical = LayoutHelper.newFrameLayoutParamsRel(MATCH_PARENT, WRAP_CONTENT,
            Gravity.CENTER_VERTICAL or Gravity.START, 21.dp, 0, 21.dp, 0)
    private val mCenterTop = LayoutHelper.newFrameLayoutParamsRel(MATCH_PARENT, WRAP_CONTENT,
            Gravity.TOP or Gravity.START, 21.dp, 10.dp, 21.dp, 0)

    init {
        minimumHeight = 50.dp
        dividerColor = ResourcesCompat.getColor(resources, R.color.divideColor, context.theme)
        // title text view
        titleView = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            setTextColor(ResourcesCompat.getColor(resources, R.color.firstTextColor, context.theme))
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }.also {
            addView(it, mCenterVertical)
        }
        // summary text view
        summaryView = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            setTextColor(ResourcesCompat.getColor(resources, R.color.thirdTextColor, context.theme))
            gravity = Gravity.START
            visibility = GONE
        }.also {
            addView(it, LayoutHelper.newFrameLayoutParamsRel(WRAP_CONTENT, WRAP_CONTENT,
                    Gravity.TOP or Gravity.START, 21.dp, 34.dp, 70.dp, 6.dp))
        }
        val valueTextColor = ThemeAttrUtils.resolveColorOrDefaultColorRes(context, androidx.appcompat.R.attr.colorAccent, R.color.colorAccent)
        errorLineColor = ThemeAttrUtils.resolveColorOrDefaultColorInt(context, R.attr.unusableColor, valueTextColor)
        // value text view
        valueView = TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            setTextColor(valueTextColor)
            visibility = GONE
        }.also {
            addView(it, LayoutHelper.newFrameLayoutParamsRel(WRAP_CONTENT, WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL or Gravity.END, 22.dp, 0, 22.dp, 0))
        }
        // switch view
        switchView = SwitchCompat(context).apply {
            visibility = GONE
            // disable click for default because this behavior is managed by the recycler view,
            // but they can still set onCheckedChangeListener if they want
            isClickable = false
        }.also {
            addView(it, LayoutHelper.newFrameLayoutParamsRel(WRAP_CONTENT, WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL or Gravity.END, 22.dp, 0, 22.dp, 0))
        }
    }

    var title: String
        get() = titleView.text?.toString() ?: ""
        set(value) {
            titleView.text = value
            invalidate()
        }

    var summary: CharSequence?
        get() = summaryView.text
        set(value) {
            summaryView.text = value
            summaryView.visibility = if (value.isNullOrEmpty()) GONE else VISIBLE
            titleView.layoutParams = if (value.isNullOrEmpty()) mCenterVertical else mCenterTop
            requestLayout()
        }

    var value: String?
        get() = valueView.text.toString()
        set(value) {
            valueView.text = value
            valueView.visibility = if (value.isNullOrEmpty()) GONE else VISIBLE
            if (!value.isNullOrEmpty()) {
                // value text and switch view are in the same position
                switchView.visibility = GONE
            }
            requestLayout()
        }

    var isHasSwitch: Boolean
        get() = switchView.visibility == VISIBLE
        set(value) {
            switchView.visibility = if (value) VISIBLE else GONE
            if (isHasSwitch) {
                // value text and switch view are in the same position
                valueView.visibility = GONE
            }
        }

    var isChecked: Boolean
        get() = switchView.isChecked
        set(value) {
            switchView.isChecked = value
            if (!isHasSwitch) {
                isHasSwitch = true
            }
        }

    var hasError: Boolean = false
        set(value) {
            val needInvalidate = field != value
            field = value
            if (needInvalidate) {
                invalidate()
            }
        }

    var hasDivider: Boolean = true
        set(value) {
            var needInvalidate = false
            if (value != field) {
                needInvalidate = true
            }
            field = value
            if (needInvalidate) {
                invalidate()
            }
        }

    fun isClickOnSwitch(x: Int): Boolean {
        return isHasSwitch && (x >= switchView.left && x <= switchView.right)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (hasDivider) {
            dividerPaint.strokeWidth = dip1
            dividerPaint.color = dividerColor
            canvas.drawLine(0f, measuredHeight.toFloat(), measuredWidth.toFloat(), measuredHeight.toFloat(), dividerPaint)
        }
        if (hasError) {
            dividerPaint.strokeWidth = dip1 * 2f
            dividerPaint.color = errorLineColor
            val textWidth = titleView.paint.measureText(titleView.text.toString())
            val startX = titleView.left
            // startY is baseline
            val startY = titleView.baseline + titleView.top + dip1 * 2f
            val endX = startX + textWidth
            canvas.drawLine(startX.toFloat(), startY, endX, startY, dividerPaint)
        }
    }
}
