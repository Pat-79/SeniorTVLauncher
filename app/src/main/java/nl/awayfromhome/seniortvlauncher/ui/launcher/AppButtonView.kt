package nl.awayfromhome.seniortvlauncher.ui.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import nl.awayfromhome.seniortvlauncher.R
import nl.awayfromhome.seniortvlauncher.data.AppInfo
import nl.awayfromhome.seniortvlauncher.data.ButtonShape
import nl.awayfromhome.seniortvlauncher.utils.ColorUtils
import kotlin.math.sqrt

class AppButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val iconContainer: ConstraintLayout
    private val iconView: ImageView
    private val appNameView: TextView
    private val addIconView: ImageView

    private var dominantColor: Int = Color.WHITE
    private var isFocused: Boolean = false
    private var isEmptySlot: Boolean = true
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        LayoutInflater.from(context).inflate(R.layout.item_app_button, this, true)
        iconContainer = findViewById(R.id.icon_container)
        iconView = findViewById(R.id.app_icon)
        appNameView = findViewById(R.id.app_name)
        addIconView = findViewById(R.id.add_icon)

        isFocusable = true
        isClickable = true
        descendantFocusability = FOCUS_BLOCK_DESCENDANTS
        setWillNotDraw(false)
    }

    fun bindApp(appInfo: AppInfo, showName: Boolean, shape: ButtonShape) {
        dominantColor = appInfo.dominantColor
        isEmptySlot = false
        iconView.setImageDrawable(appInfo.icon)
        iconView.visibility = VISIBLE
        addIconView.visibility = GONE
        appNameView.text = appInfo.label
        appNameView.visibility = if (showName) VISIBLE else GONE
        isFocusable = true
        isClickable = true
        isEnabled = true
        alpha = 1f
        applyShape(shape, false)
    }

    fun bindEmpty(shape: ButtonShape) {
        dominantColor = Color.WHITE
        isEmptySlot = true
        iconView.setImageDrawable(null)
        iconView.visibility = GONE
        addIconView.visibility = GONE
        appNameView.visibility = GONE
        isFocusable = false
        isClickable = false
        isEnabled = false
        alpha = 0f
        applyShape(shape, true)
    }

    companion object {
        private const val STROKE_WIDTH_FOCUSED = 8
        private const val STROKE_WIDTH_DEFAULT = 3
    }

    override fun onDraw(canvas: Canvas) {
        if (isFocused && !isEmptySlot && width > 0 && height > 0) {
            val cx = width / 2f
            val cy = height / 2f
            val radius = sqrt((cx * cx) + (cy * cy))
            val shader = RadialGradient(
                cx, cy, radius,
                intArrayOf(
                    ColorUtils.withAlpha(dominantColor, 180),
                    ColorUtils.withAlpha(dominantColor, 80),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0.15f, 0.55f, 1.0f),
                Shader.TileMode.CLAMP
            )
            glowPaint.shader = shader
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)
        }
        super.onDraw(canvas)
    }

    private fun applyShape(shape: ButtonShape, isEmpty: Boolean) {
        val background = GradientDrawable()
        val fillColor = when {
            isEmpty -> Color.argb(40, 255, 255, 255)
            isFocused -> ColorUtils.withAlpha(lightenColor(dominantColor, 30), 110)
            else -> ColorUtils.withAlpha(dominantColor, 40)
        }
        val strokeColor = when {
            isEmpty -> Color.argb(80, 255, 255, 255)
            isFocused -> lightenColor(dominantColor, 80)
            else -> dominantColor
        }

        background.setColor(fillColor)
        background.setStroke(if (isFocused) STROKE_WIDTH_FOCUSED else STROKE_WIDTH_DEFAULT, strokeColor)

        when (shape) {
            ButtonShape.CIRCLE -> background.shape = GradientDrawable.OVAL
            ButtonShape.ROUNDED_SQUARE -> {
                background.shape = GradientDrawable.RECTANGLE
                background.cornerRadius = 24f * resources.displayMetrics.density
            }
            ButtonShape.SQUARE -> {
                background.shape = GradientDrawable.RECTANGLE
                background.cornerRadius = 0f
            }
        }

        iconContainer.background = background
        iconContainer.elevation = (if (isFocused) 24f else 4f) * resources.displayMetrics.density

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val shadowColor = if (isFocused && !isEmpty) dominantColor else Color.BLACK
            iconContainer.outlineAmbientShadowColor = shadowColor
            iconContainer.outlineSpotShadowColor = shadowColor
        }
    }

    private fun lightenColor(color: Int, amount: Int): Int {
        val r = (Color.red(color) + amount).coerceAtMost(255)
        val g = (Color.green(color) + amount).coerceAtMost(255)
        val b = (Color.blue(color) + amount).coerceAtMost(255)
        return Color.argb(255, r, g, b)
    }

    fun setFocusedState(focused: Boolean, shape: ButtonShape) {
        isFocused = focused
        if (focused) {
            animate().scaleX(1.12f).scaleY(1.12f).setDuration(150).start()
        } else {
            animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start()
        }
        applyShape(shape, isEmptySlot)
        invalidate()
    }
}
